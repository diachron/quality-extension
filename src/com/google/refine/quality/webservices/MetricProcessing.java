package com.google.refine.quality.webservices;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.metrics.AbstractQualityMetric;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.JenaModelLoader;

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
      throws MetricException, JSONException, ClassNotFoundException, InstantiationException, IllegalAccessException {

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

}
