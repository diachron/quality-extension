package com.google.refine.quality.problems;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class UndefinedPropertyProblem extends QualityProblem {

  private String suggestion ;
  public UndefinedPropertyProblem(Quad quad, Resource qualityReport, String suggestion) {
    super(quad, qualityReport);
    this.suggestion = suggestion;
  }

  @Override
  public String getCleaningSuggestion() {
    return String.format("Possible property is %s", suggestion);
  }

  @Override
  public String getProblemDescription() {
    return String.format("%s is not defined", quad.getPredicate());
  }
}
