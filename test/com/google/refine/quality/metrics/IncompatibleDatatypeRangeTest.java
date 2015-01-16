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

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;

public class IncompatibleDatatypeRangeTest {
  private static AbstractQualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();
  private static Class<?> cls;

  @BeforeClass
  public static void setUp() throws Exception {
    cls = Class.forName((String.format("%s.%s", Constants.METRICS_PACKAGE,
        "IncompatibleDatatypeRange")));
    metric = (AbstractQualityMetric) cls.newInstance();

    Model model = ModelFactory.createDefaultModel();
    // TODO temporal resource source.
    model.read("https://raw.githubusercontent.com/diachron/quality/master/src/test/resources/"
        + "testdumps/chembl-rdf-void_2.ttl");
    // model.createResource("http://example.org/#obj1")
    // // .addLiteral(FOAF.knows,
    // ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
    // .addLiteral(RDFS.subPropertyOf,
    // ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
    // .addLiteral(RDFS.subPropertyOf,
    // ResourceFactory.createProperty("http://example.org/#property"))
    // .addLiteral(FOAF.birthday,
    // ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
    // .addLiteral(FOAF.birthday,
    // ResourceFactory.createTypedLiteral("2012-03-10", XSDDatatype.XSDstring));
    //
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

   @Test
  public void emptyQuads() throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();

    metric.compute(new ArrayList<Quad>());
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertTrue(problems.isEmpty());
    Assert.assertTrue(problems.size() == 0);
    Assert.assertEquals(0.0, metric.metricValue(), 0.0);
  }

//  @Test
  public void metric() throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();

    metric.compute(quads);
    List<QualityProblem> problems = metric.getQualityProblems();
    Assert.assertFalse(problems.isEmpty());
    Assert.assertTrue(problems.size() == 4);
    Assert.assertEquals(0.15, metric.metricValue(), 0.01);
  }

  @AfterClass
  public static void tearDown() throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
  }

}
