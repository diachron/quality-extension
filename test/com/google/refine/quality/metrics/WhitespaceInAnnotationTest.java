package com.google.refine.quality.metrics;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;

public class WhitespaceInAnnotationTest {

  private static AbstractQualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();
  private static Class<?> cls;

  @BeforeClass
  public static void setUp() throws Exception {
    cls = Class.forName((String.format("%s.%s", Constants.METRICS_PACKAGE, "WhitespaceInAnnotation")));

    Model model = ModelFactory.createDefaultModel();
    model.createResource("http://example.org/#spiderman")
        .addProperty(RDFS.comment, "Name of Spiderman").addProperty(RDF.type, FOAF.Person)
        .addProperty(RDFS.label, "SpidErman").addProperty(RDFS.label, "Otherlabel  ")
        .addProperty(RDF.type, FOAF.Person);
    model.createResource("http://example.org/#green-goblin")
        .addProperty(RDFS.comment, "Name of Green Goblin").addProperty(RDFS.label, "  GreenGoblin")
        .addProperty(RDFS.label, "Green ");

    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void emptyQuads() throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();
    metric.getClass().getDeclaredMethod("before", Object[].class).invoke(metric, new Object[]{new String[]{}});

    metric.compute(new ArrayList<Quad>());
    List<QualityProblem> problems = metric.getQualityProblems();
    System.out.println(problems.size());
    Assert.assertTrue(problems.isEmpty());
    Assert.assertTrue(problems.size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }

  @Test
  public void metric() throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();
    metric.getClass().getDeclaredMethod("before", Object[].class).invoke(metric, new Object[]{new String[]{}});

    metric.compute(quads);
    List<QualityProblem> problems = metric.getQualityProblems();
    System.out.println(problems.size());;
    Assert.assertFalse(problems.isEmpty());
    Assert.assertTrue(problems.size() == 3);
    Assert.assertEquals(0.5, metric.metricValue(), 0.0);
  }

  @Test
  public void annotationNotInFile() throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();

    Model m = ModelFactory.createDefaultModel();
    m.createResource("http://example.org/#spiderman")
        .addProperty(RDFS.comment, "Name of Spiderman").addProperty(RDF.type, FOAF.Person)
        .addProperty(RDFS.seeAlso, "SpidErman");
    metric.getClass().getDeclaredMethod("before", Object[].class).invoke(metric, new Object[]{new String[]{}});
  
    ArrayList<Quad> quards = new ArrayList<Quad>();
    StmtIterator s = m.listStatements();
    while (s.hasNext()) {
      quards.add(new Quad(null, s.next().asTriple()));
    }
    metric.compute(quards);
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertTrue(problems.isEmpty());
    System.out.println(problems.size());;
    Assert.assertTrue(problems.size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }
  
  @AfterClass
  public static void tearDown() throws IllegalAccessException, IllegalArgumentException,
  InvocationTargetException, NoSuchMethodException, SecurityException {
    metric.getClass().getMethod("after", (Class[]) null).invoke(metric, (Object[]) null);
  }
}
