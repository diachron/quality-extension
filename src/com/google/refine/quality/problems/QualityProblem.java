package com.google.refine.quality.problems;

import com.google.refine.quality.utilities.LoadQualityReportModel;
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
    
    public String getProblemDescription(){
    	return LoadQualityReportModel.getResourcePropertyValue(this.problemtURI, QPROB.problemDescription);
    }
    
    public String getCleaningSuggestion(){
    	return LoadQualityReportModel.getResourcePropertyValue(this.problemtURI, QPROB.cleaningSuggestion);
    }
    
    public String getProblemName(){
    	return  LoadQualityReportModel.getResourcePropertyValue(this.problemtURI, RDFS.label);
    }
    
    public String getGrelExpression(){
    	return  LoadQualityReportModel.getResourcePropertyValue(this.problemtURI, QPROB.qrefineRule);
    }
   
   
    
    /**
     * Default Constructor
     * @param rowIndex
     * @param quad
     * @param problemType
     * @param sourceMetric
     */
    public QualityProblem(Integer rowIndex, Quad quad, Resource qualityReport) {
        this.rowIndex = rowIndex;
        this.quad = quad;
        this.problemtURI = qualityReport;
    }
}
