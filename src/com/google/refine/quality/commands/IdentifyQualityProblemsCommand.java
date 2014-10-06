package com.google.refine.quality.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
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
import com.google.refine.quality.utilities.RefineUtils;
import com.google.refine.quality.utilities.Utilities;

public class IdentifyQualityProblemsCommand extends Command {
  private static final Logger LOG = Logger.getLogger(IdentifyQualityProblemsCommand.class);

  /** A hashtable accumulates problem messages for row entries */
  private Hashtable<Integer, String> problemMessages = new Hashtable<Integer, String>();
  private HttpServletResponse response;
  private HttpServletRequest request;
  private Project project;
  private List<Quad> quads;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
      this.response = response;
      this.request = request;

      try {
        project = getProject(request);

        if (project.getMetadata().getCustomMetadata("Qaulity") == null) {
          project.getMetadata().setCustomMetadata("Qaulity", new Boolean(true));
          RefineUtils.addColumn(project, request, response, "Problems", "Object", 3);
        }

        quads = Utilities.getQuadsFromProject(project);

        JSONArray metrics = new JSONArray(request.getParameter("metrics"));
        for (int i = 0; i < metrics.length(); i++) {
          String metricName = (String) metrics.get(i);
          Class<?> cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE, metricName));
          AbstractQualityMetric metric = (AbstractQualityMetric) cls.newInstance();

          Object[] args = { new String[]{} };
          metric.getClass().getDeclaredMethod("before", Object[].class).invoke(metric, args);
          processMetric(metric, quads);
          metric.getClass().getMethod("after",  (Class[]) null).invoke(metric, (Object[]) null);

          project.getMetadata().setCustomMetadata(metricName, new Boolean(true));
        }
        
        // TODO 
        RefineUtils.splitMultiColumn(project, request, response, "Problems", "Subject");
        RefineUtils.splitColumn(project, request, response, "Problems", 4);

        RefineUtils.renameColumn(project, request, response, "Problem Type", "Problems 1");
        RefineUtils.renameColumn(project, request, response, "Problem Description", "Problems 2");
        RefineUtils.renameColumn(project, request, response, "Cleaning Suggestion", "Problems 3");
        RefineUtils.renameColumn(project, request, response, "GREL Expresion", "Problems 4");

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
      } catch (NoSuchMethodException e) {
        throw new QualityExtensionException("Can not find the invoking method. "
          + e.getLocalizedMessage());
      } catch (SecurityException e) {
        throw new QualityExtensionException(e.getLocalizedMessage());
      } catch (IllegalArgumentException e) {
        throw new QualityExtensionException("Illegal arguments. " + e.getLocalizedMessage());
      } catch (InvocationTargetException e) {
        throw new QualityExtensionException("Can not invoke the method. " + e.getLocalizedMessage());
      }
    }

  /**
   * 
   * @param qualityProblems
   */
  protected void postProblematicQuads(List<QualityProblem> qualityProblems) {
    try {
      project = getProject(request);

      for (QualityProblem qualityProblem : qualityProblems) {
        int row = quads.indexOf(qualityProblem.getQuad());
        LOG.info(qualityProblem.getProblemDescription() + "  size " + qualityProblems.size() +
          " at row "+ row);
        String problemString = composeProblemDescString(qualityProblem, row);
        
        RefineUtils.editCell(project, request, response, row, Constants.PROBLEM_CELL, problemString);
        LOG.info(String.format("Edit single cell at row: %s, col: %s", row, Constants.PROBLEM_CELL));
      }
    } catch (Exception e) {
      //TODO checked exception?
      LOG.error(e.getLocalizedMessage());
    }
  }

  /**
   * Processes retrieved quads for a given quality metric.
   * @param metric An applied metric.
   * @param quards A list of quads to process.
   */
  protected void processMetric(AbstractQualityMetric metric, List<Quad> quards) {
    LOG.info(String.format("Processing for %s", metric.getClass()));

    metric.compute(quards);
    if (metric.getQualityProblems().isEmpty()){
      LOG.info(String.format("No problem found for %s", metric.getClass()));
    } else {
      postProblematicQuads(metric.getQualityProblems());
    }
  }

  /**
   * 
   * @param problem A data quality problem detected by a metric.
   * @param row A tow in data where the problem occurred.
   * @return String object containing description of a problem.
   */
  private String composeProblemDescString(QualityProblem problem, int row) {
    StringBuffer string = new StringBuffer();

    string.append(problem.getProblemName());
    string.append(Constants.COLUMN_SPLITER);
    string.append(problem.getProblemDescription());
    string.append(Constants.COLUMN_SPLITER);
    string.append(problem.getCleaningSuggestion());
    string.append(Constants.COLUMN_SPLITER);
    string.append(problem.getGrelExpression());

    if (problemMessages.containsKey(row)) {
      if (!problemMessages.get(row).contains(string.toString())) {
        string.insert(0, problemMessages.get(row) + Constants.ROW_SPLITER);
        problemMessages.put(row, string.toString());
      }
    } else {
      problemMessages.put(row, string.toString());
    }
    return string.toString();
  }
}
