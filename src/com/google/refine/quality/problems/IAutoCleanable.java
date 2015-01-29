package com.google.refine.quality.problems;

import com.hp.hpl.jena.rdf.model.Statement;

public interface IAutoCleanable {
  Statement getCleanedStatement();
}
