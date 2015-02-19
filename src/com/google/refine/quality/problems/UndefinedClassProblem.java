package com.google.refine.quality.problems;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class UndefinedClassProblem extends QualityProblem {

  private String suggestion;

  public UndefinedClassProblem(Quad quad, Resource qualityReport, String suggestion) {
    super(quad, qualityReport);
    this.suggestion = suggestion;
  }

  @Override
  public String getCleaningSuggestion() {
    return String.format("Possible class is %s", suggestion);
  }
}
