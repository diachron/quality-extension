package com.google.refine.quality.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.model.Cell;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.exceptions.MetricInitializationException;
import com.google.refine.quality.metrics.AbstractQualityMetric;
import com.google.refine.quality.problems.QualityProblem;


public final class Utilities {

  /**
   * The method applies metrics to list of RDF triples fetched from URL.
   * @param metrics An array of metrics as a JSONArray object.
   * @param fileURL An URL for file with RDF data.
   * @return A list of identified quality problems.
   * @throws MetricException if handed metric can not be initialized.
   */
  public static List<QualityProblem> identifyQualityProblems(JSONArray metrics, String fileURL)
      throws MetricException {
    // TODO check the url if it exists.
    List<QualityProblem> probelms = new ArrayList<QualityProblem>();
    List<Quad> quads = JenaModelLoader.getQuads(fileURL);
    try {
      for (int i = 0; i < metrics.length(); i++) {
        String metricName = (String) metrics.get(i);
        Class<?> cls;
        cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE, metricName));
        AbstractQualityMetric metric = (AbstractQualityMetric) cls.newInstance();

        metric.before();
        metric.compute(quads);
        metric.after();
        probelms.addAll(metric.getQualityProblems());
      }

    } catch (ClassNotFoundException e) {
      throw new MetricInitializationException("A metric class is not found. "
        + e.getLocalizedMessage());
    } catch (JSONException e) {
      throw new MetricInitializationException("Could not find a metric in JSON object. "
        + e.getLocalizedMessage());
    } catch (InstantiationException e) {
      throw new MetricInitializationException("An instance of a metric class can not be initalized. "
        + e.getLocalizedMessage());
    } catch (IllegalAccessException e) {
      throw new MetricInitializationException("A metric class or its nullary constructor is not. "
        + "accessible. " + e.getLocalizedMessage());
    }
    return probelms;
  }

  /**
   * Retrieves data from project and writes it to an InputStream.
   * @param project OpenRefine project.
   * @return InputStream containing an OpenRefine project.
   * @throws IOException when project can not be written to an InputStream.
   */
  public static InputStream projectToInputStream(Project project) throws IOException {
    StringBuilder tmp = new StringBuilder();
    for (int i = 0; i < project.rows.size(); i++) {
      Cell cell = project.rows.get(i).getCell(0);
      if (cell == null) {
        tmp.append("\n");
      } else {
        tmp.append(cell.toString());
        tmp.append("\n");
      }
    }
    return IOUtils.toInputStream(tmp.toString(), "UTF-8");
  }

  /**
   * Retrieves data from project as a list of quads. Data accessed at first tree columns of the
   * OpenRefine project.
   * @param project OpenRefine project.
   * @return A list of quads.
   */
  public static List<Quad> getQuadsFromProject(Project project) {
    List<Quad> quads = new ArrayList<Quad>();
    for (int row = 0; row < project.rows.size(); row++) {
      Row rowObj = project.rows.get(row);
      if (!rowObj.isEmpty()) {
        // 0 index - stared , 1 -index flagged
        Resource subject = ResourceFactory.createResource(rowObj.getCell(2).toString());
        Property predicate = ResourceFactory.createProperty(rowObj.getCell(3).toString());

        String lit = rowObj.getCell(4).toString();
        Literal object = ResourceFactory.createPlainLiteral(lit);
        if (!object.isURIResource()) {
          object = ResourceFactory.createPlainLiteral(lit.substring(1, lit.length() - 1));
        }
        Statement statement = ResourceFactory.createStatement(subject, predicate, object);
        quads.add(new Quad(null, statement.asTriple()));
      }
    }
    return quads;
  }

  /**
   * Prints a quad to stream in readable format.
   * @param quad RDF quad.
   * @param printStream
   */
  public static void printQuad(Quad quad, PrintStream printStream) {
    if (quad != null) {
      printStream.print("Subject : " + quad.getSubject());
      printStream.print("-- Predicate : " + quad.getPredicate());
      printStream.println("--> Object : " + quad.getObject());
    }
  }

  /**
   * Prints a quad to a standard output stream in readable format.
   * @param quad RDF quad.
   */
  public static void printQuad(Quad quad) {
    printQuad(quad, System.out);
  }

  /**
   * Prints s statement in a readable format.
   * @param statement RDF statement.
   * @param printStream Stream to print.
   */
  public static void printStatement(Statement statement, PrintStream printStream) {
    if (statement != null) {
      Resource  subject   = statement.getSubject();
      Property  predicate = statement.getPredicate();
      RDFNode   object    = statement.getObject();

      printStream.print(subject.toString());
      printStream.print(String.format(" %s ", predicate.toString()));

      if (object instanceof Resource) {
        printStream.print(object.toString());
      } else {
        printStream.print(String.format(" \" %s \"", object.toString()));
      }
      printStream.println(" .");
    }
  }

  /**
   * Prints a statement to a standard output stream in a readable format.
   * @param statement RDF statement.
   */
  public static void printStatment(Statement statement) {
    printStatement(statement, System.out);
  }

  /**
   * Prints statements in readable format
   * @param stmtIterator
   * @param printStream
   */
  public static void printStatements(StmtIterator stmtIterator, PrintStream printStream){
    if (stmtIterator != null) {
      if (null == printStream) { 
        printStream = System.out;
      }
      while (stmtIterator.hasNext()){
        printStatement(stmtIterator.nextStatement(), printStream);
      }
    }
  }
}
