package com.google.refine.quality.metrics;

import org.apache.log4j.Logger;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric should find resources that are - defined as a property but also
 * appear on subject or object positions in other triples (except cases like
 * ex:prop rdf:type rdfs:Property, ex:prop rds:subPropetyOf) - defined as a
 * class but also appear on predicate position in other triples. The metric is
 * computed as a ratio of misplaced classes and properties
 */

public class MisplacedClassesOrProperties extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(MisplacedClassesOrProperties.class);
  private static final Resource MISPLASED_CLASS = QPROB.MisplacedClassProblem;
  private static final Resource MISPLASED_PROPERTY = QPROB.MisplacedPropertyProblem;

  private int misplacedClasses = 0;
  private int misplacedProperties = 0;
  private int properties = 0;
  private int classes = 0;

  @Override
  public void before(Object... args) {}

  @Override
  public void compute(Quad quad) {
    Node subject = quad.getSubject();
    Node predicate = quad.getPredicate();
    Node object = quad.getObject();

    if (subject.isURI()) {
      classes++;
      if (hasDomainAndRange(subject)) {
        misplacedClasses++;
        LOG.info("Misplace Class found in subject: " + subject.toString());
        problems.add(new QualityProblem(quad, MISPLASED_CLASS));
      }
    }

    if (predicate.isURI()) {
      properties++;
      Model predicateModel = VocabularyReader.read(predicate.getURI());
      if (!predicateModel.isEmpty()) {
        if (predicateModel.getResource(predicate.getURI()).isURIResource()) {
          if (!(predicateModel.getResource(predicate.getURI()).hasProperty(RDFS.domain)
            && predicateModel.getResource(predicate.getURI()).hasProperty(RDFS.range))) {
            LOG.info("Misplace Property found in predicate: " + predicate.toString());
            misplacedProperties++;
            problems.add(new QualityProblem(quad, MISPLASED_PROPERTY));
          }
        }
      }
    }

    if (object.isURI()) {
      classes++;
      if (hasDomainAndRange(object)) {
        LOG.info("Misplace Class found in object: " + object.toString());
        misplacedClasses++;
        problems.add(new QualityProblem(quad, MISPLASED_CLASS));
      }
    }
  }

  private boolean hasDomainAndRange(Node node) {
    Model model = VocabularyReader.read(node.getURI());
    if (!model.isEmpty()) {
      if (model.getResource(node.getURI()).isURIResource()
        && (model.getResource(node.getURI()).hasProperty(RDFS.domain)
          || model.getResource(node.getURI()).hasProperty(RDFS.range))) {
        return true;
      }
    }
    return false;
  }

  /**
   * The method computes metric value.
   * @return Ratio of misplaced classes and properties to total number classes and properties.
   */
  @Override
  public double metricValue() {
    int classesAndProperties = classes + properties;
    if (classesAndProperties == 0) {
      LOG.warn("Total number of classes and properties is zero.");
      return 0.0;
    }
    return (double) (misplacedClasses + misplacedProperties) / classesAndProperties;
  }

  @Override
  public void after() {}
}
