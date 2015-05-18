package com.google.refine.quality.utilities;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.refine.commands.Command;
import com.google.refine.history.Change;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.changes.CellChange;
import com.google.refine.operations.cell.MultiValuedCellSplitOperation;
import com.google.refine.operations.cell.TextTransformOperation;
import com.google.refine.operations.column.ColumnAdditionOperation;
import com.google.refine.operations.column.ColumnRemovalOperation;
import com.google.refine.process.Process;
import com.google.refine.process.QuickHistoryEntryProcess;
import com.google.refine.util.Pool;

public class RefineCommands extends Command {

  public static void addColumn(Project project, HttpServletRequest request,
    HttpServletResponse response, String newColumnName, String baseColumnName,
    int columnInsertIndex) throws IOException, ServletException {
    try {
      String expression = "value.replace(/(?s).*/, \"\")";
      String onError = "set-to-blank";
      JSONObject engineConfig = new JSONObject();
      engineConfig.put("facets", new JSONArray());
      engineConfig.put("mode", "row-based");

      AbstractOperation op = new ColumnAdditionOperation(engineConfig, baseColumnName, expression,
          TextTransformOperation.stringToOnError(onError), newColumnName, columnInsertIndex);

      Process process = op.createProcess(project, new Properties());

      performProcessAndRespond(request, response, project, process);
    } catch (Exception e) {
      e.printStackTrace();
      respondException(response, e);
    }
  }

  public static void removeColumn(Project project, HttpServletRequest request,
    HttpServletResponse response, String columnName) throws IOException, ServletException {
    try {
      AbstractOperation op = new ColumnRemovalOperation(columnName);
      Process process = op.createProcess(project, new Properties());

      performProcessAndRespond(request, response, project, process);
    } catch (Exception e) {
      respondException(response, e);
    }
  }

  public static void splitMultiColumn(Project project, HttpServletRequest request,
      HttpServletResponse response, String columnName,  String keyColumnName) throws IOException,
      ServletException {
    try {
      String separator =  Constants.ROW_SPLITER;
      String mode = "plain";

      AbstractOperation op = new MultiValuedCellSplitOperation(columnName, keyColumnName,
        separator, mode);
      Process process = op.createProcess(project, new Properties());

      performProcessAndRespond(request, response, project, process);
    } catch (Exception e) {
      respondException(response, e);
    }
  }

  public static void editCell(Project project, HttpServletRequest request,
    HttpServletResponse response, int rowIndex, int cellIndex, String value) throws IOException,
      ServletException {
    try {
      EditOneCellProcess process = new EditOneCellProcess(project, "Edit single cell", rowIndex,
          cellIndex, value);

      HistoryEntry historyEntry = project.processManager.queueProcess(process);
      if (historyEntry != null) {
        JSONWriter writer = new JSONWriter(response.getWriter());

        Pool pool = new Pool();
        Properties options = new Properties();
        options.put("pool", pool);

        writer.object();
        writer.key("code");
        writer.value("ok");
        writer.key("historyEntry");
        historyEntry.write(writer, options);
        writer.key("cell");
        process.newCell.write(writer, options);
        writer.key("pool");
        pool.write(writer, options);
        writer.endObject();
      } else {
        respond(response, "{ \"code\" : \"pending\" }");
      }
    } catch (Exception e) {
      respondException(response, e);
    }
  }

  protected static class EditOneCellProcess extends QuickHistoryEntryProcess {
    final int rowIndex;
    final int cellIndex;
    final Serializable value;
    Cell newCell;

    EditOneCellProcess(Project project, String briefDescription, int rowIndex, int cellIndex,
        Serializable value) {
      super(project, briefDescription);

      this.rowIndex = rowIndex;
      this.cellIndex = cellIndex;
      this.value = value;
    }

    @Override
    protected HistoryEntry createHistoryEntry(long historyEntryID) throws Exception {
      Cell cell = _project.rows.get(rowIndex).getCell(cellIndex);
      Column column = _project.columnModel.getColumnByCellIndex(cellIndex);
      if (column == null) {
        throw new Exception("No such column");
      }

      newCell = new Cell(value, cell != null ? cell.recon : null);
      String description = "Edit single cell on row " + (rowIndex + 1) + ", column "
          + column.getName();
      Change change = new CellChange(rowIndex, cellIndex, cell, newCell);

      return new HistoryEntry(historyEntryID, _project, description, null, change);
    }
  }
}
