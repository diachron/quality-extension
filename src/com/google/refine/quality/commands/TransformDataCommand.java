package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.quality.utilities.Constants;
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
      project.getMetadata().setCustomMetadata("Transformed", true);
      
      Model model = JenaModelLoader.getModel(Utilities.projectToInputStream(project));
      writePrefixesToMetadata(model, project);
      List<Quad> quads = JenaModelLoader.getQuads(model);
      
      RefineCommands.addColumn(project, request, response, "Column 2", "Column 1", 1);

      int row = 0;
      int cell = 1;
      for (Quad quad : quads) {
        RefineCommands.editCell(project, request, response, row++, cell, createTripleString(quad));
        LOG.info(String.format("Edit single cell at row: %s, col: %s", row, cell));
      }

      RefineCommands.splitColumn(project, request, response, "Column 2", 3);
      renameColumns(project, request, response);
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

  private void renameColumns(Project project, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    RefineCommands.renameColumn(project, request, response, "Subject", "Column 2 1");
    RefineCommands.renameColumn(project, request, response, "Predicate", "Column 2 2");
    RefineCommands.renameColumn(project, request, response, "Object", "Column 2 3");
    RefineCommands.removeColumn(project, request, response, "Column 1");
  }

  private String createTripleString(Quad quad) {
    StringBuilder value = new StringBuilder();
    value.append(quad.getSubject());
    value.append(Constants.COLUMN_SPLITER);
    value.append(quad.getPredicate());
    value.append(Constants.COLUMN_SPLITER);
    value.append(quad.getObject());
    return value.toString();
  }
}
