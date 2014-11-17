package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Detect properties that are defined as a owl:datatype property
 * but is used as object property and properties defined as a owl:object
 * property and used as data type property The metric is computed as a ratio of
 * misused properties
 */
public class MisusedOwlDatatypeOrObjectProperties extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(MisusedOwlDatatypeOrObjectProperties.class);

  private static final Resource datatypeProperty = QPROB.MisuseOwlDatatypePropertyProblem;
  private static final Resource objectPropertyProblem = QPROB.MisuseOwlObjectPropertyProblem;

  private static final String NAMESPACE_MATCH_SUBSTRING = "http://www.w3.org/2002/07/owl#";
  private static final String OWL_DATA_TYPE_PROPERTY = "DatatypeProperty";
  private static final String OWL_OBJECT_PROPERTY = "ObjectProperty";

  private List<Node> owlDatatypePropertyList = new ArrayList<Node>();
  private List<Node> owlObjectPropertyList = new ArrayList<Node>();

  protected long misusedDatatypeProperties = 0;
  protected long datatypeProperties = 0;
  protected long misusedObjectProperties = 0;
  protected long objectProperties = 0;

  private static List<String> tmpPredicateURI = new ArrayList<String>();
  /**
   * This method separates out owl properties from a given list of quad
   * @param quadList a list of quad to be filtered
   */
  public void before(List<Quad> quads) {
    for (Quad quad : quads) {
      OwlDatatypeProperty(quad);
      OwlObjectProperty(quad);
    }
  }

  private void OwlObjectProperty(Quad quad) {
    Node predicate = quad.getPredicate();
    if (predicate.isURI()) {

      if (!tmpPredicateURI.contains(predicate.getURI())) {
        tmpPredicateURI.add(predicate.getURI());
        System.out.println(tmpPredicateURI.size());
        
        Model model = VocabularyReader.read(predicate.getURI());
        if (!model.isEmpty()) {
          StmtIterator stmt = model.listStatements();
          while (stmt.hasNext()) {
            Statement statement = stmt.next();
            Node object = statement.asTriple().getObject();
            if (object.toString().equals(NAMESPACE_MATCH_SUBSTRING + OWL_DATA_TYPE_PROPERTY)) {
              owlDatatypePropertyList.add(quad.getSubject());
            }
          }
        }
      }
    }
  }

  private void OwlDatatypeProperty(Quad quad) {
    if (quad.getObject().toString().equals(NAMESPACE_MATCH_SUBSTRING + OWL_DATA_TYPE_PROPERTY)) {
      owlDatatypePropertyList.add(quad.getSubject());
    } else if (quad.getObject().toString().equals(NAMESPACE_MATCH_SUBSTRING + OWL_OBJECT_PROPERTY)) {
      owlObjectPropertyList.add(quad.getSubject());
    }
  }

  /**
   * This method computes identified a given quad is a misuse owl data type property or object property.
   * @param quad Quad to identified
   */
  @Override
  public void compute(Quad quad) {
    try {

      Node predicate = quad.getPredicate();
      Node object = quad.getObject();

      if (owlDatatypePropertyList.contains(predicate)) {
        datatypeProperties++;
        if (!object.isLiteral()) {
          misusedDatatypeProperties++;
          problems.add(new QualityProblem(quad, datatypeProperty));
        }
      }

      else if (owlObjectPropertyList.contains(predicate)) {
        objectProperties++;
        if (!object.isURI()) {
          misusedObjectProperties++;
          problems.add(new QualityProblem(quad, objectPropertyProblem));
        }
      }
    } catch (Exception exception) {
      LOG.error(exception.getMessage());
    }
  }

  /**
   * This method computes metric value for the object of this class
   * @return (total misuse properties) / (total properties)
   */
  @Override
  public double metricValue() {
    long properties = datatypeProperties + objectProperties;
    if (properties == 0) {
      LOG.warn("Total number of owl properties is zero.");
      return 0.0;
    }
    return (double) (misusedDatatypeProperties + misusedObjectProperties) / properties;
  }

  /**
   * This method clears all content in the owlDatatypePropertyList and
   * owlObjectPropertyList
   */
  public void after() {
    owlDatatypePropertyList.clear();
    owlObjectPropertyList.clear();
  }
}
