package com.google.refine.quality.utilities;


import static org.junit.Assert.fail;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.vocabulary.RDFS;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.vocabularies.QPROB;

public class UtilitiesTest {

  @Test
  public void ntDataReadTest() {
    // checks whether .nt can be read from url.
    String url = "http://www.w3.org/TR/REC-rdf-syntax/example07.nt";
    try {
      Utilities.identifyQualityProblems(new JSONArray(), url);
    } catch (Exception e) {
      fail();
    }
    // fails, wrong url.
    String url1 = "http://www.w3.org/TR/REC-rdf-syntax/example01.nt";
    try {
      Utilities.identifyQualityProblems(new JSONArray(), url1);
      fail();
    } catch (Exception e) {
    }
  }

  @Test
  public void ttlDataReadTest() {
    // checks whether .ttl can be read from url.
    String url = "https://raw.githubusercontent.com/diachron/quality/master/src/test/resources/"
        + "testdumps/SampleInput_LabelsUsingCapitals.ttl";
    try {
      Utilities.identifyQualityProblems(new JSONArray(), url);
    } catch (Exception e) {
      fail();
    }
    // fails, wrong url.
    String url1 = "https://raw.githubusercontent.com/diachron/quality/master/src/test/resources/"
        + "testdumps/SSampleInput_LabelsUsingCapitals.ttl";
    try {
      Utilities.identifyQualityProblems(new JSONArray(), url1);
      fail();
    } catch (Exception e) {
    }
  }

  // T
  @Test
  public void test() throws JSONException {
    String url1 = "https://raw.githubusercontent.com/diachron/quality/master/src/test/resources/"
        + "testdumps/SampleInput_LabelsUsingCapitals.ttl";
    JSONArray metrics1 = new JSONArray("[LabelsUsingCapitals]");
    List<QualityProblem> problems = Utilities.identifyQualityProblems(metrics1, url1);
    Assert.assertFalse(problems.isEmpty());
    Assert.assertEquals(QualityReportModelLoader.getResourcePropertyValue(
        QPROB.LabelsUsingCapitalsProblem, RDFS.label), problems.get(0).getProblemName());

    String url2 = "https://raw.githubusercontent.com/diachron/quality/master/src/test/resources/"
        + "testdumps/SampleInput_WhitespaceInAnnotation.ttl";
    JSONArray metrics2 = new JSONArray("[WhitespaceInAnnotation]");
    List<QualityProblem> problems2 = Utilities.identifyQualityProblems(metrics2, url2);
    Assert.assertFalse(problems2.isEmpty());
    Assert.assertEquals(QualityReportModelLoader.getResourcePropertyValue(
        QPROB.WhitespaceInAnnotationProblem, RDFS.label), problems2.get(0).getProblemName());
  }
}
