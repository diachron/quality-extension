package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;
import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.utilities.RefineCommands;
import com.google.refine.quality.utilities.Utilities;

public class TransformDataCommand extends Command {
  private static final Logger LOG = Logger.getLogger(TransformDataCommand.class);

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Project project = getProject(request);

    if (project.getMetadata().getCustomMetadata("Transformed") == null) {
      addTripleColumns(project, request, response);
      project.getMetadata().setCustomMetadata("metrics", new ArrayList<String>());

      Model model = JenaModelLoader.getModel(Utilities.projectToInputStream(project));
      writePrefixesToMetadata(model, project);
      List<Quad> quads = JenaModelLoader.getQuads(model);

      for (Quad quad : quads) {
        String[] elements = getQuadComponents(quad);
        for (int i = 0; i < elements.length; i++) {
          int cell = 1 + i;
          RefineCommands.editCell(project, request, response, quads.indexOf(quad), cell, elements[i]);
          LOG.info(String.format("Edit single cell at row: %s, col: %s",  quads.indexOf(quad), cell));
        }
      }
      RefineCommands.removeColumn(project, request, response, "Column 1");
      project.getMetadata().setCustomMetadata("triples", project.rows.size());
      project.getMetadata().setCustomMetadata("Transformed", true);
      Utilities.removeEmptyRows(project);
    }
  }

  /**
   * Writes a serialized prefixes map to an OpenRefine's metadata.
   * @param model A Jena model.
   * @param project An OpenRefine project.
   */
  private void writePrefixesToMetadata(Model model, Project project) {
    String prefixes = Utilities.convertMapToString(model.getNsPrefixMap());
    project.getMetadata().setCustomMetadata("prefixes", prefixes);
  }

  private void addTripleColumns(Project project, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    RefineCommands.addColumn(project, request, response, "Subject", "Column 1", 1);
    RefineCommands.addColumn(project, request, response, "Predicate", "Subject", 2);
    RefineCommands.addColumn(project, request, response, "Object", "Predicate", 3);
  }

  private String[] getQuadComponents(Quad quad) {
    return new String[] { quad.getSubject().toString(), quad.getPredicate().toString(),
      quad.getObject().toString() };
  }
}
