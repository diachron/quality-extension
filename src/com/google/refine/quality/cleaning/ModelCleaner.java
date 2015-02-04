package com.google.refine.quality.cleaning;

import java.util.List;

import com.google.refine.quality.problems.AutoCleanable;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Utilities;
import com.hp.hpl.jena.rdf.model.Model;

public class ModelCleaner {

  /**
   * Automatically cleans a model from determined quality problems.
   * <p> An quality problem is cleaned only if its data type implements
   * the AutoCleanble interface.</p>
   *
   * @param model A {@code Model} for cleaning.
   * @param problems A {@code List} of quality problems corresponding to the {@code Model}.
   * @return A cleaned RDF {@code Model}}.
   */
  public Model cleanModel(Model model, List<QualityProblem> problems) {
    for(QualityProblem problem: problems) {
      model.remove(Utilities.createStatement(problem.getQuad()));
      if (problem instanceof AutoCleanable) {
        model.add(((AutoCleanable) problem).getCleanedStatement());
      }
    }
    return model;
  }
}
