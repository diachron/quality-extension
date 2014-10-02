package com.google.refine.quality.problems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.google.refine.quality.vocabularies.QPROB;

public class QualityProblemTest {
  private final static Resource qualityReport = QPROB.LabelsUsingCapitalsProblem;
  private static Model model = ModelFactory.createDefaultModel();
  private static List<Quad> quads = new ArrayList<Quad>();

  @BeforeClass
  public static void before() {
    model.createResource("http://example.org/#spiderman").addProperty(RDFS.label, "Otherlabel");
    model.createResource("http://example.org/#spiderman").addProperty(RDFS.label, "Otherlabellll");
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
  }

  @Test
  public void eqaulsMethodTest() {
    QualityProblem n1 = new QualityProblem(0, quads.get(0), qualityReport);
    QualityProblem n2 = new QualityProblem(0, quads.get(1), qualityReport);
    QualityProblem n3 = new QualityProblem(0, quads.get(0), qualityReport);
    Assert.assertTrue(n1.equals(n3));
    Assert.assertFalse(n2.equals(n3));
  }

  @Test
  public void hashCodeTest() {
    QualityProblem n1 = new QualityProblem(0, quads.get(0), qualityReport);
    QualityProblem n2 = new QualityProblem(0, quads.get(1), qualityReport);
    QualityProblem n3 = new QualityProblem(0, quads.get(0), qualityReport);
    HashMap<Integer, QualityProblem> map = new HashMap<Integer, QualityProblem>();
    map.put(n1.hashCode(), n1);
    Assert.assertTrue(map.containsKey(n3.hashCode()));
    Assert.assertFalse(map.containsKey(n2.hashCode()));
  }
}
