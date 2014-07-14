package com.google.refine.quality.metrics;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.sparql.core.Quad;

public class HelloWorldMetrics extends AbstractQualityMetrics{
    /**
     * logger static object
     */
    private static Logger logger = Logger
                    .getLogger(HelloWorldMetrics.class);
    /**
     * total number of literals
     */
    private double totalLiterals = 0;
    /**
     * total number of malformed literals
     */
    private double helloWorldLiterals = 0;
    /**
     * This method does nothing. Its a test metrics
     * 
     * @param quad
     *            - to be identified
     */
    @Override
    public void compute(Integer index, Quad quad) {
    
            logger.trace("compute() --Started--");
            this.totalLiterals++;
            this.helloWorldLiterals++;
            ReportProblems reportProblems = new ReportProblems(index, quad, "hello world problem", "hello world metric");
            this.problemList.add(reportProblems);
            logger.trace("compute() --Ended--");
    }

    /**
     * Returns metric value for object this class
     * 
     * @return (number of hello world literals) / (total number of literals)
     */
    @Override
    public double metricValue() {

            logger.trace("metricValue() --Started--");
            logger.debug("Hello World Literals :: " + this.helloWorldLiterals);
            logger.debug("Total Literals :: " + this.totalLiterals);

            // return ZERO if total number of RDF literals are ZERO [WARN]
            if (0 >= this.totalLiterals) {
                    logger.warn("Total number of RDF data type literals in given document is found to be zero.");
                    return 0.0;
            }

            double metricValue = this.helloWorldLiterals / this.totalLiterals;
            logger.debug("Metric Value :: " + metricValue);
            logger.trace("metricValue() --Ended--");
            return metricValue;
    }
}
