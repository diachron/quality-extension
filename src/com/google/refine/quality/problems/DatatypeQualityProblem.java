package com.google.refine.quality.problems;

import java.net.URI;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class DatatypeQualityProblem extends QualityProblem {

  private String expectedDatatype;
  private String currentDatatype;

  public DatatypeQualityProblem(Quad quad, Resource qualityReport) {
    super(quad, qualityReport);
  }

  public void setDatatypes(URI expected, URI available) {
    expectedDatatype = expected.getFragment();
    currentDatatype = available.getFragment();
  }

  @Override
  public String getProblemDescription() {
    return String.format("Expected: %s available: %s", expectedDatatype, currentDatatype);
  }

  public String getCleaningSuggestion() {
    return String.format("Convert literal to %s", expectedDatatype);
  }

  @Override
  public String getGrelExpression() {
    if (expectedDatatype.endsWith("dateTime")) {
      return "split(value, \"\\\"\")[0].toDate()+ replace(split(value,  \"\\\"\")[1]," + "\""
          + currentDatatype + "\", " + "\"" + expectedDatatype + "\")";
    } else if (expectedDatatype.endsWith("int")) {
      return "split(value, \"\\\"\")[0].toNumber()+ replace(split(value,  \"\\\"\")[1]," + "\""
          + currentDatatype + "\", " + "\"" + expectedDatatype + "\")";
    } else {
      return "";
    }
  }
}
