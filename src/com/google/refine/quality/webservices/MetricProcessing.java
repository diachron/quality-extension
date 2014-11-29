package com.google.refine.quality.webservices;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.metrics.AbstractQualityMetric;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;

public class MetricProcessing {

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
  public static List<QualityProblem> identifyQualityProblems(JSONArray metrics, String fileURL)
      throws MetricException, JSONException, ClassNotFoundException, InstantiationException,
      IllegalAccessException {

    // check if the file exists and than load model from the url
    // other approach is to get the file from and pass it as an input stream to
    // the getQuads of the JenaLoader class.
    // Add new private class method for it within this class.

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
   * The method applies metrics to list of RDF triples fetched from URL.
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
}
