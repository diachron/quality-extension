package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.quality.exceptions.QualityExtensionException;
import com.google.refine.quality.metrics.AbstractQualityMetric;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.RefineCommands;
import com.google.refine.quality.utilities.Utilities;

public class IdentifyQualityProblemsCommand extends Command {
  private static final Logger LOG = Logger.getLogger(IdentifyQualityProblemsCommand.class);

  private HttpServletResponse response;
  private HttpServletRequest request;
  private Project project;
  private List<Quad> quads;
  private HashMap<Integer, Integer> refine;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
      this.response = response;
      this.request = request;

      try {
        project = getProject(request);
        if (project.getMetadata().getCustomMetadata("Quality") == null) {
          project.getMetadata().setCustomMetadata("Quality", true);
          RefineCommands.addColumn(project, request, response, "Problem Type", "Object", 3);
          RefineCommands.addColumn(project, request, response, "Problem Description", "Problem Type", 4);
          RefineCommands.addColumn(project, request, response, "Cleaning Suggestion", "Problem Description", 5);
          RefineCommands.addColumn(project, request, response, "GREL Expresion", "Cleaning Suggestion", 6);
        }
        quads = Utilities.getQuadsFromProject(project);

        JSONArray metrics = new JSONArray(request.getParameter("metrics"));
        for (int i = 0; i < metrics.length(); i++) {
          String metricName = (String) metrics.get(i);

          if (project.getMetadata().getCustomMetadata(metricName) == null) {
            project.getMetadata().setCustomMetadata(metricName, new Boolean(true));
            Class<?> cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE, metricName));
            AbstractQualityMetric metric = (AbstractQualityMetric) cls.newInstance();

            metric.before();
            metric.compute(quads);
            postProblems(metric.getQualityProblems());
            metric.after();
          }
        }

      } catch (IOException e) {
        LOG.error(e.getLocalizedMessage());
        throw new QualityExtensionException("Project can not be written to an InputStream. "
          + e.getLocalizedMessage());
      } catch (ServletException e) {
        LOG.error(e.getLocalizedMessage());
        throw new QualityExtensionException("Can not get a project from OpenRefine. "
          + e.getLocalizedMessage());
      } catch (JSONException e) {
        throw new QualityExtensionException("Can not parse json srting containg metrics "
            + "to process. " + e.getLocalizedMessage());
      } catch (ClassNotFoundException e) {
        throw new QualityExtensionException("Can not find a class to initialize. " 
          + e.getLocalizedMessage());
      } catch (InstantiationException e) {
        throw new QualityExtensionException("Can not instantiate the class. " 
          + e.getLocalizedMessage());
      } catch (IllegalAccessException e) {
        throw new QualityExtensionException(e.getLocalizedMessage());
      } catch (SecurityException e) {
        throw new QualityExtensionException(e.getLocalizedMessage());
      } catch (IllegalArgumentException e) {
        throw new QualityExtensionException("Illegal arguments. " + e.getLocalizedMessage());
      }
    }

  /**
   * Add a problem list identified by a single metric into
   * an OpenRefine project
   * @param problems A list of identified problems.
   */
  protected void postProblems(List<QualityProblem> problems) {
    try {
      for (QualityProblem qualityProblem : problems) {
        refine = Utilities.getQuadsAsHashes(project);
        postProblem(qualityProblem);
      }
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage());
      throw new QualityExtensionException(e.getLocalizedMessage());
    } catch (ServletException e) {
      LOG.error(e.getLocalizedMessage());
      throw new QualityExtensionException(e.getLocalizedMessage());
    }
  }

  /**
   * Adds a problem description to a row of a problematic quad into
   * an OpenRefine project.
   * @param problem A quality problem.
   * @throws IOException
   * @throws ServletException
   */
  private void postProblem(QualityProblem problem) throws IOException, ServletException {
    String[] values = getQualityProblemStrings(problem);
    int quadHash = Math.abs(problem.getQuad().hashCode());
    int row = refine.get(quadHash);
    for (int i = 0; i < values.length; i++) {
      int cell = Constants.PROBLEM_CELL + i;
      String oldValue = project.rows.get(row).getCell(cell).toString();
      RefineCommands.editCell(project, request, response, row, cell, values[i]);
      LOG.info(String.format("Edit single cell at row: %s, col: %s", row, Constants.PROBLEM_CELL));

      // check if quad had quality problems.
      if (project.getMetadata().getCustomMetadata(Integer.toString(quadHash)) != null) {
        String value = project.rows.get(row).getCell(cell).toString() + Constants.ROW_SPLITER + oldValue;
        RefineCommands.editCell(project, request, response, row, cell, value);
        LOG.info(String.format("Edit single cell at row: %s, col: %s", row, Constants.PROBLEM_CELL));
        RefineCommands.splitMultiColumn(project, request, response,
          project.columnModel.getColumnByCellIndex(cell).getName(), "Subject");
      }
    }
    project.getMetadata().setCustomMetadata(Integer.toString(quadHash), true);
  }

  private String[] getQualityProblemStrings(QualityProblem problem) {
    return new String[]{problem.getProblemName(), problem.getProblemDescription(),
      problem.getCleaningSuggestion(), problem.getGrelExpression()};
  }
}
