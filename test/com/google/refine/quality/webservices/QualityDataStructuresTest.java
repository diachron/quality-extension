package com.google.refine.quality.webservices;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.refine.quality.cleaning.CleaningUtils;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.reports.QualityReport;
import com.google.refine.quality.reports.QualityStatistic;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.vocabularies.QPROB;
import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class QualityDataStructuresTest {

  @Test
  public void testQualityReport() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    Model model = JenaModelLoader
      .getModel("https://github.com/diachron/quality-extension/blob/cleaning/"
       + "resources/testdumps/SampleInput_EmptyAnnotationValue.ttl");

    List<String> metrics = Arrays.asList("EmptyAnnotationValue");
    List<String> problemsRes = Arrays.asList(QPROB.EmptyAnnotationValueProblem.toString());
    List<QualityProblem> problems = CleaningUtils.identifyQualityProblems(model, metrics);

    QualityReport qr = new QualityReport("reporturi");
    for (QualityProblem pr : problems) {
      qr.addQualityProblem(pr);
    }
    Assert.assertEquals("reporturi", qr.getUri());

    NodeIterator iter = qr.getQualityProblemsBag().iterator();
    while (iter.hasNext()) {
      Resource bagItem = (Resource) iter.next();
      String prob = bagItem.getProperty(RDF.type).getObject().toString();
      Assert.assertTrue(problemsRes.contains(prob));
    }
  }

  @Test
  public void testQualityStatistic() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    QualityStatistic stat = new QualityStatistic(20, 10);
    stat.incrementProblemCounter(QPROB.EmptyAnnotationValueProblem);
    stat.incrementProblemCounter(QPROB.EmptyAnnotationValueProblem);
    stat.incrementProblemCounter(QPROB.EmptyAnnotationValueProblem);

    stat.incrementProblemCounter(QPROB.LabelsUsingCapitalsProblem);
    stat.incrementProblemCounter(QPROB.LabelsUsingCapitalsProblem);

    Assert.assertEquals(20, stat.getNumberOfTriples());
    Assert.assertEquals(10, stat.getNumberOfProblems());

    NodeIterator iter = stat.getProblemsCountBag().iterator();
    while (iter.hasNext()) {
      Resource bagItem = (Resource) iter.next();
      Statement st = bagItem.getProperty(RDF.type);
      Statement st2 = bagItem.getProperty(QR.numberOfAffectedTriples);

      if (st.getObject().toString().equals(QPROB.EmptyAnnotationValueProblem.toString())) {
        Assert.assertEquals(3, st2.getObject().asLiteral().getLong());
      } else if (st.getObject().toString().equals(QPROB.LabelsUsingCapitalsProblem.toString())) {
        Assert.assertEquals(2, st2.getObject().asLiteral().getLong());
      }
    }
  }

  @Test
  public void testQualityStatAndReport() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    QualityStatistic stat = new QualityStatistic(20, 10);
    stat.incrementProblemCounter(QPROB.EmptyAnnotationValueProblem);
    stat.incrementProblemCounter(QPROB.EmptyAnnotationValueProblem);
    stat.incrementProblemCounter(QPROB.EmptyAnnotationValueProblem);

    stat.incrementProblemCounter(QPROB.LabelsUsingCapitalsProblem);
    stat.incrementProblemCounter(QPROB.LabelsUsingCapitalsProblem);

    Assert.assertEquals(20, stat.getNumberOfTriples());
    Assert.assertEquals(10, stat.getNumberOfProblems());

    Bag problemsBag = stat.getProblemsCountBag();
    NodeIterator iter = problemsBag.iterator();
    while (iter.hasNext()) {
      Resource bagItem = (Resource) iter.next();
      Statement st = bagItem.getProperty(RDF.type);
      Statement st2 = bagItem.getProperty(QR.numberOfAffectedTriples);

      if (st.getObject().toString().equals(QPROB.EmptyAnnotationValueProblem.toString())) {
        Assert.assertEquals(3, st2.getObject().asLiteral().getLong());
      } else if (st.getObject().toString().equals(QPROB.LabelsUsingCapitalsProblem.toString())) {
        Assert.assertEquals(2, st2.getObject().asLiteral().getLong());
      }
    }
    
    QualityReport qr = new QualityReport("report uri");
    qr.setQualityStatistic(stat.getQualityStatisticModel());
    // assertions
    qr.getQualityReportModel().write(System.out, "Turtle");
  }
}
