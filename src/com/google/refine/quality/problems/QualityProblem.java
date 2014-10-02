package com.google.refine.quality.problems;

import com.google.refine.quality.utilities.QualityReportModelLoader;
import com.google.refine.quality.vocabularies.QPROB;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Contains information about problematic quads.
 * 
 * @author Muhammad
 * 
 */
public class QualityProblem {

  protected Integer rowIndex;
  protected Quad quad;
  protected Resource problemtURI;

  public Integer getRowIndex() {
    return rowIndex;
  }

  public Quad getQuad() {
    return quad;
  }

  public Resource getProblemURI() {
    return problemtURI;
  }

  public String getProblemDescription() {
    return QualityReportModelLoader.getResourcePropertyValue(this.problemtURI,
        QPROB.problemDescription);
  }

  public String getCleaningSuggestion() {
    return QualityReportModelLoader.getResourcePropertyValue(this.problemtURI,
        QPROB.cleaningSuggestion);
  }

  public String getProblemName() {
    return QualityReportModelLoader.getResourcePropertyValue(this.problemtURI, RDFS.label);
  }

  public String getGrelExpression() {
    return QualityReportModelLoader.getResourcePropertyValue(this.problemtURI, QPROB.qrefineRule);
  }

  public QualityProblem(Integer rowIndex, Quad quad, Resource qualityReport) {
    this.rowIndex = rowIndex;
    this.quad = quad;
    this.problemtURI = qualityReport;
  }

  @Override
  public boolean equals(Object other){
      if(this == other) return true;
      if(other == null || (this.getClass() != other.getClass())){
          return false;
      }
      QualityProblem obj = (QualityProblem) other;
      return ((this.quad != null && this.quad.equals(obj.quad)) &&
             (this.problemtURI != null && this.problemtURI.equals(obj.problemtURI)));
  }

   @Override
  public int hashCode() {
    int hash = 17;
    hash = 31 * hash + quad.hashCode();
    hash = 31 * hash + problemtURI.hashCode();
    return hash;
  }
}
