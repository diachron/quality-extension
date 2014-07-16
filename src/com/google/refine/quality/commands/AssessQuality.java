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
import org.json.JSONWriter;

import com.google.refine.commands.Command;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.Project;
import com.google.refine.quality.utilities.LoadJenaModel;
import com.google.refine.quality.utilities.Utilities;
import com.google.refine.quality.commands.TransformData.EditOneCellProcess;
import com.google.refine.quality.metrics.AbstractQualityMetrics;
import com.google.refine.quality.metrics.EmptyAnnotationValue;
import com.google.refine.quality.metrics.HelloWorldMetrics;
import com.google.refine.quality.metrics.IncompatibleDatatypeRange;
import com.google.refine.quality.metrics.LabelsUsingCapitals;
import com.google.refine.quality.metrics.MalformedDatatypeLiterals;
import com.google.refine.quality.metrics.MisplacedClassesOrProperties;
import com.google.refine.quality.metrics.MisusedOwlDatatypeOrObjectProperties;
import com.google.refine.quality.metrics.OntologyHijacking;
import com.google.refine.quality.metrics.ReportProblems;
import com.google.refine.quality.metrics.UndefinedClasses;
import com.google.refine.quality.metrics.UndefinedProperties;
import com.google.refine.quality.metrics.WhitespaceInAnnotation;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.Pool;
import com.hp.hpl.jena.sparql.core.Quad;

public class AssessQuality extends Command{
    
    protected void writeProblemicQuads(HttpServletRequest request, HttpServletResponse response, List<ReportProblems> reportProblemsList) throws ServletException {
        try {

            /** Get Projet Details **/
            
            // Retrieve project object
            Project project = getProject(request);
            int i = 0;

            EditOneCellProcess process = null;
            HistoryEntry historyEntry = null;

            for (ReportProblems reportProblem : reportProblemsList) {

                if (reportProblemsList.size() < reportProblem.get_rowIndex()) {
                    
                int rowIndex = reportProblem.get_rowIndex();
                int cellIndex = 1;

                String type = "String";
                String valueString = "";
                if (null != project.rows.get(rowIndex).getCell(cellIndex).toString() && !project.rows.get(rowIndex).getCell(1).toString().isEmpty()){
                valueString =  project.rows.get(rowIndex).getCell(cellIndex).toString().trim() + " , " + reportProblem.get_sourceMetric() + " :: " + reportProblem.get_problemType();
                }
                else {
                    valueString = reportProblem.get_sourceMetric().toString() + " :: " + reportProblem.get_problemType();
                }
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

                process = new EditOneCellProcess(project, "Edit single cell", rowIndex, cellIndex, value);

                historyEntry = project.processManager.queueProcess(process);
                
                }
            }

            if (historyEntry != null) {
                /*
                 * If the operation has been done, return the new cell's data so
                 * the client side can update the cell's rendering right away.
                 */
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
            e.printStackTrace();
        }
    }
    
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
    
    /**
     * Process quads for given quality metric
     * 
     * @param abstractQualityMetrics
     * @param listQuad
     * @throws ServletException 
     */
    protected void processMetric(HttpServletRequest request, HttpServletResponse response, AbstractQualityMetrics abstractQualityMetrics, List<Quad> listQuad) throws ServletException{
        
        System.out.println("Processing for " +  abstractQualityMetrics.getClass());
        
        abstractQualityMetrics.compute(listQuad);
        if (abstractQualityMetrics.getQualityProblems().isEmpty()){
            System.out.println("No problem found for " + abstractQualityMetrics.getClass());
        }
        else {
            writeProblemicQuads(request, response, abstractQualityMetrics.getQualityProblems());
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        try {
             
            /** Get Projet Details **/
            
            // Retrieve project object
            Project project = getProject(request);

            //List all Statemtents loaded in model -- DEBUG ONLY 
            //Utilities.printStatements(LoadJenaModel.getModel(retrieveRDFData(project)).listStatements(), System.out);
            
            /** Get Project Data **/
            
            // Retrieve rdf data from project
            InputStream inputStream = retrieveRDFData(project);
            // Retrieve all quad in model
            List<Quad> listQuad = LoadJenaModel.getQuads(inputStream);
            // Close input stream
            inputStream.close();
            
            
            /** Compute Metrics **/
            // for Hello World Meric -- DEBUG ONLY
            //processMetric(request, response, new HelloWorldMetrics(), listQuad);
            
            // for Empty Annotation value
            EmptyAnnotationValue.loadAnnotationPropertiesSet(null); // Pre-Process
            processMetric(request, response, new EmptyAnnotationValue(), listQuad);
            EmptyAnnotationValue.clearAnnotationPropertiesSet(); //Post-Process

            //TODO homogeneousDatatypes
            
            // for IncompatiableDatatypeRange
            processMetric(request, response, new IncompatibleDatatypeRange(), listQuad);
            IncompatibleDatatypeRange.clearCache(); //Post-Process
            
            // for Malformed Datatype Literals
            processMetric(request, response, new MalformedDatatypeLiterals(), listQuad);
            
            
            // for MisplacedClassesOrProperties -- DISABLE B/C TAKES TOO MUCH TIME
            ////processMetric(request, response, new MisplacedClassesOrProperties(), listQuad);
            
            // for MisusedOwlDatatypeOrObjectProperties
            ////MisusedOwlDatatypeOrObjectProperties.filterAllOwlProperties(listQuad); //Pre-Process
            ////processMetric(request, response, new MisusedOwlDatatypeOrObjectProperties(), listQuad);
            ////MisusedOwlDatatypeOrObjectProperties.clearAllOwlPropertiesList(); //Post-Process
            
            // for OntologyHijacking
            //processMetric(request, response, new OntologyHijacking(), listQuad);
            
            // for WhitespaceInAnnotation
            WhitespaceInAnnotation.loadAnnotationPropertiesSet(null); //Pre-Process
            processMetric(request, response, new WhitespaceInAnnotation(), listQuad);
            WhitespaceInAnnotation.clearAnnotationPropertiesSet(); //Post-Process
            
            // for LabelUsingCapitals
            LabelsUsingCapitals.loadAnnotationPropertiesSet(null); //Pre-Process
            processMetric(request, response, new LabelsUsingCapitals(), listQuad);
            LabelsUsingCapitals.clearAnnotationPropertiesSet();
            
            // for Undefined Classes
            processMetric(request, response, new UndefinedClasses(), listQuad);
            
            // for Undefined Properties
            processMetric(request, response, new UndefinedProperties(), listQuad);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
