package com.google.refine.quality.metrics;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.google.refine.quality.utilities.Constants;

public class HomogeneousDatatypesTest {

  private static AbstractQualityMetric metric;
  private static List<Quad> quads = new ArrayList<Quad>();
  private static Class<?> cls;

  @BeforeClass
  public static void setUp() throws Exception {
    cls = Class.forName((String.format("%s.%s", Constants.METRICS_PACKAGE, "HomogeneousDatatypes")));

    Model model = ModelFactory.createDefaultModel();

    model.createResource("http://example.org/#spiderman")
      .addLiteral(FOAF.birthday, ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDdate))
      .addLiteral(FOAF.birthday, ResourceFactory.createTypedLiteral("2012-03-11", XSDDatatype.XSDstring))
        .addLiteral(FOAF.birthday, ResourceFactory.createTypedLiteral("2012-03-10", XSDDatatype.XSDstring))

        .addProperty(RDF.type, FOAF.Person).addProperty(RDFS.label, "Otherlabelddd");
    model.createResource("http://example.org/#green-goblin")
        .addProperty(RDFS.comment, "Name of Green Goblin").addProperty(RDFS.label, "GreenGoblin")
        .addProperty(RDFS.label, "Green");

    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void metric() throws IllegalArgumentException, SecurityException, IllegalAccessException,
    InvocationTargetException, NoSuchMethodException, InstantiationException {
    metric = (AbstractQualityMetric) cls.newInstance();

    metric.compute(quads);
//    List<QualityProblem> problems = metric.getQualityProblems();
//    Assert.assertFalse(problems.isEmpty());
//    Assert.assertTrue(problems.size() == 2);
//    Assert.assertEquals(0.5, metric.metricValue(), 0.0);
  }

}
