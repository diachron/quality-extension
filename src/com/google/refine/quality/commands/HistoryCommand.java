package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.refine.Jsonizable;
import com.google.refine.commands.Command;
import com.google.refine.model.Project;

public class HistoryCommand extends Command {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Project project = getProject(request);

    final int number = (Integer) project.getMetadata().getCustomMetadata("triples");
    final ArrayList<String> metrics = (ArrayList<String>) project.getMetadata().getCustomMetadata("metrics");
    final Map<String, Integer> table = new HashMap<String, Integer>();

    for (String metric : metrics) {
      table.put(metric, (Integer) project.getMetadata().getCustomMetadata(metric));
    }

    try {
      respondJSON(response, new Jsonizable() {
        @Override
        public void write(JSONWriter writer, Properties options) throws JSONException {
          writer.object();
          writer.key("problems_count").value(table);
          writer.key("metrics").value(metrics);
          writer.key("number_triples").value(number);
          writer.endObject();
        }
      });
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
