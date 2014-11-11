package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MisplacedClassesOrPropertiesTest {

  private static AbstractQualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();
  private static Class<?> cls;

  @BeforeClass
  public static void setUpBeforeClass() throws ClassNotFoundException {
    cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE,
        "MisplacedClassesOrProperties"));

    Model model = ModelFactory.createDefaultModel();
    Property pr2 = model.createProperty(FOAF.Person.toString());

    model.createResource(RDFS.subPropertyOf.toString()).addProperty(RDF.value,
      "http://example.org/#prop1");
    model.createResource("http://example.org/#prop3").addProperty(pr2,
      "http://example.org/#prop1");

    Resource prop3 = model.createResource("http://example.org/#prop3");
    Property pr3 = model.createProperty("http://example.org/#prop3");
    prop3.addProperty(pr3, RDFS.subPropertyOf);

    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void test() throws InstantiationException, IllegalAccessException {
    metric = (AbstractQualityMetric) cls.newInstance();

    metric.compute(quads);
    metric.metricValue();
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.isEmpty());
    Assert.assertTrue(problems.size() == 3);
    Assert.assertEquals(0.42, metric.metricValue(), 0.01);
  }

  @Test
  public void emptyQuads() throws InstantiationException, IllegalAccessException {
    metric = (AbstractQualityMetric) cls.newInstance();

    metric.compute(new ArrayList<Quad>());
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertTrue(problems.isEmpty());
    Assert.assertTrue(problems.size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }

}
