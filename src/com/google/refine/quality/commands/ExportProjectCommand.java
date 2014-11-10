package com.google.refine.quality.commands;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.refine.Jsonizable;
import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.quality.utilities.Utilities;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Quad;

public class ExportProjectCommand extends Command {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Project project = getProject(request);
    if (project.getMetadata().getCustomMetadata("Transformed") != null) {

      final Map<String, String> table = new HashMap<String, String>();
      List<Quad> quads = Utilities.getQuadsFromProject(project);
      try {
        JSONArray serializations = new JSONArray(request.getParameter("serializations"));
        for (int i = 0; i < serializations.length(); i++) {
          Model model = createModel(quads, project);
          String serial = (String) serializations.get(i);

          Writer writer = new StringWriter();
          model.write(writer, serial);
          table.put(serial, writer.toString());
        }

        respondJSON(response, new Jsonizable() {
          @Override
          public void write(JSONWriter writer, Properties options) throws JSONException {
            writer.object();
            writer.key("data").value(table);
            writer.endObject();
          }
        });

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Creates a Jena model, from a list of quads.
   * @param quads A list of quads.
   * @param project An OpenRefine project.
   * @return A Jena model object.
   */
  private Model createModel(List<Quad> quads, Project project) {
    Model model = ModelFactory.createDefaultModel();
    String prefixes = (String) project.getMetadata().getCustomMetadata("prefixes");
    model.setNsPrefixes(readPrefixesMapFromMetadata(prefixes));

    for (Quad quad : quads) {
      model.add(Utilities.createStatement(quad.getSubject().toString(), quad.getPredicate()
          .toString(), quad.getObject().toString()));
    }
    return model;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> readPrefixesMapFromMetadata(String serializedMap) {
    XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(serializedMap.getBytes()));
    return (Map<String, String>) xmlDecoder.readObject();
  }
}
