package com.google.refine.quality.metrics;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.problems.UndefinedClassORPropertyProblem;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;

/**
 *  * TODO refactor
 * Detects undefined classes and properties from data set by checking for its
 * definition in their respective referred vocabulary.
 * 
 * Metric Value Range : [0 - 1] Best Case : 0 Worst Case : 1
 */
public class UndefinedClasses extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(UndefinedClasses.class);

  private final Resource qualityReport = QPROB.UndefinedClassesProblem;
  private static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private long undefinedClasses = 0;
  private long classes = 0;

  /**
   * This method identifies whether a component (subject, predicate or object)
   * of the given quad references an undefined class .
   * 
   * @param quad
   *          - to be identified
   */
  @Override
  public void compute(Integer index, Quad quad) {
    try {
      Node predicate = quad.getPredicate();
      Node object = quad.getObject();

      String tmpURI = predicate.getURI();

      if (tmpURI != null
          && (tmpURI.equals(RDF.type.toString()) || tmpURI.equals(RDFS.domain.toString())
              || tmpURI.equals(RDFS.range.toString())
              || tmpURI.equals(RDFS.subPropertyOf.toString())
              || tmpURI.equals(RDFS.subClassOf.toString())
              || tmpURI.equals(OWL.allValuesFrom.toString())
              || tmpURI.equals(OWL.unionOf.toString())
              || tmpURI.equals(OWL.intersectionOf.toString())
              || tmpURI.equals(OWL.someValuesFrom.toString())
              || tmpURI.equals(OWL.equivalentClass.toString())
              || tmpURI.equals(OWL.complementOf.toString()) || tmpURI.equals(OWL.oneOf.toString())
              || tmpURI.equals(OWL.complementOf.toString()) || tmpURI.equals(OWL.disjointWith
              .toString()))) {

        if (object.isURI()) {
          classes++;

          Model objectModel = VocabularyReader.read(object.getURI());
          if (objectModel == null) { // check if system is able to
                                     // retrieve model
            undefinedClasses++;
            QualityProblem reportProblems = new QualityProblem(index, quad, qualityReport);
            this.problemList.add(reportProblems);
          } else {
            // search for URI resource from Model
            if (!objectModel.getResource(object.getURI()).isURIResource()) {
              undefinedClasses++;
              UndefinedClassORPropertyProblem reportProblems = new UndefinedClassORPropertyProblem(
                  index, quad, qualityReport);
              this.problemList.add(reportProblems);
            }
          }
        }
      }
    }

    catch (Exception exception) {
    }
  }

  /**
   * This method returns metric value for the object of this class
   * 
   * @return (total number of undefined classes ) / ( total number of classes )
   */
  @Override
  public double metricValue() {
    if (classes == 0) {
      LOG.warn("Total number of classes  in given document is found to be zero.");
      return 0.0;
    }

    return (double) undefinedClasses / classes;
  }

}
