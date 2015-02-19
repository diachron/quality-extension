package com.google.refine.quality.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.problems.UndefinedClassProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Detects undefined classes and properties from data set by checking for its
 * definition in their respective referred vocabulary.
 * 
 * Metric Value Range : [0 - 1] Best Case : 0 Worst Case : 1
 */
public class UndefinedClasses extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(UndefinedClasses.class);

  private final Resource qualityReport = QPROB.UndefinedClassesProblem;
  private long undefinedClasses = 0;
  private long classes = 0;

  private static Set<String> properties = new HashSet<String>();
  private static Set<String> allClasses = new HashSet<String>();

  /**
   * Loads a list of class properties.
   * @param args Arguments, args[0] is a path to annotation properties file.
   */
  @Override
  public void before(Object... args) {
    String path = (args == null || args.length == 0) ? Constants.UNDEFINED_CLASS_PROPERTIES_FILE
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
   * Clears a list of class properties.
   */
  @Override
  public void after() {
    properties.clear();
  }

  @Override
  public void compute(List<Quad> quads) {
    collectClasses(quads);
    for (Quad quad : quads) {
      compute(quad);
    }
  }

  private void collectClasses(List<Quad> quads) {
    for (Quad quad : quads) {
      Node object = quad.getObject();
      if (properties.contains(quad.getPredicate().getURI()) && object.isURI()) {
        Model model = VocabularyReader.read(object.getURI());
        if (!model.isEmpty()) {
          allClasses.add(object.toString());
        }
      }
    }
  }

  /**
   * The method identifies whether a component (subject, predicate or object)
   * of the given quad references an undefined class.
   * @param quad A quad to check for quality problems.
   */
  public void compute(Quad quad) {
    Node object = quad.getObject();

    if (properties.contains(quad.getPredicate().getURI()) && object.isURI()) {
      classes++;
      Model model = VocabularyReader.read(object.getURI());
      if (model.isEmpty()) {
        undefinedClasses++;
        problems.add(new UndefinedClassProblem(quad, qualityReport, getMostSimilarType(quad.getObject())));
        LOG.info(String.format("Undefined class is found in quad: %s", quad.toString()));
      } else if (!model.getResource(object.getURI()).isURIResource()) {
        undefinedClasses++;
        getMostSimilarType(quad.getObject());
        problems.add(new UndefinedClassProblem(quad, qualityReport, getMostSimilarType(quad.getObject())));
        LOG.info(String.format("Undefined class is found in quad: %s", quad.toString()));
      }
    }
  }

  private String getMostSimilarType(Node object) {
    Levenshtein sim = new Levenshtein();
    double max = Constants.LEVENSTEIN_THRESHOLD;
    String bestMatch = "";
    for (String type : allClasses) {
      double similarity = sim.getSimilarity(type, object.toString());
      if (similarity > max) { max = similarity; bestMatch = type; }
    }
    LOG.info(String.format("Best match is \" %s \" with Levenstein distance of %f", bestMatch, max));
    return bestMatch;
  }

  /**
   * This method returns metric value for the object of this class.
   * @return The ratio of undefined classes to the total number of classes.
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
