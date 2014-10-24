package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.refine.quality.vocabularies.QPROB;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * TODO refactor
 * This metric reports outliers or conflicts of literal datatypes. Since it is
 * meant to measure the homogeneity and not the validity possible rdfs:range
 * restrictions are not evaluated.
 * 
 * This metric makes two distinctions as far as homogeneity is concerned: 1) if
 * there are just a few triples of one predicate having a datatype different
 * from all other triples, they are considered as outliers and are all reported
 * 2) if there is no obvious ratio of possibly wrong and possibly right triples
 * the conflicting view candidates are reported
 * 
 * @author Muhammad Ali Qasmi
 * @date 12th May 2014
 */
public class HomogeneousDatatypes extends AbstractQualityMetric {
  private static final Logger LOG = Logger.getLogger(HomogeneousDatatypes.class);
  private static final Resource qualityReport  = QPROB.HomogeneousDatatypesProblem;

  private static final int THRESHOLD = 99;
  private long propertiesWithHeterogeneousDatatype = 0;
  private long properties = 0;

  private List<Node> problems= new ArrayList<Node>();
  private Hashtable<Node, Hashtable<RDFDatatype, Long>> propertiesDatatypeMatrix =
    new Hashtable<Node, Hashtable<RDFDatatype, Long>>();
  /**
   * This method extracts properties from a given quad and stores it into the
   * hash table with required information e.g. rdf type, count.
   */
  @Override
  public void compute(Quad quad) {
    Node predicate = quad.getPredicate();
    Node object = quad.getObject();
    if (object.isLiteral()) {
      properties++;
      RDFDatatype rdfdataType = object.getLiteralDatatype();
      if (rdfdataType != null) {
        if (propertiesDatatypeMatrix.containsKey(predicate)) {
          Hashtable<RDFDatatype, Long> tmpObjectTypes = propertiesDatatypeMatrix.get(predicate);
          if (tmpObjectTypes != null) {

            long tmpCount = 0;

            if (tmpObjectTypes.containsKey(rdfdataType)) {
              tmpCount = tmpObjectTypes.get(rdfdataType) + 1;

              tmpObjectTypes.remove(rdfdataType);
              tmpObjectTypes.put(rdfdataType, tmpCount);
            } else {
              tmpObjectTypes.put(rdfdataType, ++tmpCount);
            }
            propertiesDatatypeMatrix.remove(predicate);
            propertiesDatatypeMatrix.put(predicate, tmpObjectTypes);
          } else {

            tmpObjectTypes = new Hashtable<RDFDatatype, Long>();
            tmpObjectTypes.put(rdfdataType, 1L);

            propertiesDatatypeMatrix.remove(predicate);
            propertiesDatatypeMatrix.put(predicate, tmpObjectTypes);
          }
        } else {
          Hashtable<RDFDatatype, Long> tmpObjectTypes = new Hashtable<RDFDatatype, Long>();
          tmpObjectTypes.put(rdfdataType, new Long(1));
          propertiesDatatypeMatrix.put(predicate, tmpObjectTypes);
        }
      }
    }
  }
  /**
   * This method identifies whether a given property is heterogeneous or not.
   * @param givenTable property (its rdf type and its count)
   * @param threshold to declare a property as heterogeneous
   * @return true - if heterogeneous
   */
  private boolean isHeterogeneousDataType(Hashtable<RDFDatatype, Long> givenTable, int threshold) {
    long max = 0;
    long total = 0;
    Enumeration<RDFDatatype> enumKey = givenTable.keys();

    while (enumKey.hasMoreElements()) {
      RDFDatatype key = enumKey.nextElement();
      long value = givenTable.get(key);
      max = (value > max) ? value : max;
      total += value;
    }
    return (((max / total) * 100) >= threshold) ? true : false;
  }

  /**
   * This method counts number of heterogeneous data type properties
   * @return
   */
  protected long countHeterogeneousDataTypePropeties() {
    long count = 0;
    Enumeration<Node> enumKey = propertiesDatatypeMatrix.keys();
    while (enumKey.hasMoreElements()) {
      Node key = enumKey.nextElement();
      if (!isHeterogeneousDataType(propertiesDatatypeMatrix.get(key), THRESHOLD)) {
        count++;
        problems.add(key);
      }
    }
    return count;
  }

  @Override
  public double metricValue() {
    propertiesWithHeterogeneousDatatype = countHeterogeneousDataTypePropeties();
    if (properties == 0) {
      LOG.warn("Total number of properties is zero.");
      return 0.0;
    }
    return (double) propertiesWithHeterogeneousDatatype / (double) properties;
  }
}
