package com.google.refine.quality.metrics;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * TODO
 * The Ontology Hijacking detects the redefinition by analyzing defined classes
 * or properties in data set and looks of same definition in its respective
 * vocabulary.
 * 
 * Note: This class uses utilities.VocabularyReader to download models
 * (vocabularies) from web. Though VocabularyReader has it own cache but it has
 * some inherit performance/scalability issues.
 */
public class OntologyHijacking extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(OntologyHijacking.class);
  private Resource qualityReport = QPROB.OntologyHijackingProblem;

  private long propertiesAndClasses = 0;
  private long hijacked = 0;

  @Override
  public void before(Object... args) {};

  @Override
  public void after() {};

  /**
   * Check if the given quad has predicate of URI with given fragment
   * 
   * @param predicate
   * @param fragment
   * @return true if predicate is URI with given fragment
   */
  protected boolean isDefinedClassOrProperty(Quad quad, String fragment) {
    try {
      if (quad.getPredicate().isURI()) {
        URI tmpURI = new URI(quad.getPredicate().getURI());
        return (tmpURI.getFragment() != null &&
            tmpURI.getFragment().toLowerCase().equals(fragment)) ? true : false;
      }
    } catch (MalformedURIException e) {
      LOG.error(e.getMessage());
    }
    return false;
  }

  /**
   * Detects if given node is defined in vocabulary or not
   * @param node
   * @return true - if given node is found in the vocabulary with property of RDF.type
   */
  protected boolean isHijacked(Node node) {
    Model model = VocabularyReader.read(node.getURI());
    if (model != null && model.getResource(node.getURI()).isURIResource()) {
      if (model.getResource(node.getURI()).hasProperty(RDF.type)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   */
  @Override
  public void compute(Quad quad) {
    if (isDefinedClassOrProperty(quad, "type")) {
      propertiesAndClasses++;
      Node subject = quad.getSubject();

      if (isHijacked(subject)) {
        hijacked++;
        problems.add(new QualityProblem(quad, qualityReport));
      }
    } else if (isDefinedClassOrProperty(quad, "domain")) {
      propertiesAndClasses++;
      Node object = quad.getObject();

      if (isHijacked(object)) {
        hijacked++;
        problems.add(new QualityProblem(quad, qualityReport));
      }
    }
  }

  /**
   * Returns metric value for between 0 to 1. Where 0 as the best case and 1 as
   * worst case
   * @return double - range [0 - 1]
   */
  @Override
  public double metricValue() {
    if (propertiesAndClasses == 0) {
      LOG.warn("Total classes or properties count is ZERO");
      return 0;
    }
    return (double) hijacked / (double) propertiesAndClasses;
  }
}
