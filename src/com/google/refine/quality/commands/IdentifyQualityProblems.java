package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONWriter;

import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.commands.Command;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.Project;
import com.google.refine.quality.commands.TransformData.EditOneCellProcess;
import com.google.refine.quality.exceptions.QualityExtensionException;
import com.google.refine.quality.metrics.AbstractQualityMetric;
import com.google.refine.quality.metrics.LabelsUsingCapitals;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.utilities.Utilities;
import com.google.refine.util.Pool;

public class IdentifyQualityProblems extends Command {
  private static final Logger LOG = Logger.getLogger(IdentifyQualityProblems.class);

  private static final String COLUMN_SPLITER = "|&SPLITCOLUMN&|";
  private static final String ROW_SPLITER = "|&SPLITROW&|";

  /** A hashtable accumulates problem messages for row entries */
  private Hashtable<Integer, String> problemMessages = new Hashtable<Integer, String>();
  private HttpServletResponse response;
  private Project project;

  /**
   * 
   * @param qualityProblems
   */
  protected void postProblematicQuads(List<QualityProblem> qualityProblems) {
    try {
      HistoryEntry historyEntry = null;
      EditOneCellProcess process = null;

      int cell = 1;
      for (QualityProblem qualityProblem : qualityProblems) {
        int row = qualityProblem.getRowIndex();
        String problemString = composeProblemDescString(qualityProblem, row);

        process = new EditOneCellProcess(project, "Edit single cell", row, cell, problemString);
        historyEntry = project.processManager.queueProcess(process);
      }
      updateCell(process, historyEntry);
    } catch (Exception e) {
      //TODO checked exception?
      LOG.error(e.getLocalizedMessage());
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
    string.append(COLUMN_SPLITER);
    string.append(problem.getProblemDescription());
    string.append(COLUMN_SPLITER);
    string.append(problem.getCleaningSuggestion());
    string.append(COLUMN_SPLITER);
    string.append(problem.getGrelExpression());

    if (problemMessages.containsKey(row)) {
      if (!problemMessages.get(row).contains(string.toString())) {
        string.insert(0, problemMessages.get(row) + ROW_SPLITER);
        problemMessages.put(row, string.toString());
      }
    } else {
      problemMessages.put(row, string.toString());
    }

    return string.toString();
  }

  /**
   * Updates an entry in OpenRefine with a problem information.
   * @param process
   * @param entry
   */
  private void updateCell(EditOneCellProcess process, HistoryEntry entry) {
    try {
      if (entry != null) {
        JSONWriter writer = new JSONWriter(response.getWriter());
        Pool pool = new Pool();
        Properties options = new Properties();
        options.put("pool", pool);

        writer.object();
        writer.key("code");
        writer.value("ok");
        writer.key("historyEntry");
        entry.write(writer, options);
        writer.key("cell");
        process.newCell.write(writer, options);
        writer.key("pool");
        pool.write(writer, options);
        writer.endObject();
      } else {
        respond(response, "{ \"code\" : \"pending\" }");
      }
    } catch (JSONException e) {
      LOG.error(e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage());
    } catch (ServletException e) {
      LOG.error(e.getLocalizedMessage());
    }

  }

  /**
   * Processes retrieved quards for a given quality metric.
   * @param metric An applied metric.
   * @param quards A list of quards to process.
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

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
      this.response = response;
      List<Quad> quards = null;

      try {
        project = getProject(request);
        quards = JenaModelLoader.getQuads(Utilities.projectToInputStream(project));

        //Utilities.printStatements(LoadJenaModel.getModel(retrieveRDFData(project)).listStatements(), System.out);
      } catch (IOException e) {
        LOG.error(e.getLocalizedMessage());
        throw new QualityExtensionException("Project can not be written to an InputStream. "
          + e.getLocalizedMessage());
      } catch (ServletException e) {
        LOG.error(e.getLocalizedMessage());
        throw new QualityExtensionException("Can not get a project from OpenRefine. "
          + e.getLocalizedMessage());
      }

      // for Hello World Meric -- DEBUG ONLY
      ////processMetric(request, response, new HelloWorldMetrics(), quards);

      // for Empty Annotation value
      //            EmptyAnnotationValue.loadAnnotationPropertiesSet(null); // Pre-Process
      //            processMetric(request, response, new EmptyAnnotationValue(), quards);
      //            EmptyAnnotationValue.clearAnnotationPropertiesSet(); //Post-Process
      //
      //            //TODO homogeneousDatatypes
      //            
      //            // for IncompatiableDatatypeRange
      //            processMetric(request, response, new IncompatibleDatatypeRange(), quards);
      //            IncompatibleDatatypeRange.clearCache(); //Post-Process

      // for Malformed Datatype Literals
      //  processMetric(request, response, new MalformedDatatypeLiterals(), quards);


      // for MisplacedClassesOrProperties -- DISABLE B/C TAKES TOO MUCH TIME
      //  processMetric(request, response, new MisplacedClassesOrProperties(), quards);

      // for MisusedOwlDatatypeOrObjectProperties
      //  MisusedOwlDatatypeOrObjectProperties.filterAllOwlProperties(listQuad); //Pre-Process
      //   processMetric(request, response, new MisusedOwlDatatypeOrObjectProperties(), listQuad);
      //   MisusedOwlDatatypeOrObjectProperties.clearAllOwlPropertiesList(); //Post-Process

      // for OntologyHijacking
      // processMetric(request, response, new OntologyHijacking(), quards);

      // for WhitespaceInAnnotation
      //            WhitespaceInAnnotation.loadAnnotationPropertiesSet(null); //Pre-Process
      //            processMetric(request, response, new WhitespaceInAnnotation(), quards);
      //            WhitespaceInAnnotation.clearAnnotationPropertiesSet(); //Post-Process

      // for LabelUsingCapitals
      LabelsUsingCapitals.loadAnnotationPropertiesSet(null); //Pre-Process
      processMetric(new LabelsUsingCapitals(), quards);
      LabelsUsingCapitals.clearAnnotationPropertiesSet();

      // for Undefined Classes
      //  processMetric(request, response, new UndefinedClasses(), quards);

      // for Undefined Properties
      //    processMetric(request, response, new UndefinedProperties(), quards);
    }
}
