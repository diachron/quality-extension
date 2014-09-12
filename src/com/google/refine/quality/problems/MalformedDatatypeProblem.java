package com.google.refine.quality.problems;

import java.net.URI;
import java.net.URISyntaxException;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class MalformedDatatypeProblem extends QualityProblem {

	public MalformedDatatypeProblem(Integer rowIndex, Quad quad,
			Resource qualityReport) {
		super(rowIndex, quad, qualityReport);
		// TODO Auto-generated constructor stub
	}
	
	private String expectedDatatype;
	

	
	
	public void setDatatype(String expected){
		try {
			this.expectedDatatype = new URI(expected).getFragment();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
	@Override
 public String getProblemDescription(){
	    	return "Expected datatype" + expectedDatatype ;
	  }
	    
 public String getCleaningSuggestion(){
	    	return "Convert " + this.quad.getObject().getLiteralLexicalForm() + " to " + expectedDatatype;
	    }
 
 @Override
 public String getGrelExpression(){
	 if (expectedDatatype.endsWith("dateTime")) {
		return  "split(value.trim(), \"\\\"\")[0].toDate()+ split(value, \"\\\"\")[1]";
	 }else if (expectedDatatype.endsWith("int")) {
		 return  "split(value.trim(), \"\\\"\")[0].toNumber()+ split(value, \"\\\"\")[1]";
	}
 	return  " ";
 }

}
