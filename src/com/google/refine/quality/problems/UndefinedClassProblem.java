package com.google.refine.quality.problems;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class UndefinedClassProblem extends QualityProblem {

  public UndefinedClassProblem(Quad quad, Resource qualityReport) {
    super(quad, qualityReport);
  }

  @Override
  public String getProblemDescription() {
    return String.format("%s is not defined", quad.getObject());
  }

  @Override
  public Quad getCleanedQuad() {
    throw new NotImplementedException();
  }
}
