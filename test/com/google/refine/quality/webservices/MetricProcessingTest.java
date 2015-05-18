package com.google.refine.quality.webservices;

import static org.junit.Assert.fail;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import com.google.refine.quality.cleaning.CleaningUtils;
import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.VocabularyLoader;
import com.google.refine.quality.vocabularies.QPROB;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MetricProcessingTest {

  @Test
  public void ntDataReadTest() {
    // checks whether .nt can be read from url.
    String url = "http://www.w3.org/TR/REC-rdf-syntax/example07.nt";
    try {
      CleaningUtils.identifyQualityProblems(new JSONArray(), url);
    } catch (Exception e) {
      fail();
    }
    // fails, wrong url.
    String url1 = "http://www.w3.org/TR/REC-rdf-syntax/example01.nt";
    try {
      CleaningUtils.identifyQualityProblems(new JSONArray(), url1);
      fail();
    } catch (Exception e) {
    }
  }

  @Test
  public void ttlDataReadTest() {
    // checks whether .ttl can be read from url.
    String url = "https://raw.githubusercontent.com/diachron/quality-extension/"
      + "master/resources/testdumps/SampleInput_LabelsUsingCapitals.ttl";
    try {
      CleaningUtils.identifyQualityProblems(new JSONArray(), url);
    } catch (Exception e) {
      fail();
    }
    // fails, wrong url.
    String url1 = "https://raw.githubusercontent.com/diachron/quality/master/src/test/resources/"
        + "testdumps/SampleInput_LabelsUsingCapitals.ttl";
    try {
      CleaningUtils.identifyQualityProblems(new JSONArray(), url1);
      fail();
    } catch (Exception e) {
    }
  }

  @Test
  public void test() throws JSONException, MetricException, ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    String url1 = "https://raw.githubusercontent.com/diachron/quality-extension/"
      + "master/resources/testdumps/SampleInput_LabelsUsingCapitals.ttl";
    JSONArray metrics1 = new JSONArray("[LabelsUsingCapitals]");
    List<QualityProblem> problems = CleaningUtils.identifyQualityProblems(metrics1, url1);
    Assert.assertFalse(problems.isEmpty());
    Assert.assertEquals(VocabularyLoader.getResourcePropertyValue(QPROB.LabelsUsingCapitalsProblem,
        RDFS.label, Constants.QPROB_VOCAB), problems.get(0).getProblemName());

    String url2 = "https://raw.githubusercontent.com/diachron/quality-extension/"
      + "master/resources/testdumps/SampleInput_WhitespaceInAnnotation.ttl";
    JSONArray metrics2 = new JSONArray("[WhitespaceInAnnotation]");
    List<QualityProblem> problems2 = CleaningUtils.identifyQualityProblems(metrics2, url2);
    Assert.assertFalse(problems2.isEmpty());
    Assert.assertEquals(VocabularyLoader.getResourcePropertyValue(
        QPROB.WhitespaceInAnnotationProblem, RDFS.label, Constants.QPROB_VOCAB), problems2.get(0)
        .getProblemName());
  }

  @Test
  public void testVocabularyLoader() {
    Assert.assertEquals(VocabularyLoader.getResourcePropertyValue(
        QPROB.WhitespaceInAnnotationProblem, RDFS.label, Constants.QPROB_VOCAB),
        "Whitespace in Annotation");
  }
}
