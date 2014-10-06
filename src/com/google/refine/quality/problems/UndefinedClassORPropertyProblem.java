package com.google.refine.quality.problems;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class UndefinedClassORPropertyProblem extends QualityProblem {
  private boolean isClass = true;

  public UndefinedClassORPropertyProblem(Quad quad, Resource qualityReport) {
    super(quad, qualityReport);
  }

  public void setToProperty() {
    isClass = false;
  }

  @Override
  public String getProblemDescription() {
    String description = " is not defined.";
    if (isClass) {
      return quad.getObject() + description;
    } else {
      return quad.getPredicate() + description;
    }
  }
}
