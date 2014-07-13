package com.google.refine.quality.commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.refine.browsing.Engine;
import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.quality.utilities.LoadJenaModel;
import com.google.refine.quality.utilities.Utilities;
import com.google.refine.quality.metrics.EmptyAnnotationValue;
import com.google.refine.quality.metrics.MalformedDatatypeLiterals;
import com.hp.hpl.jena.sparql.core.Quad;

public class AssessQuality extends Command{
    
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
     * process quads for EmptyAnnotationValue
     * 
     * @param inputStream
     */
    protected void processForEmptyAnnotationValue(List<Quad> listQuad){
        
        EmptyAnnotationValue.loadAnnotationPropertiesSet(null);
        
        EmptyAnnotationValue emptyAnnotationValue = new EmptyAnnotationValue();
        emptyAnnotationValue.compute(listQuad);
        
        if (emptyAnnotationValue.getQualityProblems().isEmpty())
        {
            System.out.println("No problem found for EmptyAnnotationValue");
        }
        else {
            for (Quad quad : emptyAnnotationValue.getQualityProblems()){
                Utilities.printQuad(quad, System.out);
            }
        }
        
        EmptyAnnotationValue.clearAnnotationPropertiesSet();
    }
    
    /**
     * Process quads for MalformedDatatypeLiterals
     * 
     * @param is
     */
    protected void processForMalformedDatatypeLiterals(List<Quad> listQuad) {
        
        MalformedDatatypeLiterals malformedDatatypeLiterals = new MalformedDatatypeLiterals();
        malformedDatatypeLiterals.compute(listQuad);
        
        if (malformedDatatypeLiterals.getQualityProblems().isEmpty()) {
            System.out.println("No problem found for MalformedDatatypeLiterals");
        }
        else {
            for (Quad quad : malformedDatatypeLiterals.getQualityProblems()){
                Utilities.printQuad(quad, System.out);
            }
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        System.out.println("Retrieve Rows");
        
        try {
            
            /** Get Projet Details **/
            
            // Retrieve project object
            Project project = getProject(request);

            /** For Debug Only **/
            
            //List all Statemtents loaded in model 
            Utilities.printStatements(LoadJenaModel.getModel(retrieveRDFData(project)).listStatements(), System.out);
            
            /** Get Project Data **/
            
            // Retrieve rdf data from project
            InputStream inputStream = retrieveRDFData(project);
            // Retrieve all quad in model
            List<Quad> listQuad = LoadJenaModel.getQuads(inputStream);
            // Close input stream
            inputStream.close();
            
            /** Compute Metrics **/
            
            // for Empty Annotation value 
            processForEmptyAnnotationValue(listQuad);
            
            // for Malformed Datatype Literals
            processForMalformedDatatypeLiterals(listQuad);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
