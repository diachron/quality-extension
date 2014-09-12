package com.google.refine.quality.metrics;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;







import org.apache.log4j.Logger;

import com.google.refine.quality.problems.DatatypeQualityProblem;
import com.google.refine.quality.utilities.LoadQualityReportModel;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;
import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Detects incompatible data type of literals by comparing its Data Type URI with the
 * Data Type URI specified in the range of the Object's predicate
 * 
 * @author Muhammad Ali Qasmi
 * @date 21st June 2014 
 */
public class IncompatibleDatatypeRange extends AbstractQualityMetrics{
        /**
         * Description of quality report 
         */
        protected Resource qualityReport  = QPROB.IncompatibleDatatypeRangeProblem;
        /**
         * logger object
         */
        static Logger logger = Logger.getLogger(IncompatibleDatatypeRange.class);
        /**
         * cache frequently used Properties
         */
        static Map<String, Statement> cacheProperty = new HashMap<String, Statement>();
        /**
         * total number of literals
         */
        private double totalLiterals = 0;
        /**
         * total number of incompatiable data type literals.
         */
        private double incompatiableDataTypeLiterals = 0;
        /**
         * Clears Property Cache
         */
        public static void clearCache() {
            cacheProperty.clear();
        }
        /**
         * Validates data type of literal by comparing its Data Type URI with the
         * Data Type URI specified in the range of the Object's predicate
         * 
         * @param literalDateTypeURI
         * @param RangeDataTypeURI
         * @return true - if validated
         */
        protected boolean checkTypeByComparingURI(URI literalDataTypeURI,
                URI rangeReferredURI) {

            // case: literDataTyprURI NOT null but RangeReferredURI is null
            if (literalDataTypeURI != null && rangeReferredURI == null) {
                logger.warn("literalDataTypeURI is NOT null but RangeReferredURI is null.");
                return true;
            }
            // case: literDataType is NUll and RangeRefferedURI is a literal
            else if (literalDataTypeURI == null
                    && rangeReferredURI.getFragment().toLowerCase()
                            .equals("literal")) {
                return true;
            }
            // case: literalDataTypeURI is null
            else if (literalDataTypeURI == null) {
                logger.info("literDataTypeURI is null.");
                return true;
            }
            // case: rangeReferredURI is null
            else if (rangeReferredURI == null) {
                logger.warn("RangeReferredURI is null.");
                return true;
            }
            // case: Both are EQUAL
            else if (literalDataTypeURI.equals(rangeReferredURI)) {
                return true;
            } else {
                return false;
            }
        }
        
        /**
         * Computes whether a given quad is incompatible data type literal or not
         * 
         * @param quad
         *            - to be processed
         */
        @Override
        public void compute(Integer index, Quad quad) {
            logger.trace("compute() --Started--");
            try {
                // retrieve predicate
                Node predicate = quad.getPredicate();
                // retrieve object
                Node object = quad.getObject();

                // check if predicate and object are NOT null
                if (predicate != null && object != null) {

                    logger.debug("Processing for object ::" + object.toString());

                    if (object.isLiteral()) { // check if the object is literal

                        this.totalLiterals++; // increment total number of literals

                        // retrieve predicate URI
                        if (predicate.getURI() != null) {

                            Statement tmpProperty = null;

                            // check if the property is present in cache
                            if (cacheProperty.containsKey(predicate.getURI())) {
                                logger.debug(predicate.getURI()
                                        + " :: found in cache.");
                                tmpProperty = cacheProperty.get(predicate.getURI());
                            } else { // load property from given URI source
                                logger.debug("predicate vocabulary not found in cache.");
                                logger.debug("loading vocabulary for predicate from :: "
                                        + predicate.getURI());
                                Model tmpModel = VocabularyReader.read(predicate.getURI()); // load
                                                                                        // vocabulary
                                                                                        // from
                                                                                        // the
                                                                                        // URI
                                tmpProperty = (tmpModel != null) ? (tmpModel
                                        .getResource(predicate.getURI()))
                                        .getProperty(RDFS.range) : null;
                            }

                            // check if property is not empty
                            if (tmpProperty != null) {
                                // store new statement in cache
                                if (!cacheProperty.containsKey(predicate.getURI())) {
                                    cacheProperty.put(predicate.getURI(),
                                            tmpProperty);
                                }

                                Triple triple = tmpProperty.asTriple();

                                String predicateURI = predicate.getURI().toString(); // given
                                                                                        // predicate
                                String subject = triple.getSubject().toString(); // retrieved
                                                                                    // predicate

                                // check if retrieved predicate matches with the
                                // given predicate
                                if (subject.equals(predicateURI)) {

                                    logger.debug("Object DataType URI :: "
                                            + object.getLiteralDatatypeURI());
                                    logger.debug("Range Referred DateType URI :: "
                                            + triple.getObject());

                                    try {

                                        URI givenObjectDateTypeURI = (object
                                                .getLiteralDatatypeURI() != null) ? new URI(
                                                object.getLiteralDatatypeURI())
                                                : null;
                                        URI rangeObjectURI = (triple.getObject()
                                                .toString() != null) ? new URI(
                                                triple.getObject().toString())
                                                : null;
                                        if (!checkTypeByComparingURI(
                                                givenObjectDateTypeURI,
                                                rangeObjectURI)) {
                                            this.incompatiableDataTypeLiterals++;
                                            DatatypeQualityProblem reportProblems = new DatatypeQualityProblem(index, quad, qualityReport);
                                            reportProblems.setDatatypes(rangeObjectURI, givenObjectDateTypeURI);
                                            this.problemList.add(reportProblems);
                                        }
                                    } catch (URISyntaxException e) {
                                        logger.error("Malformed URI exception for "
                                                + e.getMessage());
                                        logger.debug(e.getStackTrace());
                                    }

                                } // End-if (subject.equals(predicateURI))

                            } // End-if (tmpProperty != null)

                        } // End-if (predicate.getURI() != null)

                    } // End-if (object.isLiteral())
                }
            } catch (Exception exception) {
                logger.debug(exception);
                logger.error(exception.getMessage());
            }
            logger.trace("compute() --Ended--");
        }
        
        /**
         * Returns value of the metric based on
         * 
         * @return ( number of incompatible Data type literals ) / ( total number of
         *         Literals )
         */
        @Override
        public double metricValue() {
            logger.trace("metricValue() --Started--");
            logger.debug("Incompatiable DataType Literals :: "
                    + this.incompatiableDataTypeLiterals);
            logger.debug("Total Literals :: " + this.totalLiterals);

            // return ZERO if total number of RDF literals are ZERO [WARN]
            if (this.totalLiterals <= 0) {
                logger.warn("Total number of RDF data type literals in given document is found to be zero.");
                return 0.0;
            }

            double metricValue = this.incompatiableDataTypeLiterals
                    / this.totalLiterals;
            logger.debug("Metric Value :: " + metricValue);
            logger.trace("metricValue() --Ended--");
            return metricValue;
        }
}
