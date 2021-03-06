package com.google.refine.quality.cleaning;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.metrics.AbstractQualityMetric;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.utilities.Utilities;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

public class CleaningUtils {

  /**
   * The method applies metrics to list of RDF triples fetched from URL.
   * @param metrics An array of metrics as a JSONArray object.
   * @param fileURL An URL for file with RDF data.
   * @return A list of identified quality problems.
   * @throws MetricException if handed metric can not be initialized.
   * @throws JSONException 
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  @Deprecated
  public static List<QualityProblem> identifyQualityProblems(JSONArray metrics, String fileURL) 
      throws ClassNotFoundException, JSONException, InstantiationException, IllegalAccessException {
    List<QualityProblem> probelms = new ArrayList<QualityProblem>();
    List<Quad> quads = JenaModelLoader.getQuads(fileURL);
    for (int i = 0; i < metrics.length(); i++) {
      String metricName = (String) metrics.get(i);
      Class<?> cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE, metricName));
      AbstractQualityMetric metric = (AbstractQualityMetric) cls.newInstance();

      metric.before();
      metric.compute(quads);
      metric.after();
      probelms.addAll(metric.getQualityProblems());
    }
    return probelms;
  }

  /**
   * The method applies metrics to a RDF jena model.
   * @param metrics An array list of metrics.
   * @param model A RDF data model.
   * @return A list of identified quality problems.
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */ 
  public static List<QualityProblem> identifyQualityProblems(Model model, List<String> metrics)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    List<QualityProblem> probelms = new ArrayList<QualityProblem>();
    List<Quad> quads = JenaModelLoader.getQuads(model);

    for (String metricName : metrics) {
      Class<?> cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE, metricName));
      AbstractQualityMetric metric = (AbstractQualityMetric) cls.newInstance();
      metric.before();
      metric.compute(quads);
      metric.after();

      probelms.addAll(metric.getQualityProblems());
    }
    return probelms;
  }

  /**
   * Creates a RDF Jena model containing statements with quality problems.
   * @param model A RDF model.
   * @param problems A list of quality problems.
   * @return A RDF jena model with problematic triples.
   */
  public static Model getDeltaModel(Model model, List<QualityProblem> problems) {
    Model deltaModel = ModelFactory.createDefaultModel();
    for (QualityProblem problem : problems) {
      Quad quad = problem.getQuad();
      Statement stat = Utilities.createStatement(quad.getSubject().toString(), quad.getPredicate()
          .toString(), quad.getObject().toString());
      deltaModel.add(stat);
    }
    return deltaModel;
  }

  /**
   * Cleans a RDF jena model from problematic statements.
   * @param model A RDF jena model.
   * @param problems A list of quality problems.
   * @return A cleaned RDF jena model.
   */
  public static Model cleanModel(Model model, List<QualityProblem> problems) {
    for (QualityProblem problem : problems) {
      Quad quad = problem.getQuad();
      Statement stat = Utilities.createStatement(quad.getSubject().toString(), quad.getPredicate()
        .toString(), quad.getObject().toString());
      model.remove(stat);
    }
    return model;
  }
}
