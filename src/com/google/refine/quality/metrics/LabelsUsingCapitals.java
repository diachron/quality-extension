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

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.vocabularies.QPROB;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * LabelsUsingCapitals identifies triples whose property is from a pre-configured list of label 
 * properties, and whose object uses a bad style of capitalization.
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
  private final static Logger LOG = Logger.getLogger(LabelsUsingCapitals.class);

  // TODO move to class with constants.
  private final static String CAMEL_CASE_REGEX = "[A-Z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*[A-Z][A-Z0-9]*[a-z])[A-Za-z0-9]*";
  private final static String LABEL_PROPERTIES_FILE = "extensions/quality-extension/resources/LabelPropertiesList";

  private int literals = 0;
  private int badCapitalizationLiterals = 0;

  private final static Resource qualityReport = QPROB.LabelsUsingCapitalsProblem;
  private static Set<String> annotationPropertiesSet = new HashSet<String>();

  /**
   * Loads a list of annotation properties. Uses the default annotation property file.
   * @param path
   *          A path to annotation properties file.
   */
  public static void loadAnnotationPropertiesSet() {
    loadAnnotationPropertiesSet(LABEL_PROPERTIES_FILE);
  }

  /**
   * Loads a list of annotation properties.
   * @param path
   *          A path to annotation properties file.
   */
  public static void loadAnnotationPropertiesSet(String path) {
    File file = null;
    try {
      file = new File(path);
      if (file.exists() && file.isFile()) {
        String line = null;
        BufferedReader in = new BufferedReader(new FileReader(file));
        while ((line = in.readLine()) != null) {
          if (new URI(line) != null) {
            annotationPropertiesSet.add(line);
          }
        }
        in.close();
      }
    } catch (FileNotFoundException e) {
      LOG.debug(e.getStackTrace());
      LOG.error(e.getMessage());
    } catch (IOException e) {
      LOG.debug(e.getStackTrace());
      LOG.error(e.getMessage());
    }
  }

  /**
   * Clears a list of annotation properties.
   */
  public static void clearAnnotationPropertiesSet() {
    LabelsUsingCapitals.annotationPropertiesSet.clear();
  }

  /**
   * Checks whether given quad has predicate with URI found in annotation
   * properties set if true then checks the object's value in that quad; whether
   * it is bad capitalization or not.
   * 
   */
  @Override
  public void compute(Integer index, Quad quad) {
    Node predicate = quad.getPredicate();
    if (predicate.isURI()) {
      if (LabelsUsingCapitals.annotationPropertiesSet.contains(predicate.getURI())) {
        Node object = quad.getObject();
        literals++;
        assessLiteralValue(object, index, quad);
      }
    }
  }

  /**
   * Checks a literal value for bad capitalization.
   * @param object
   * @param index
   * @param quad
   */
  private void assessLiteralValue(Node object, Integer index, Quad quad) {
    if (object.isLiteral()) {
      String value = object.getLiteralValue().toString().trim();

      if (value != null && !value.isEmpty()) {
        if (value.matches(CAMEL_CASE_REGEX)) {
          badCapitalizationLiterals++;
          problemList.add(new QualityProblem(index, quad, qualityReport));
        }
      }
    }
  }

  /**
   * Calculates a metric value. Ratio of bad capitalization literals to total number of literals.
   * @return Ratio of bad capitalization literals to total number of literals.
   */
  @Override
  public double metricValue() {
    LOG.debug("Total number of bad capitalization literals : " + badCapitalizationLiterals);
    LOG.debug("Total number of literals: " + literals);
    if (literals <= 0) {
      LOG.warn("Total number of literals is 0.");
      return 0;
    }
    return ((double) badCapitalizationLiterals / (double) literals);
  }
}
