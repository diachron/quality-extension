package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;

public class MalformedDatatypeLiteralsTest {

  private static AbstractQualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();
  private static Class<?> cls;

  @BeforeClass
  public static void setUp() throws Exception {
    cls = Class.forName((String.format("%s.%s", Constants.METRICS_PACKAGE,
        "MalformedDatatypeLiterals")));
    metric = (AbstractQualityMetric) cls.newInstance();

    Model model = ModelFactory.createDefaultModel();
    model
        .createResource("http://example.org/#spiderman")
        .addLiteral(FOAF.birthday,
            ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
        .addLiteral(FOAF.givenname,
            ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
        .addLiteral(FOAF.name, ResourceFactory.createTypedLiteral("name", XSDDatatype.XSDstring))
        .addLiteral(FOAF.aimChatID,
            ResourceFactory.createTypedLiteral("3333", XSDDatatype.XSDboolean));

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
    Assert.assertTrue(problems.size() == 1);
    Assert.assertEquals(0.25, metric.metricValue(), 0.0);
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
