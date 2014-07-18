package com.google.refine.quality.metrics;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Contains information about problematic quads.
 * 
 * @author Muhammad
 *
 */
public class ReportProblems {
    
    protected Integer rowIndex;
    protected Quad quad;
    protected Resource qualityReport;
    
    public Integer getRowIndex() {
        return rowIndex;
    }

    public Quad getQuad() {
        return quad;
    }
    
    public Resource getQualityReport() {
        return qualityReport;
    }
    
    /**
     * Default Constructor
     * @param rowIndex
     * @param quad
     * @param problemType
     * @param sourceMetric
     */
    public ReportProblems(Integer rowIndex, Quad quad, Resource qualityReport) {
        this.rowIndex = rowIndex;
        this.quad = quad;
        this.qualityReport = qualityReport;
    }
}
