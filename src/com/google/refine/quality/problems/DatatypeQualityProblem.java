package com.google.refine.quality.problems;





import java.net.URI;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

public class DatatypeQualityProblem  extends QualityProblem{
	
	private String expectedDatatype;
	private String currentDatatype;

	public DatatypeQualityProblem(Integer rowIndex, Quad quad,
			Resource qualityReport) {
		super(rowIndex, quad, qualityReport);
		// TODO Auto-generated constructor stub
	}
	
	public void setDatatypes(URI expected, URI available){
		this.expectedDatatype = expected.getFragment();
		this.currentDatatype = available.getFragment();
	}
	
	@Override
 public String getProblemDescription(){
	    	return "Expected:" + expectedDatatype + " available:" + currentDatatype;
	  }
	    
 public String getCleaningSuggestion(){
	    	return "Convert literal to " + expectedDatatype;
	    }
 
 

 
 @Override
 public String getGrelExpression(){
	 if (expectedDatatype.endsWith("dateTime")) {
		return  "split(value, \"\\\"\")[0].toDate()+ replace(split(value,  \"\\\"\")[1],"+ "\""+currentDatatype+ "\", "+ "\""+expectedDatatype+ "\")" ;
	 }else if (expectedDatatype.endsWith("int")) {
		 return  "split(value, \"\\\"\")[0].toNumber()+ replace(split(value,  \"\\\"\")[1],"+ "\""+currentDatatype+ "\", "+ "\""+expectedDatatype+ "\")" ;
	}
 	return  " ";
 }
 


}
