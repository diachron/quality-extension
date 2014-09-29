package com.google.refine.quality.metrics;

import java.lang.reflect.InvocationTargetException;
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
  private static Class<?> cls;

  @Before
  public void setUp() throws Exception {
    // creating an instance using reflection, the same way instances created after receiving the
    // metric's name to apply to quads.
    cls = Class.forName("com.google.refine.quality.metrics." + "LabelsUsingCapitals");
    metric = (AbstractQualityMetric) cls.newInstance();
    metric.getClass().getDeclaredMethod("before", Object[].class).invoke(metric, new Object[]{new String[]{}});
    metric.getClass().getMethod("after",  (Class[]) null).invoke(metric, (Object[]) null);

    Model model = ModelFactory.createDefaultModel();

    model.createResource("http://example.org/#spiderman")
        .addProperty(RDFS.comment, "Name of Spiderman").addProperty(RDF.type, FOAF.Person)
        .addProperty(RDFS.label, "SpidErman").addProperty(RDFS.label, "Otherlabel")
        .addProperty(RDF.type, FOAF.Person);
    model.createResource("http://example.org/#green-goblin")
        .addProperty(RDFS.comment, "Name of Green Goblin").addProperty(RDFS.label, "GreenGoblin")
        .addProperty(RDFS.label, "Green");

    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void emptyQuads() throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, NoSuchMethodException, SecurityException {
    metric.getClass().getDeclaredMethod("before", Object[].class).invoke(metric, new Object[]{new String[]{}});

    metric.compute(new ArrayList<Quad>());
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertTrue(problems.isEmpty());
    Assert.assertTrue(problems.size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);

    metric.getClass().getMethod("after",  (Class[]) null).invoke(metric, (Object[]) null);
  }

  @Test
  public void metric() throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, NoSuchMethodException, SecurityException {
    metric.getClass().getDeclaredMethod("before", Object[].class).invoke(metric, new Object[]{new String[]{}});

    metric.compute(quads);
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.isEmpty());
    Assert.assertTrue(problems.size() == 2);
    Assert.assertEquals(0.5, metric.metricValue(), 0.0);

    metric.getClass().getMethod("after",  (Class[]) null).invoke(metric, (Object[]) null);
  }

  @After
  public void tearDown() throws IllegalAccessException, IllegalArgumentException,
  InvocationTargetException, NoSuchMethodException, SecurityException {
    metric.getClass().getMethod("after", (Class[]) null).invoke(metric, (Object[]) null);
  }
}
