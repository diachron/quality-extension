package com.google.refine.quality.problems;

import com.google.refine.quality.utilities.Utilities;
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
    String object = quad.getObject().toString();
    
    Statement statement = Utilities.createStatement(subject, predicate, object);
    String literalVal = statement.getObject().asLiteral().getString();
    statement.changeObject(literalVal.trim());
    
    return statement;
  }
}
