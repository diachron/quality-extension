package com.google.refine.quality.problems;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Interface {@code AutoCleanable } determines that a {@code QualityProblem}
 * instance that implements it is able of cleaning problematic RDF
 * {@code Statement} automatically.
 * 
 * @TODO detail description..
 */
public interface AutoCleanable {
  Statement getCleanedStatement();
}
