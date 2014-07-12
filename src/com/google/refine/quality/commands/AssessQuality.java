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
                tmpStr += project.rows.get(i).getCell(0).toString() + "\n";
            }
            
            inputStream = IOUtils.toInputStream(tmpStr, "UTF-8");
        
        } catch (IOException e){
            e.printStackTrace();
        }
        
        return inputStream;
    }
    
    /**
     * Process records for MalformedDatatypeLiterals
     * 
     * @param is
     */
    protected void processForMalformedDatatypeLiterals(InputStream is) {
        
        MalformedDatatypeLiterals malformedDatatypeLiterals = new MalformedDatatypeLiterals();
        malformedDatatypeLiterals.compute(LoadJenaModel.getQuads(is));
        
        for (Quad quad : malformedDatatypeLiterals.getQualityProblems()){
            System.out.print("Subject : " + quad.getSubject());
            System.out.print("-- Predicate : " + quad.getPredicate());
            System.out.println("--> Object : " + quad.getObject());
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Retrieve Rows");
        try {
            
            Project project = getProject(request);
            
            InputStream inputStream = retrieveRDFData(project);
            processForMalformedDatatypeLiterals(inputStream);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
