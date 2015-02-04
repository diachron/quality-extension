package com.google.refine.quality.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.problems.WhitespaceInAnnotationProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.vocabularies.QPROB;

/**
 * WhitespaceInAnnotation consider the following widely used annotation
 * properties (labels, comments, notes, etc.) and identifies triples whose
 * property is from a pre-configured list of annotation properties, and whose
 * object value has leading or ending white space in string.
 */
public class WhitespaceInAnnotation extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(LabelsUsingCapitals.class);
  private final Resource qualityReport = QPROB.WhitespaceInAnnotationProblem;

  private long literals = 0;
  private long whitespaceLiterals = 0;
  private static Set<String> properties = new HashSet<String>();

  /**
   * Loads a list of annotation properties.
   * @param args Arguments, args[0] is a path to annotation properties file.
   */
  @Override
  public void before(Object... args) {
    String path = (args == null || args.length == 0) ? Constants.ANNOTATION_PROPERTIES_FILE
        : (String) args[0];
    File file = null;
    try {
      if (!path.isEmpty()) {
        file = new File(path);
        if (file.exists() && file.isFile()) {
          String line = null;
          BufferedReader in = new BufferedReader(new FileReader(file));
          while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (new URI(line.trim()) != null) {
              properties.add(line);
            }
          }
          in.close();
        }
      }
    } catch (FileNotFoundException e) {
      LOG.error(e.getMessage());
      throw new MetricException("Annotation properties file is not found. "
          + e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.error(e.getMessage());
      throw new MetricException("Annotation properties file is not found. "
          + e.getLocalizedMessage());
    }
  }

  /**
   * Clears a list of annotation properties.
   */
  @Override
  public void after() {
    properties.clear();
  }

  /**
   * Check an object of a quad for whitespace.
   * @param quad A quad to check for quality problems.
   */
  @Override
  public void compute(Quad quad) {
    Node predicate = quad.getPredicate();
    if (predicate.isURI() && properties.contains(predicate.getURI())) {
      literals++;
      detectWhitespaces(quad);
    }
  }

  /**
   * Check whether annotation in a quad has whitespace.
   * @param quad A quad to check for quality problems.
   */
  private void detectWhitespaces(Quad quad) {
    Node object = quad.getObject();
    if (object.isLiteral()) {
      String value = object.getLiteralValue().toString();
      if (!value.equals(value.trim()) && !value.trim().isEmpty()) {
        whitespaceLiterals++;
        problems.add(new WhitespaceInAnnotationProblem(quad, qualityReport));
        LOG.info(String.format("Whitespce is found in annotation of quad: %s", quad.toString()));
      }
    }
  }

  /**
   * Calculates a metric value. Ratio of literals with whitespace to total
   * number of literals.
   * @return Ratio of literals with whitespace to the total number of literals.
   */
  @Override
  public double metricValue() {
    if (literals == 0) {
      LOG.warn("Total number of literals is 0.");
      return 0;
    }
    return ((double) whitespaceLiterals / (double) literals);
  }
}
