package com.google.refine.quality.metrics;

import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Contains information about problematic quads.
 * 
 * @author Muhammad
 *
 */
public class ReportProblems {
    protected Integer _rowIndex;
    
    public Integer get_rowIndex() {
        return _rowIndex;
    }

    
    public Quad get_quad() {
        return _quad;
    }

    
    public String get_problemType() {
        return _problemType;
    }

    
    public String get_sourceMetric() {
        return _sourceMetric;
    }

    protected Quad _quad;
    protected String  _problemType;
    protected String  _sourceMetric;
    
    /**
     * Default Constructor
     * @param rowIndex
     * @param quad
     * @param problemType
     * @param sourceMetric
     */
    public ReportProblems(Integer rowIndex, Quad quad, String problemType, String sourceMetric) {
        this._rowIndex = rowIndex;
        this._quad = quad;
        this._problemType = problemType;
        this._sourceMetric = sourceMetric;
    }
}
