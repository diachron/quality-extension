package com.google.refine.quality.commands;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.quality.utilities.Utilities;

public class ExportProjectCommand extends Command {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    Project project = getProject(request);
    if (project.getMetadata().getCustomMetadata("Transformed") != null) {

      List<Quad> quads = Utilities.getQuadsFromProject(project);
      try {
        JSONArray serializations = new JSONArray(request.getParameter("serializations"));
        for (int i = 0; i < serializations.length(); i++) {
          Model model = createModel(quads, project);
          File file = writeModelToFile(model, (String) serializations.get(i));
          respondWithFile(response, file);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }}
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

  private void respondWithFile(HttpServletResponse response, File file) throws IOException {
    response.setContentType("application/octet-stream");
    response.setHeader("Content-disposition", "attachment; filename=" + file.getName());

    OutputStream out = response.getOutputStream();
    FileInputStream in = new FileInputStream(file);
    byte[] buffer = new byte[4096];
    int length;
    while ((length = in.read(buffer)) > 0) {
      out.write(buffer, 0, length);
    }
    in.close();
    out.flush();
  }

  private File writeModelToFile(Model model, String serialization) throws IOException {
    File file = File.createTempFile("file", ".ttl");
    OutputStream fileOutputStream = new FileOutputStream(file);
    model.write(fileOutputStream, serialization);
    return file;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> readPrefixesMapFromMetadata(String serializedMap) {
    XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(serializedMap.getBytes()));
    return (Map<String, String>) xmlDecoder.readObject();
  }
}
