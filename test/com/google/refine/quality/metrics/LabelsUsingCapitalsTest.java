package com.google.refine.quality.metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.refine.quality.problems.QualityProblem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LabelsUsingCapitalsTest {

  private static AbstractQualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();
  private String path = System.getProperty("user.dir") + "/resources/LabelPropertiesList";

  @Before
  public void setUp() {
    File a = new File(path);
    boolean d = a.exists();
    assert d == true;

    LabelsUsingCapitals.loadAnnotationPropertiesSet(path);
    metric = new LabelsUsingCapitals();

    Model model = ModelFactory.createDefaultModel();

    model.createResource("http://example.org/#spiderman")
        .addProperty(RDFS.comment, "Name of Spiderman").addProperty(RDF.type, FOAF.Person)
        .addProperty(RDFS.label, "SpidErman")
        .addProperty(RDFS.label, "Otherlabel")
        .addProperty(RDF.type, FOAF.Person);
    model.createResource("http://example.org/#green-goblin")
        .addProperty(RDFS.comment, "Name of Green Goblin")
        .addProperty(RDFS.label, "GreenGoblin")
        .addProperty(RDFS.label, "Green");

    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void emptyQuads() {
    metric.compute(new ArrayList<Quad>());
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertTrue(problems.isEmpty());
    Assert.assertTrue(problems.size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }

  @Test
  public void metric() {
    metric.compute(quads);
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.isEmpty());
    Assert.assertTrue(problems.size() == 2);
    Assert.assertEquals(0.5, metric.metricValue(), 0.0);
  }

  @After
  public void tearDown() {
    LabelsUsingCapitals.clearAnnotationPropertiesSet();
  }
}
