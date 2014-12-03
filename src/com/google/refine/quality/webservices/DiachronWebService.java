package com.google.refine.quality.webservices;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.refine.Jsonizable;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.hp.hpl.jena.rdf.model.Model;

public class DiachronWebService {
  private static final int SC_BAD_REQUEST = 400;
  private static final int SC_OK = 200;
  private static final String SERIALIZATION = "Turtle";

  /**
   * Returns a set of cleaning suggestions as a JSON entity in the http
   * response. See D3.2 section 4.2.4.
   * @throws IOException
   * @throws JSONException
   */
  public static void getCleaningSuggestions(HttpServletRequest request, HttpServletResponse response)
      throws IOException, JSONException {
    try {
      String datasetURL = getDatasetURL(request);
      List<String> metrics = getMetrics(request);
      Model model = JenaModelLoader.getModel(datasetURL);

      List<QualityProblem> problems = CleaningUtils.identifyQualityProblems(model, metrics);
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
  public static void clean(HttpServletRequest request, HttpServletResponse response)
      throws IOException, JSONException {
    try {
      String datasetURL = getDatasetURL(request);
      List<String> metrics = getMetrics(request);
      final boolean delta = getDelta(request);

      Model model = JenaModelLoader.getModel(datasetURL);
      List<QualityProblem> problems = CleaningUtils.identifyQualityProblems(model, metrics);
      CleaningUtils.cleanModel(model, problems);

      final StringWriter out = new StringWriter();
      model.write(out, SERIALIZATION);

      final Model deltaModel = CleaningUtils.getDeltaModel(model, problems);

      response.setStatus(SC_OK);
      respondJSON(response, new Jsonizable() {
        @Override
        public void write(JSONWriter writer, Properties options) throws JSONException {
          writer.object();
          writer.key("status").value("ok");
          // TODO return just url where the model located. Tmp returning serialized model.
          writer.key("uri").value(out.toString());
          if (delta) {
            StringWriter out = new StringWriter();
            deltaModel.write(out, SERIALIZATION);
            writer.key("delta").value(out.toString());
          }
          writer.endObject();
        }
      }, new Properties());
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
   * 
   * @param request
   * @return
   * @throws IllegalArgumentException
   */
  protected static boolean getDelta(HttpServletRequest request) throws IllegalArgumentException {
    String deltaParam = request.getParameter("delta");
    boolean delta = false;
    if (deltaParam == null) {
      throw new IllegalArgumentException();
    } else {
      delta = Boolean.parseBoolean(deltaParam);
    }
    return delta;
  }

  /**
   * Parses a metric parameter of the request. The string parameter has a format
   * of a json array. Example of a string ["metrics1", "metrics2"].
   * 
   * @return A list of metrics.
   * @throws IllegalArgumentException
   * @throws JSONException
   */
  protected static List<String> getMetrics(HttpServletRequest request)
      throws IllegalArgumentException, JSONException {
    String metrics = request.getParameter("metrics");
    JSONArray metricsArray = new JSONArray(metrics);
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < metricsArray.length(); i++) {
      list.add(metricsArray.getString(i));
    }
    if (metrics == null) {
      throw new IllegalArgumentException();
    }
    return list;
  }

  protected static Model generateQualityReport(String dataset, long triples,
      List<QualityProblem> qualityProblems) {
    QualityReport qualityReport = new QualityReport(dataset);
    QualityStatistic qualityStatistic = new QualityStatistic(triples, qualityProblems.size());

    for (QualityProblem problem : qualityProblems) {
      qualityReport.addQualityProblem(problem);
      qualityStatistic.incrementProblemCounter(problem.getProblemURI());
    }
    qualityReport.setQualityStatistic(qualityStatistic.getQualityStatisticModel());
    return qualityReport.getQualityReportModel();
  }

  private static void respondJSON(HttpServletResponse response, Jsonizable o, Properties options)
      throws IOException, JSONException {
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Content-Type", "application/json");
    response.setHeader("Cache-Control", "no-cache");

    Writer w = response.getWriter();
    JSONWriter writer = new JSONWriter(w);

    o.write(writer, options);
    w.flush();
    w.close();
  }

  private static void respond(HttpServletResponse response, String status, String message)
      throws IOException, JSONException {
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Content-Type", "application/json");
    response.setHeader("Cache-Control", "no-cache");

    Writer w = response.getWriter();
    JSONWriter writer = new JSONWriter(w);
    writer.object();
    writer.key("status"); writer.value(status);
    writer.key("message"); writer.value(message);
    writer.endObject();
    w.flush();
    w.close();
  }
}
