package com.google.refine.quality.problems;

import java.net.URI;
import java.net.URISyntaxException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class MalformedDatatypeProblem extends QualityProblem {

  private String expectedDatatype;

  public MalformedDatatypeProblem(Quad quad, Resource qualityReport) {
    super(quad, qualityReport);
  }

  public void setDatatype(String expected) {
    try {
      expectedDatatype = new URI(expected).getFragment();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getProblemDescription() {
    return String.format("Expected datatype %s.", expectedDatatype);
  }

  @Override
  public String getCleaningSuggestion() {
    return String.format("Convert %s to %s.", quad.getObject().getLiteralLexicalForm(),
      expectedDatatype);
  }

  @Override
  public String getGrelExpression() {
    if (expectedDatatype.endsWith("dateTime")) {
      return "split(value.trim(), \"\\\"\")[0].toDate()+ split(value, \"\\\"\")[1]";
    } else if (expectedDatatype.endsWith("int")) {
      return "split(value.trim(), \"\\\"\")[0].toNumber()+ split(value, \"\\\"\")[1]";
    } else {
      return " ";
    }
  }

  @Override
  public Quad getCleanedQuad() {
    throw new NotImplementedException();
  }
}
