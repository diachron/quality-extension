package com.google.refine.quality.commands;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONWriter;

import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.commands.Command;
import com.google.refine.history.Change;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.changes.CellChange;
import com.google.refine.process.QuickHistoryEntryProcess;
import com.google.refine.quality.exceptions.QualityExtensionException;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.utilities.Utilities;
import com.google.refine.util.Pool;

public class TransformData extends Command {

  private static final Logger LOG = Logger.getLogger(TransformData.class);

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    Project project = getProject(request);
    List<Quad> quads = JenaModelLoader.getQuads(Utilities.projectToInputStream(project));

    EditOneCellProcess process = null;
    HistoryEntry historyEntry = null;

    int rowIndex = 0;
    int cellIndex = 2;
    for (Quad qaud : quads) {

      StringBuilder value = new StringBuilder();
      value.append(qaud.getSubject());
      value.append(Constants.COLUMN_SPLITER);
      value.append(qaud.getPredicate());
      value.append(Constants.COLUMN_SPLITER);
      value.append(qaud.getObject());

      process = new EditOneCellProcess(project, "Edit single cell", rowIndex++, cellIndex,
        value.toString());
      try {
        historyEntry = project.processManager.queueProcess(process);
      } catch (Exception e) {
        LOG.error(e.getLocalizedMessage());
      }
    }
    updateCell(historyEntry, process, response);
  }

  private void updateCell(HistoryEntry historyEntry, EditOneCellProcess process,
    HttpServletResponse response) {
    try {
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
    } catch (JSONException e) {
      LOG.error(e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage());
    } catch (ServletException e) {
      LOG.error(e.getLocalizedMessage());
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
    protected HistoryEntry createHistoryEntry(long historyEntryID) {
      Cell cell = _project.rows.get(rowIndex).getCell(cellIndex);
      Column column = _project.columnModel.getColumnByCellIndex(cellIndex);
      if (column == null) {
        throw new QualityExtensionException("No such column");
      }

      newCell = new Cell(value, cell != null ? cell.recon : null);
      String description = String.format("Edit single cell on row %s, column %s", (rowIndex + 1),
        column.getName());
      Change change = new CellChange(rowIndex, cellIndex, cell, newCell);

      return new HistoryEntry(historyEntryID, _project, description, null, change);
    }
  }
}
