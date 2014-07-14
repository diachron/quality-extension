package com.google.refine.quality.metrics;

import org.apache.log4j.Logger;

import com.google.refine.quality.utilities.ProcessProblemProperties;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Detects whether the value of a typed literal is valid with respect to its
 * given xsd datatype.
 * 
 * @author Muhammad Ali Qasmi
 * @date 13th Feb 2014
 */
public class MalformedDatatypeLiterals extends AbstractQualityMetrics{
	/**
	 * logger static object
	 */
	private static Logger logger = Logger
			.getLogger(MalformedDatatypeLiterals.class);
	/**
	 * total number of literals
	 */
	private double totalLiterals = 0;
	/**
	 * total number of malformed literals
	 */
	private double malformedLiterals = 0;
	/**
	 * This method identify whether a given quad is malformed or not.
	 * 
	 * @param quad
	 *            - to be identified
	 */
	@Override
	public void compute(Integer index, Quad quad) {
        
    		logger.trace("compute() --Started--");
    		// retrieves object from statement
    		Node object = quad.getObject();
    		// checks if object is a literal
    		if (object.isLiteral()) {
    			// retrieves rdfDataType from literal
    			RDFDatatype rdfdataType = object.getLiteralDatatype();
    			// check if rdf data type is a valid data type
    			if (rdfdataType != null) {
    				logger.debug("RdfDataTypeLiteral :: " + object.toString());
    				if (!rdfdataType.isValidLiteral(object.getLiteral())) {
    					this.malformedLiterals++;
    					ReportProblems reportProblems = new ReportProblems(index, quad, ProcessProblemProperties.getProblemMessage(this.getClass().getName()), this.getClass().getName());
    					this.problemList.add(reportProblems);
    					logger.debug("MalformedRDFDataTypeLiteral :: "
    							+ object.toString());
    				}
    				this.totalLiterals++;
    			}
    			logger.debug("Literal :: " + object.toString());
    		}
    		logger.debug("Object :: " + object.toString());
		logger.trace("compute() --Ended--");
	}

	/**
	 * Returns metric value for object this class
	 * 
	 * @return (number of malformed literals) / (total number of literals)
	 */
	@Override
	public double metricValue() {

		logger.trace("metricValue() --Started--");
		logger.debug("Malformed Literals :: " + this.malformedLiterals);
		logger.debug("Total Literals :: " + this.totalLiterals);

		// return ZERO if total number of RDF literals are ZERO [WARN]
		if (0 >= this.totalLiterals) {
			logger.warn("Total number of RDF data type literals in given document is found to be zero.");
			return 0.0;
		}

		double metricValue = this.malformedLiterals / this.totalLiterals;
		logger.debug("Metric Value :: " + metricValue);
		logger.trace("metricValue() --Ended--");
		return metricValue;
	}
	
}