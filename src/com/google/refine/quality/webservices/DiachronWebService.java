package com.google.refine.quality.webservices;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.vocabularies.QPROB;
import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DiachronWebService {
  private static final int SC_BAD_REQUEST = 400;
  private static final int SC_OK = 200;
  private static final String SERIALIZATION = "Turtle";

  /**
   * Returns a set of cleaning suggestions as a JSON entity in the http response.
   * See D3.2 section 4.2.4.
   * @throws IOException
   * @throws JSONException
   */
  public static void getCleaningSuggestions(HttpServletRequest request, HttpServletResponse response)
    throws IOException, JSONException {
    try {
      String datasetURL = getDatasetURL(request);
      List<String> metrics = getMetrics(request);
      Model model = JenaModelLoader.getModel(datasetURL);

      List<QualityProblem> problems = MetricProcessing.identifyQualityProblems(model, metrics);
      StringWriter out = new StringWriter();
      generateQualityReport(datasetURL, model.size(), problems).write(out, SERIALIZATION);

      response.setStatus(SC_OK);
      respond(response, "ok", out.toString());
    } catch (IllegalArgumentException e) {
      response.setStatus(SC_BAD_REQUEST);
      respond(response, "error", "Request parameters are not complete.");
    } catch (Exception e) {
      response.setStatus(SC_BAD_REQUEST);
      respond(response, "error",
          "Request parameters cannot be parsed or an error in applying metrics");
    }
  }

  /**
   * Cleans a dataset. See D3.2 section 4.2.4.
   * 
   * @param request
   * @param response
   * @throws IOException
   * @throws JSONException
   */
  public static void clean(HttpServletRequest request, HttpServletResponse response) throws IOException,
    JSONException {
    // TODO
    respond(response, "error","Not Implemented");
  }

  /**
   * 
   * @param request
   * @return
   * @throws IllegalArgumentException
   */
  protected static String getDatasetURL(HttpServletRequest request) throws IllegalArgumentException {
    String dataset = request.getParameter("download");
    if (dataset == null) {
      throw new IllegalArgumentException();
    }
    return dataset;
  }

  /**
   * Parses a metric parameter of the request. The string parameter has a format of a json array.
   * Example of a string ["metrics1", "metrics2"].
   * @return A list of metrics.
   * @throws IllegalArgumentException
   * @throws JSONException 
   */
  protected static List<String> getMetrics(HttpServletRequest request) throws IllegalArgumentException, JSONException {
    String metrics = request.getParameter("metrics");
    // TODO parse response. ....
    JSONArray metricsArray = new JSONArray(metrics);
    List<String> list = new ArrayList<String>();
    for (int i = 0; i< metricsArray.length(); i++) {
      list.add(metricsArray.getString(i));
    }
    if (metrics == null) {
      throw new IllegalArgumentException();
    }
    return list;
  }

  // TODO the quality report does not correspond to the one in the D3.2
  protected static Model generateQualityReport(String dataset, long totalTriples,
      List<QualityProblem> qualityProblems) {
    Hashtable<Resource, Integer> problemCount = new Hashtable<Resource, Integer>();

    Model model = ModelFactory.createDefaultModel();
    Resource report = model.createResource();
    report.addProperty(QR.computedOn, model.createResource(dataset));

    Resource qualityStats = model.createResource();
    qualityStats.addProperty(QR.totalNumberOfTriples, model.createTypedLiteral(totalTriples));
    qualityStats.addProperty(QR.numberOfProblems, model.createTypedLiteral(qualityProblems.size()));

    for (QualityProblem qualityProblem : qualityProblems) {
      Resource qprob = model.createResource(qualityProblem.getProblemURI());
      qprob.addProperty(RDFS.label, qualityProblem.getProblemName());
      qprob.addProperty(QPROB.problemDescription, qualityProblem.getProblemDescription());
      qprob.addProperty(QPROB.cleaningSuggestion, qualityProblem.getCleaningSuggestion());
      qprob.addProperty(QPROB.qrefineRule, qualityProblem.getCleaningSuggestion());

      if (!problemCount.containsKey(qualityProblem.getProblemURI())) {
        problemCount.put(qualityProblem.getProblemURI(), 0);
      }
      int currentCount = problemCount.get(qualityProblem.getProblemURI()).intValue();
      problemCount.put(qualityProblem.getProblemURI(), currentCount + 1);
    }

    updateStats(model, qualityStats, problemCount);
    return model;
  }

  private static void updateStats(Model model, Resource stats,
      Hashtable<Resource, Integer> problemCount) {
    Set<Entry<Resource, Integer>> entrySet = problemCount.entrySet();
    Iterator<Entry<Resource, Integer>> iterator = entrySet.iterator();

    while (iterator.hasNext()) {
      Entry<Resource, Integer> entry = iterator.next();
      Resource qProb = entry.getKey();
      qProb.addProperty(QR.numberOfAffectedTriples,
        model.createTypedLiteral(entry.getValue().intValue()));

      Resource triple = model.createResource(generateURI()).addProperty(RDF.type, RDF.Statement)
        .addProperty(RDF.subject, qProb.getProperty(QR.numberOfAffectedTriples).getSubject())
        .addProperty(RDF.predicate, model.createResource(QR.numberOfAffectedTriples))
        .addProperty(RDF.object, qProb.getProperty(QR.numberOfAffectedTriples).getObject());
      stats.addProperty(QR.problem, triple);
    }
  }

  private static void respond(HttpServletResponse response, String status, String message)
      throws IOException, JSONException {
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Content-Type", "application/json");
    response.setHeader("Cache-Control", "no-cache");

    Writer w = response.getWriter();
    JSONWriter writer = new JSONWriter(w);
    writer.object();
    writer.key("status");
    writer.value(status);
    writer.key("message");
    writer.value(message);
    writer.endObject();
    w.flush();
    w.close();
  }

  private static Resource generateURI() {
    String uri = "urn:" + UUID.randomUUID().toString();
    Resource r = ModelFactory.createDefaultModel().createResource(uri);
    return r;
  }
}
