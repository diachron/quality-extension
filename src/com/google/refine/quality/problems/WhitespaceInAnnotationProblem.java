package com.google.refine.quality.problems;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class WhitespaceInAnnotationProblem extends QualityProblem {

  public WhitespaceInAnnotationProblem(Quad quad, Resource qualityReport) {
    super(quad, qualityReport);
  }

  @Override
  public Quad getCleanedQuad() {
    throw new NotImplementedException();
  }
}
