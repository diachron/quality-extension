package com.google.refine.quality.problems;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class WhitespaceInAnnotationProblem extends QualityProblem {

  public WhitespaceInAnnotationProblem(Quad quad, Resource qualityReport) {
    super(quad, qualityReport);
  }

  @Override
  public Quad getCleanedQuad() {
    Node objectNode = 
        NodeFactory.createLiteral(quad.getObject().getLiteralValue().toString().trim());
    return new Quad(null, quad.getSubject(), quad.getPredicate(), objectNode);
  }
}
