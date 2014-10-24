package com.google.refine.quality.metrics;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.refine.quality.problems.DatatypeQualityProblem;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Detects incompatible data type of literals by comparing its
 * Data Type URI with the Data Type URI specified in the range of the Object's predicate.
 */
public class IncompatibleDatatypeRange extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(IncompatibleDatatypeRange.class);
  private static final Resource qualityReport = QPROB.IncompatibleDatatypeRangeProblem;

  private Map<String, Statement> cacheProperty = new HashMap<String, Statement>();
  private long literals = 0;
  private long incompatiableDataTypeLiterals = 0;

  @Override
  public void before(Object... args) {  }
  /**
   * Computes whether an quad has an incompatible data type in a subject.
   * @param quad A RDF quad.
   */
  @Override
  public void compute(Quad quad) {
    Node predicate = quad.getPredicate();
    Node object = quad.getObject();

    if (predicate != null && object.isLiteral()) {
      literals++;

      Statement rangeStatement = getRangeAsStatement(predicate);
      if (rangeStatement != null) {
        cacheRangeStatement(predicate, rangeStatement);
        if (isRangeForPredicate(quad, rangeStatement)) {
          checkTypeCompatibility(quad, rangeStatement);
        }
      }
    }
  }

  private Statement getRangeAsStatement(Node predicate) {
    Statement statement;
    if (cacheProperty.containsKey(predicate.getURI())) {
      statement = cacheProperty.get(predicate.getURI());
    } else {
      Model model = VocabularyReader.read(predicate.getURI());
      statement = (!model.isEmpty()) ? (model.getResource(predicate.getURI()))
          .getProperty(RDFS.range) : null;
    }
    return statement;
  }

  private void cacheRangeStatement(Node predicate, Statement rangeStatement) {
    if (!cacheProperty.containsKey(predicate.getURI())) {
      cacheProperty.put(predicate.getURI(), rangeStatement);
    }
  }

  private boolean isRangeForPredicate(Quad quad, Statement rangeStatement) {
    String predicateOfQuad = quad.getPredicate().getURI().toString();
    String subjectOfRange = rangeStatement.getSubject().toString();
    return subjectOfRange.equals(predicateOfQuad);
  }

  private void checkTypeCompatibility(Quad quad, Statement rangeStatement) {
    Node object = quad.getObject();
    try {
      URI actual = (object.getLiteralDatatypeURI() != null) ? new URI(
        object.getLiteralDatatypeURI()) : null;
      URI expected = (rangeStatement.getObject().toString() != null) ? new URI(rangeStatement
        .getObject().toString()) : null;

      if (!checkTypeByComparingURI(actual, expected)) {
        incompatiableDataTypeLiterals++;
        problems.add(new DatatypeQualityProblem(quad, qualityReport, expected, actual));
      }
    } catch (URISyntaxException e) {
      LOG.error("Can not parse string to URI. " + e.getLocalizedMessage());
    }
  }
  
  /**
   * Validates data types of a literal and the one given by the range of a predicate.
   */
  protected boolean checkTypeByComparingURI(URI actualDataType, URI expectedDataType) {
    if (actualDataType != null && expectedDataType == null) {
      return true;
    } else if (actualDataType == null && expectedDataType.getFragment().toLowerCase().equals("literal")) {
      return true;
    } else if (actualDataType == null) {
      return true;
    } else if (expectedDataType == null) {
      return true;
    } else if (actualDataType.equals(expectedDataType)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method returns metric value for the object of this class.
   * @return The ratio of incompatible data types in literals to the total number of literals.
   */
  @Override
  public double metricValue() {
    if (literals == 0) {
      return 0.0;
    }
    return (double) incompatiableDataTypeLiterals / (double) literals;
  }

  @Override
  public void after() {
    cacheProperty.clear();
  }
}
