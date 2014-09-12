package com.google.refine.quality.problems;


import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class UndefinedClassORPropertyProblem extends QualityProblem {
	private boolean isClass = true;

	public UndefinedClassORPropertyProblem(Integer rowIndex, Quad quad,
			Resource qualityReport) {
		super(rowIndex, quad, qualityReport);
		// TODO Auto-generated constructor stub
	}
	
	public void isProperty(){
		isClass = false;
	}
	
	@Override
 public String getProblemDescription(){
		String description = " is not defined";
		if (isClass) {
			return this.quad.getObject() + description;
		}
		else{
			return this.quad.getPredicate() + description;
		}	    	
	  }    
 

}
