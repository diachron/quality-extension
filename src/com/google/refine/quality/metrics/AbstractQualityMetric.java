package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import com.google.refine.quality.problems.QualityProblem;

import com.hp.hpl.jena.sparql.core.Quad;

public abstract class AbstractQualityMetric {

  protected List<QualityProblem> problems = new ArrayList<QualityProblem>();
//  protected HashMap<Integer, QualityProblem> problems = new HashMap<Integer, QualityProblem>();

  /**
   * Returns value of a metric.
   * @return floating point metric's value.
   */
  public abstract double metricValue();

  /**
   * Computes a metric value.
   * @param quad A quad a metric applied to.
   */
  public abstract void compute(Quad quad);

  /**
   * Computes a metric's value
   * @param quads A list of quads a metric applied to.
   */
  public void compute(List<Quad> quads) {
    for (Quad quad : quads) {
      compute(quad);
    }
  }

  /**
   * Returns a list of problematic quads.
   * @return A list of problematic quads.
   */
  public List<QualityProblem> getQualityProblems() {
    return new ArrayList<QualityProblem>(problems);
  }
  
  //TODO change to abstract later.
  public void before(Object... args) {}
  public void after() {}
}
