package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

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
      
      List<Quad> quads = JenaModelLoader.getQuads(Utilities.projectToInputStream(project));
      RefineCommands.addColumn(project, request, response, "Column 2", "Column 1", 1);

      int rowIndex = 0;
      int cellIndex = 1;
      for (Quad qaud : quads) {

        StringBuilder value = new StringBuilder();
        value.append(qaud.getSubject());
        value.append(Constants.COLUMN_SPLITER);
        value.append(qaud.getPredicate());
        value.append(Constants.COLUMN_SPLITER);
        value.append(qaud.getObject());

        RefineCommands.editCell(project, request, response, rowIndex++, cellIndex, value.toString());
        LOG.info(String.format("Edit single cell at row: %s, col: %s", rowIndex, cellIndex));
      }

      RefineCommands.splitColumn(project, request, response, "Column 2", 3);
      RefineCommands.renameColumn(project, request, response, "Subject", "Column 2 1");
      RefineCommands.renameColumn(project, request, response, "Predicate", "Column 2 2");
      RefineCommands.renameColumn(project, request, response, "Object", "Column 2 3");
      RefineCommands.removeColumn(project, request, response, "Column 1");
    }
  }
}
