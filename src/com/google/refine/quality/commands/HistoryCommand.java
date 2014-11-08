package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.google.refine.commands.Command;
import com.google.refine.model.Project;

public class HistoryCommand extends Command {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Project project = getProject(request);
    int number = Integer.parseInt((String) project.getMetadata().getCustomMetadata("triples"));
    List<String> metrics = (List<String>) project.getMetadata().getCustomMetadata("metrics");

    
    try {
      respondJSON(response, project.history);
    } catch (JSONException e) {
      respondException(response, e);
    }
  }
}


