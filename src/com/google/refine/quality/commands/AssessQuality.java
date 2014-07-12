package com.google.refine.quality.commands;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.refine.browsing.Engine;
import com.google.refine.commands.Command;
import com.google.refine.model.Project;


public class AssessQuality extends Command{
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Retrieve Rows");
        try {
            
            Project project = getProject(request);
            Engine engine = getEngine(request, project);
            
            int start = Math.min(project.rows.size(), Math.max(0, getIntegerParameter(request, "start", 0)));
            int limit = Math.min(project.rows.size() - start, Math.max(0, getIntegerParameter(request, "limit", 20)));
            
            for (int i=start; i < limit; i++){
                System.out.println(project.rows.get(i));
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
