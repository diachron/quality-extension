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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;

public class UndefinedClassesTest {
  private static AbstractQualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();
  private static Class<?> cls;

  @BeforeClass
  public static void setUpBeforeClass() throws ClassNotFoundException {
    cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE, "UndefinedClasses"));

    Model model = ModelFactory.createDefaultModel();
    Resource rdfResource = model.createResource("http://example.org/them", FOAF.Person);
    Resource rdfResource1 = model.createResource("http://example.org/them", FOAF.Person);

    rdfResource1
        .addProperty(RDFS.domain, FOAF.Agent).addProperty(RDFS.range, FOAF.Agent)
        .addProperty(OWL.allValuesFrom, rdfResource).addProperty(OWL.oneOf, rdfResource);

    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void emptyQuads() throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();
    metric.getClass().getDeclaredMethod("before", Object[].class)
        .invoke(metric, new Object[] { new String[] {} });

    metric.compute(new ArrayList<Quad>());
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertTrue(problems.isEmpty());
    Assert.assertTrue(problems.size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }

  @Test
  public void metric() throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();
    metric.getClass().getDeclaredMethod("before", Object[].class)
    .invoke(metric, new Object[] { new String[] {} });

    metric.compute(quads);
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.isEmpty());
    Assert.assertTrue(problems.size() == 2);
    Assert.assertEquals(0.4, metric.metricValue(), 0.0);
  }
  
  
  @Test
  public void suggestion() throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    Model model = ModelFactory.createDefaultModel();

    Resource foafPersonWrong = model.createResource("http://xmlnds.com/foaf/0.1/Personaas");
    Resource foafPersonWrong1 = model.createResource("http://xmlns.com/fofaf/0.1/dpersona");
    Resource foafPersonWrong2 = model.createResource("http://example.org/foaf/0.1/Person");
    Resource foafAgentWrong = model.createResource("http://xmlns.com/fodaf/0.1/Agent");

    model.createResource("http://example.org/#1").addProperty(RDFS.subClassOf, FOAF.Person)
    .addProperty(RDFS.domain, FOAF.Agent).addProperty(RDFS.range, FOAF.Agent)
    .addProperty(OWL.allValuesFrom, foafPersonWrong);
    model.createResource("http://example.org/#2").addProperty(RDF.type, foafPersonWrong1);
    model.createResource("http://example.org/#3").addProperty(RDF.type, foafPersonWrong2);
    model.createResource("http://example.org/#4").addProperty(RDF.type, foafAgentWrong);
    model.write(System.out, "Turtle");

    List<Quad> quads = new ArrayList<Quad>();
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
    metric = (AbstractQualityMetric) cls.newInstance();
    metric.getClass().getDeclaredMethod("before", Object[].class)
    .invoke(metric, new Object[] { new String[] {} });

    metric.compute(quads);
    List<QualityProblem> problems = metric.getQualityProblems();
    for (QualityProblem pr : problems) {
      if (pr.getCleaningSuggestion().isEmpty()) {}
    }
    Assert.assertFalse(problems.isEmpty());
    Assert.assertTrue(problems.size() == 4);
    Assert.assertEquals(0.57, metric.metricValue(), 0.1);
  }

  @AfterClass
  public static void tearDown() throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    metric.getClass().getMethod("after", (Class[]) null).invoke(metric, (Object[]) null);
  }
}
