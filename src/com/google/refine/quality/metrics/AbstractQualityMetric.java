package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import com.google.refine.quality.problems.QualityProblem;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public abstract class AbstractQualityMetric {

  protected Resource qualityReport = null;
  protected List<QualityProblem> problemList = new ArrayList<QualityProblem>();

  /**
   * Returns value of a metric.
   * @return floating point metric's value.
   */
  public abstract double metricValue();

  /**
   * Computes a metric value.
   * @param quad A quad a metric applied to.
   */
  public abstract void compute(Integer index, Quad quad);

  /**
   * Computes a metric's value
   * @param quad A list of quads a metric applied to.
   */
  public void compute(List<Quad> listQuad) {
    Integer index = 0;
    for (Quad quad : listQuad) {
      this.compute(index++, quad);
    }
  }

  /**
   * Returns a list of problematic quads.
   * @return A list of problematic quads.
   */
  public List<QualityProblem> getQualityProblems() {
    return new ArrayList<QualityProblem>(this.problemList);
  }
  
  //TODO change to abstract later.
  public void before(Object... args) {}
  public void after() {}
}
