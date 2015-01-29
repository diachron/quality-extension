package com.google.refine.quality.problems;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

public class WhitespaceInAnnotationProblem extends QualityProblem implements IAutoCleanable {

  public WhitespaceInAnnotationProblem(Quad quad, Resource qualityReport) {
    super(quad, qualityReport);
  }

  @Override
  public Statement getCleanedStatement() {
    
    String subject = quad.getSubject().toString();
    String predicate = quad.getPredicate().toString();
    String object = quad.getObject().getLiteralValue().toString().trim();
    
    return ResourceFactory.createStatement(ResourceFactory.createResource(subject), 
        ResourceFactory.createProperty(predicate), 
        ResourceFactory.createPlainLiteral(object));
  }
}
