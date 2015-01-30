package com.google.refine.quality.cleaning;

import java.util.List;

import com.google.refine.quality.problems.IAutoCleanable;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Utilities;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

public class ModelCleaner {
  public Model cleanModel(Model model, List<QualityProblem> problems) {
    
    for(QualityProblem problem: problems) {
      
      Quad quad = problem.getQuad();
      Statement statement = 
          Utilities.createStatement(quad.getSubject().toString(), 
              quad.getPredicate().toString(), quad.getObject().toString());
      model.remove(statement);
      if (problem instanceof IAutoCleanable) {
        model.add(((IAutoCleanable) problem).getCleanedStatement());
      }
    }
    
    return model;
  }
}
