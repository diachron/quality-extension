package com.google.refine.quality.webservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.riot.RiotException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.refine.quality.cleaning.CleaningUtils;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.utilities.Utilities;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

public class DiachronWebServiceTest {

  @Test
  public void cleanDatasetTest() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException, IOException, JSONException {
    List<String> metrics = Arrays.asList("EmptyAnnotationValue");
    Model model = JenaModelLoader.getModel("https://raw.githubusercontent.com/diachron/"
        + "quality-extension/master/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");

    List<QualityProblem> problems = CleaningUtils.identifyQualityProblems(model, metrics);
    Model delta = ModelFactory.createDefaultModel();
    for (QualityProblem problem : problems) {
      Quad quad = problem.getQuad();
      Statement stat = Utilities.createStatement(quad.getSubject().toString(), quad.getPredicate()
        .toString(), quad.getObject().toString());
      delta.add(stat);
    }
    
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com/diachron"
      + "/quality-extension/master/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
    Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\","
      + " \"EmptyAnnotationValue\"]");
    Mockito.when(req.getParameter("delta")).thenReturn("true");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.cleanREST(req, res);

    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
    String status = (String) json.get("status");
    String uri = (String) json.get("uri");
    String message = (String) json.get("delta");

    Model model1 = ModelFactory.createDefaultModel();
    InputStream is = new ByteArrayInputStream(message.getBytes());
    model1.read(is, null, "Turtle");

    Assert.assertEquals("ok", status);
    StmtIterator iter = model1.listStatements();
    while (iter.hasNext()) {
      Assert.assertTrue(delta.contains(iter.next()));
    }
    // assert for uris
    Assert.assertNotNull(uri);
  }

  @Test
  public void getCleaningSuggestionsTest() throws IOException, JSONException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com/diachron/"
      + "quality-extension/master/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
    Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\","
      + " \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestionsREST(req, res);

    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
    String status = (String) json.get("status");
    String message = (String) json.get("message");

    Model model = ModelFactory.createDefaultModel();
    InputStream is = new ByteArrayInputStream( message.getBytes());
    model.read(is, null, "Turtle");
    model.write(System.out, "Turtle");
    Assert.assertEquals("ok", status);
    Assert.assertFalse(model.isEmpty());
    // assert number of problems and other things 
    // asserts for the quality report. 
    // added when the structure of the vocabularies is clear.
    // TODO
  }

  @Test(expected=RiotException.class)
  public void getCleaningSuggestionsWrongDatasetTest() throws IOException, JSONException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com"
      + "/diachron/quality-extension/master/resources/testdumps/SampleIsnput_EmptyAnnotationValue.ttl");
      Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\","
        + " \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestionsREST(req, res);

    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
    String status = (String) json.get("status");
    String message = (String) json.get("message");
    Assert.assertEquals("error", status);

    Model model = ModelFactory.createDefaultModel();
    InputStream is = new ByteArrayInputStream( message.getBytes() );
    model.read(is, null, "Turtle");
  }

  @Test(expected=RiotException.class)
  public void getCleaningSuggestionsWrongtMetricParamtTest() throws IOException, JSONException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com/diachron/"
      + "quality-extension/master/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
      Mockito.when(req.getParameter("metrics")).thenReturn("\"LabelsUsingCapitals\","
        + "EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestionsREST(req, res);

    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
    String status = (String) json.get("status");
    String message = (String) json.get("message");
    Assert.assertEquals("error", status);

    Model model = ModelFactory.createDefaultModel();
    InputStream is = new ByteArrayInputStream( message.getBytes() );
    model.read(is, null, "Turtle");
  }

  @Test(expected=RiotException.class)
  public void getCleaningSuggestionsNullDatasetParamtTest() throws IOException, JSONException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn(null);
    Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\","
      + " \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestionsREST(req, res);

    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
    String status = (String) json.get("status");
    String message = (String) json.get("message");
    Assert.assertEquals("error", status);

    Model model = ModelFactory.createDefaultModel();
    InputStream is = new ByteArrayInputStream( message.getBytes() );
    model.read(is, null, "Turtle");
  }

  @Test(expected=RiotException.class)
  public void getCleaningSuggestionsNullMetricParamTest() throws IOException, JSONException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com/"
      + "diachron/quality-extension/master/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
    Mockito.when(req.getParameter("metrics")).thenReturn(null);


    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestionsREST(req, res);

    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
    String status = (String) json.get("status");
    String message = (String) json.get("message");
    Assert.assertEquals("error", status);

    Model model = ModelFactory.createDefaultModel();
    InputStream is = new ByteArrayInputStream( message.getBytes() );
    model.read(is, null, "Turtle");
  }

//  @Test
  public void testClean() throws IOException, JSONException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com/diachron/"
      + "quality-extension/master/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
    Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\","
      + " \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.cleanREST(req, res);

//    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
//    String status = (String) json.get("status");
//    String message = (String) json.get("message");
//
//    Model model = ModelFactory.createDefaultModel();
//    InputStream is = new ByteArrayInputStream( message.getBytes() );
//    model.read(is, null, "Turtle");

//    Assert.assertEquals("ok", status);
//    Assert.assertFalse(model.isEmpty());
    // assert number of problems and other things 
    // asserts for the quality report. 
    // added when the structure of the vocabularies is clear.
    // TODO
  }
}
