package com.google.refine.quality.metrics;

import org.apache.log4j.Logger;

import com.google.refine.quality.problems.MalformedDatatypeProblem;
import com.google.refine.quality.vocabularies.QPROB;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Detects whether the value of a typed literal is valid with respect to its
 * given xsd datatype.
 */
public class MalformedDatatypeLiterals extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(MalformedDatatypeLiterals.class);
  private static final Resource qualityReport = QPROB.MalformedDatatypeLiteralsProblem;

  private int literals = 0;
  private int malformedLiterals = 0;

  /**
   * Identifies whether a given quad is malformed.
   * @param quad A quad to check for the problem.
   */
  @Override
  public void compute(Quad quad) {
    Node object = quad.getObject();
    if (object.isLiteral()) {
      detectMalformedDatatype(quad);
      literals++;
    }
  }

  private void detectMalformedDatatype(Quad quad) {
    Node object = quad.getObject();
    RDFDatatype rdfdataType = object.getLiteralDatatype();

    if (rdfdataType != null) {
      if (!rdfdataType.isValidLiteral(object.getLiteral())) {
        malformedLiterals++;

        MalformedDatatypeProblem problem = new MalformedDatatypeProblem(quad, qualityReport);
        problem.setDatatype(rdfdataType.getURI());
        problems.add(problem);
        LOG.info(String.format("Malformed literal is found in quad: %s", quad.toString()));
      }
    }
  }

  /**
   * Calculates a metric value. Ratio of literals with malformed data types to total
   * number of literals.
   * @return Ratio of literals with malformed data types to total number of literals.
   */
  @Override
  public double metricValue() {
    if (literals == 0) {
      LOG.warn("Total number of literals in given document is 0.");
      return 0.0;
    }
    return (double) malformedLiterals / (double) literals;
  }
}
