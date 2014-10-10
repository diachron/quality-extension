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
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.vocabularies.QPROB;

/**
 * LabelsUsingCapitals identifies triples whose property is from a
 * pre-configured list of label properties, and whose object uses a bad style of
 * capitalization.
 * 
 * Metric value Range = [0 - 1]. Best Case = 0, Worst Case = 1
 * 
 * The metric value is defined as the ratio of labels with "bad capitalization"
 * to all labels ( triples having such properties).
 * 
 * This metric is from the list of constrains for scientific pilots and is
 * introduced in the Deliverable 3.1 (Table 20)
 */

public class LabelsUsingCapitals extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(LabelsUsingCapitals.class);

  private int literals = 0;
  private int capitalizationLiterals = 0;

  private static Resource qualityReport = QPROB.LabelsUsingCapitalsProblem;
  private static Set<String> properties = new HashSet<String>();

  /**
   * Loads a list of label properties.
   * @param args Arguments, args[0] is a path to label properties file.
   */
  @Override
  public void before(Object... args) {
    String path = (args == null || args.length == 0) ? Constants.LABEL_PROPERTIES_FILE
        : (String) args[0];
    File file = null;
    try {
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
    } catch (FileNotFoundException e) {
      LOG.error(e.getMessage());
      throw new MetricException("Label properties file is not found. "
          + e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.error(e.getMessage());
      throw new MetricException("Label properties file is not found. "
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
   * The method checks an object's value for bad capitalization.
   * @param quad A quad to check for quality problems.
   */
  @Override
  public void compute(Quad quad) {
    Node predicate = quad.getPredicate();
    if (predicate.isURI() && properties.contains(predicate.getURI())) {
      literals++;
      assessLiteralValue(quad);
    }
  }

  /**
   * Checks a literal value for bad capitalization.
   * 
   * @param object
   * @param index
   * @param quad
   */
  private void assessLiteralValue(Quad quad) {
    Node object = quad.getObject();
    if (object.isLiteral()) {
      String value = object.getLiteralValue().toString().trim();
      if (!value.isEmpty() && value.matches(Constants.CAMEL_CASE_REGEX)) {
        capitalizationLiterals++;
        problems.add(new QualityProblem(quad, qualityReport));
        LOG.info(String.format("Bad capitalization is found in quad: %s", quad.toString()));
      }
    }
  }

  /**
   * Calculates a metric value. Ratio of bad capitalization literals to total
   * number of literals.
   * @return Ratio of bad capitalization literals to the total number of literals.
   */
  @Override
  public double metricValue() {
    if (literals == 0) {
      LOG.warn("Total number of literals is 0.");
      return 0;
    }
    return (double) capitalizationLiterals / (double) literals;
  }
}
