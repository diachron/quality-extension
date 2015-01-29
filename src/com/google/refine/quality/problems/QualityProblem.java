package com.google.refine.quality.problems;

import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.VocabularyLoader;
import com.google.refine.quality.vocabularies.QPROB;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Contains information about problematic quads.
 * TODO make the class abstract.
 */
public class QualityProblem {

  protected final Quad quad;
  protected final Resource problemtURI;

  public QualityProblem(Quad quad, Resource qualityReport) {
    this.quad = quad;
    this.problemtURI = qualityReport;
  }

  public Quad getQuad() {
    return quad;
  }

  public Resource getProblemURI() {
    return problemtURI;
  }

  public String getProblemDescription() {
    return VocabularyLoader.getResourcePropertyValue(this.problemtURI, QPROB.problemDescription,
        Constants.QPROB_VOCAB);
  }

  public String getCleaningSuggestion() {
    return VocabularyLoader.getResourcePropertyValue(this.problemtURI, QPROB.cleaningSuggestion,
        Constants.QPROB_VOCAB);
  }
  public String getQRefineRule() {
    return VocabularyLoader.getResourcePropertyValue(this.problemtURI, QPROB.qrefineRule, 
        Constants.QPROB_VOCAB);
  }

  public String getProblemName() {
    return VocabularyLoader.getResourcePropertyValue(this.problemtURI, RDFS.label,
        Constants.QPROB_VOCAB);
  }

  public String getGrelExpression() {
    return VocabularyLoader.getResourcePropertyValue(this.problemtURI, QPROB.qrefineRule,
        Constants.QPROB_VOCAB);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (other == null || (this.getClass() != other.getClass())) {
      return false;
    }
    QualityProblem obj = (QualityProblem) other;
    return ((this.quad != null && this.quad.equals(obj.quad)) && (this.problemtURI != null
      && this.problemtURI.equals(obj.problemtURI)));
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = 31 * hash + quad.hashCode();
    hash = 31 * hash + problemtURI.hashCode();
    return hash;
  }
}
