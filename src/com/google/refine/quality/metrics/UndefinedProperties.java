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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.problems.UndefinedPropertyProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;

public class UndefinedProperties extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(UndefinedClasses.class);
  private Resource qualityReport = QPROB.UndefinedPropertiesProblem;

  private long undefinedProperties = 0;
  private long properties = 0;

  private static Set<String> propertiesSet = new HashSet<String>();

  /**
   * Loads a list of properties.
   * @param args Arguments, args[0] is a path to properties file.
   */
  @Override
  public void before(Object... args) {
    String path = (args == null || args.length == 0) ? Constants.UNDEFINED_PROPERTIES_FILE
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
              propertiesSet.add(line);
            }
          }
          in.close();
        }
      }
    } catch (FileNotFoundException e) {
      LOG.error(e.getMessage());
      throw new MetricException("Properties file is not found. " + e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.error(e.getMessage());
      throw new MetricException("Properties file is not found. " + e.getLocalizedMessage());
    }
  }

  /**
   * Clears a list of class properties.
   */
  @Override
  public void after() {
    propertiesSet.clear();
  }

  /**
   * The method identifies whether a component (subject, predicate or object) of
   * the given quad references an undefined class or property.
   * @param quad A quad to check for quality problems.
   */
  @Override
  public void compute(Quad quad) {
    String predicateURI = quad.getPredicate().getURI();

    if (quad.getPredicate().isURI()) {
      properties++;
      Model model = VocabularyReader.read(predicateURI);
      if (model.isEmpty()) {
        undefinedProperties++;
        problems.add(new QualityProblem(quad, qualityReport));
        LOG.info(String.format("Undefined property is found: %s", predicateURI));
      } else if (model.getResource(predicateURI).isURIResource()) {
        checkDomainAndRange(model, predicateURI, quad);
      }
    }

    if (propertiesSet.contains(predicateURI)) {
      checkPropertyInObject(quad);
    }
  }

  private void checkDomainAndRange(Model model, String uri, Quad quad) {
    if (!(model.getResource(uri).hasProperty(RDFS.domain) && model.getResource(uri)
      .hasProperty(RDFS.range))) {
      undefinedProperties++;
      problems.add(new UndefinedPropertyProblem(quad, qualityReport));
      LOG.info(String.format("Property has not a domain and range: %s", uri));
    }
  }

  private void checkPropertyInObject(Quad quad) {
    String objectURI = quad.getObject().getURI();
    if (quad.getObject().isURI()) {
      properties++;
      Model model = VocabularyReader.read(objectURI);
      if (model.isEmpty()) {
        undefinedProperties++;
        problems.add(new QualityProblem(quad, qualityReport));
        LOG.info(String.format("Object contains an undefined property: %s", objectURI));
      } else if (!model.getResource(objectURI).isURIResource()) {
        undefinedProperties++;
        problems.add(new QualityProblem(quad, qualityReport));
        LOG.info(String.format("Object contains an undefined property: %s", objectURI));
      }
    }
  }

  /**
   * This method returns metric value for the object of this class.
   * @return The ratio of undefined properties to the total number of properties.
   */
  @Override
  public double metricValue() {
    if (properties == 0) {
      LOG.warn("Total number of properties is 0.");
      return 0.0;
    }
    return (double) undefinedProperties / (double) properties;
  }
}
