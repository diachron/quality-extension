package com.google.refine.quality.webservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.riot.RiotException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DiachronWebServiceTest {

  @Test
  public void getCleaningSuggestionsTest() throws IOException, JSONException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com"
      + "/diachron/quality/master/src/test/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
    Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\", \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestions(req, res);

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

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusssssssssercontent.com"
        + "/diachron/quality/master/src/test/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
      Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\", \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestions(req, res);

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

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com"
        + "/diachron/quality/master/src/test/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
      Mockito.when(req.getParameter("metrics")).thenReturn("\"LabelsUsingCapitals\",EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestions(req, res);

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
    Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\", \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestions(req, res);

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

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com"
        + "/diachron/quality/master/src/test/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
    Mockito.when(req.getParameter("metrics")).thenReturn(null);


    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.getCleaningSuggestions(req, res);

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

    Mockito.when(req.getParameter("download")).thenReturn("https://raw.githubusercontent.com"
      + "/diachron/quality/master/src/test/resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");
    Mockito.when(req.getParameter("metrics")).thenReturn("[\"LabelsUsingCapitals\", \"EmptyAnnotationValue\"]");

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    Mockito.when(res.getWriter()).thenReturn(new PrintWriter(responseStream));

    DiachronWebService.clean(req, res);

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
