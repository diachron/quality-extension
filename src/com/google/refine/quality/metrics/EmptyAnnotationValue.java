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

import com.google.refine.quality.exceptions.QualityExtensionException;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.vocabularies.QPROB;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * EmptyAnnotationValue consider the following widely used annotation properties
 * (labels, comments, notes, etc.) and identifies triples whose property is from
 * a pre-configured list of annotation properties, and whose object is an empty
 * string.
 */
public class EmptyAnnotationValue extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(EmptyAnnotationValue.class);
  private static final Resource qualityReport = QPROB.EmptyAnnotationValueProblem;

  private long literals = 0;
  private long emptyLiterals = 0;
  private static Set<String> annotationProperties = new HashSet<String>();

  /**
   * Loads a annotation properties.
   * @param args Arguments, args[0] is a path to annotation properties file.
   */
  @Override
  public void before(Object... args) {
    String path = (args == null || args.length == 0) ? Constants.ANNOTATION_PROPERTIES_FILE
        : (String) args[0];
    File file = null;
    try {
      file = new File(path);
      if (file.exists() && file.isFile()) {
        String line = null;
        BufferedReader in = new BufferedReader(new FileReader(file));
        while ((line = in.readLine()) != null && !line.isEmpty()) {
          if (new URI(line.trim()) != null) {
            annotationProperties.add(line);
          }
        }
        in.close();
      }
    } catch (FileNotFoundException e) {
      LOG.error(e.getMessage());
      throw new QualityExtensionException("Annotation properties file is not found. "
          + e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.error(e.getMessage());
      throw new QualityExtensionException("Annotation properties file is not found. "
          + e.getLocalizedMessage());
    }
  }

  @Override
  public void after() {
    annotationProperties.clear();
  }

  /**
   * Checks whether given quad has predicate with URI found in annotation
   * properties set if true then checks the object's value in that quad; whether
   * it is empty or not.
   */
  @Override
  public void compute(Quad quad) {
    Node predicate = quad.getPredicate();
    if (predicate.isURI() && annotationProperties.contains(predicate.getURI())) {
      literals++;

      if (isEmptyLiteral(quad)) {
        emptyLiterals++;
        QualityProblem reportProblems = new QualityProblem(quad, qualityReport);
        problems.add(reportProblems);
        LOG.info(String.format("Empty annotation value in quad %s", quad.toString()));
      }
    }
  }

  private boolean isEmptyLiteral(Quad quad) {
    Node object = quad.getObject();
    if (object.isBlank()) {
      return true;
    } else if (object.isLiteral()) {
      String value = object.getLiteralValue().toString().trim();
      if (value.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Calculates a metric value. Ratio of empty annotation literals to total
   * number of literals.
   * 
   * @return Ratio of empty annotation literals to total number of literals.
   */
  @Override
  public double metricValue() {
    if (literals == 0) {
      LOG.warn("Total number of literals are ZERO");
      return 0;
    }
    return ((double) emptyLiterals / (double) literals);
  }
}
