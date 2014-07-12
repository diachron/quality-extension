package com.google.refine.quality.commands;

import java.io.IOException;
import java.io.InputStream;

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
    protected void processForEmptyAnnotationValue(InputStream inputStream){
        
        EmptyAnnotationValue.loadAnnotationPropertiesSet(null);
        
        EmptyAnnotationValue emptyAnnotationValue = new EmptyAnnotationValue();
        emptyAnnotationValue.compute(LoadJenaModel.getQuads(inputStream));
        
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
    protected void processForMalformedDatatypeLiterals(InputStream inputStream) {
        
        MalformedDatatypeLiterals malformedDatatypeLiterals = new MalformedDatatypeLiterals();
        malformedDatatypeLiterals.compute(LoadJenaModel.getQuads(inputStream));
        
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
            
            // Retrieve project object
            Project project = getProject(request);
            
            // Retrieve rdf data from project
            InputStream inputStream = retrieveRDFData(project);
            
            // List all Statemtents loaded in model
            Utilities.printStatements(LoadJenaModel.getModel(inputStream).listStatements(), System.out);
            
            /** Compute Metrics **/
            
            // for Empty Annotation value 
            processForEmptyAnnotationValue(inputStream);
            
            // for Malformed Datatype Literals
            processForMalformedDatatypeLiterals(inputStream);
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
