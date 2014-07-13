package com.google.refine.quality.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.formula.functions.Rows;
import org.json.JSONWriter;

import com.google.refine.commands.Command;
import com.google.refine.history.Change;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.model.changes.CellChange;
import com.google.refine.process.QuickHistoryEntryProcess;
import com.google.refine.quality.utilities.LoadJenaModel;
import com.google.refine.quality.utilities.Utilities;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.Pool;
import com.hp.hpl.jena.sparql.core.Quad;


public class TransformData extends Command {
    
    protected final String splitter = "|&SPLIT&|";
    /**
     * Retrieves rdf - turtel data from project. RDF data is only stored in first column of the data grid.
     * 
     * @param project
     * @return
     */
    protected InputStream retrieveRDFData(Project project) {
        
        InputStream inputStream = null;

        try {
            int start = 0; 
            int limit = project.rows.size(); 
            
            String tmpStr = "";
            
            for (int i=start; i < limit; i++){
                if (null == project.rows.get(i).getCell(0)) {
                    tmpStr += "\n";
                }
                else {
                    tmpStr += project.rows.get(i).getCell(0).toString() + "\n";
                }
            }
            
            inputStream = IOUtils.toInputStream(tmpStr, "UTF-8");
        
        } catch (IOException e){
            e.printStackTrace();
        }
        
        return inputStream;
    }
    
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        try {
            /** Get Projet Details **/
            
            // Retrieve project object
            Project project = getProject(request);

            /** Retrieve all quads from rows **/
            //List all Statemtents loaded in model 
            List<Quad> listQuad = LoadJenaModel.getQuads(retrieveRDFData(project));
            
            int i = 0;
            
            EditOneCellProcess process = null;
            HistoryEntry historyEntry = null;
            
            for (Quad tmpQuad : listQuad) {
                
                int rowIndex = i;
                int cellIndex = 1;

                String type = "String";
                String valueString = tmpQuad.getSubject() + splitter + tmpQuad.getPredicate() + splitter + tmpQuad.getObject(); 
                Serializable value = null;
                
                if ("number".equals(type)) {
                    value = Double.parseDouble(valueString);
                } else if ("boolean".equals(type)) {
                    value = "true".equalsIgnoreCase(valueString);
                } else if ("date".equals(type)) {
                    value = ParsingUtilities.stringToDate(valueString);
                } else {
                    value = valueString;
                }

                process = new EditOneCellProcess(
                    project,
                    "Edit single cell",
                    rowIndex,
                    cellIndex,
                    value
                );

                historyEntry = project.processManager.queueProcess(process);
                i++;
            }
            
            if (historyEntry != null) {
                /*
                 * If the operation has been done, return the new cell's data
                 * so the client side can update the cell's rendering right away.
                 */
                JSONWriter writer = new JSONWriter(response.getWriter());

                Pool pool = new Pool();
                Properties options = new Properties();
                options.put("pool", pool);

                writer.object();
                writer.key("code"); writer.value("ok");
                writer.key("historyEntry"); historyEntry.write(writer, options);
                writer.key("cell"); process.newCell.write(writer, options);
                writer.key("pool"); pool.write(writer, options);
                writer.endObject();
            } else {
                respond(response, "{ \"code\" : \"pending\" }");
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected static class EditOneCellProcess extends QuickHistoryEntryProcess {
        final int rowIndex;
        final int cellIndex;
        final Serializable value;
        Cell newCell;

        EditOneCellProcess(
            Project project,
            String briefDescription,
            int rowIndex,
            int cellIndex,
            Serializable value
        ) {
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

            newCell = new Cell(
                value,
                cell != null ? cell.recon : null
            );

            String description =
                "Edit single cell on row " + (rowIndex + 1) +
                ", column " + column.getName();

            Change change = new CellChange(rowIndex, cellIndex, cell, newCell);

            return new HistoryEntry(
                historyEntryID, _project, description, null, change);
        }
    }
    
}
