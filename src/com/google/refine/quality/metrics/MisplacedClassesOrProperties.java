package com.google.refine.quality.metrics;


import org.apache.log4j.Logger;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QPROB;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 *  * TODO refactor
 * This metric should find resources that are - defined as a property but also
 * appear on subject or object positions in other triples (except cases like
 * ex:prop rdf:type rdfs:Property, ex:prop rds:subPropetyOf) - defined as a
 * class but also appear on predicate position in other triples. The metric is
 * computed as a ratio of misplaced classes and properties
 * 
 * @author Muhammad Ali Qasmi
 * @date 13th March 2014
 */
public class MisplacedClassesOrProperties extends AbstractQualityMetric {
        /**
         * Description of quality report 
         */
        protected Resource misplacedClass  = QPROB.MisplacedClassProblem;
        protected Resource misplacedProperty  = QPROB.MisplacedPropertyProblem;
        /**
         * static logger object
         */
        static Logger logger = Logger.getLogger(MisplacedClassesOrProperties.class);
        /**
         * total number of misplaces classes
         */
        protected long misplacedClassesCount = 0;
        /**
         * total number of classes
         */
        protected long totalClassesCount = 0;
        /**
         * total number of misplaces properties
         */
        protected long misplacedPropertiesCount = 0;
        /**
         * total number properties
         */
        protected long totalPropertiesCount = 0;
        
        /**
         * This method identifies whether a given quad is a misplaced class or a
         * misplaced property.
         * 
         * @param quad
         *            - to be identified
         */
        @Override
        public void before(Object... args) {}
        @Override
        public void after() {}
        @Override
        public void compute(Quad quad) {
            logger.trace("compute() --Started--");

            try {

                Node subject = quad.getSubject(); // retrieve subject
                Node predicate = quad.getPredicate(); // retrieve predicate
                Node object = quad.getObject(); // retrieve object

                if (subject.isURI()) { // check if subject is URI (not Blank)
                    this.totalClassesCount++;
                    // load model
                    Model subjectModel = VocabularyReader.read(subject.getURI());
                    if (!subjectModel.isEmpty()) { // check if system is able to
                                                // retrieve model
                        // search for URI resource from Model
                        if (subjectModel.getResource(subject.getURI())
                                .isURIResource()) {
                            // search for its domain and range properties
                            // if it has one then it is a property not class.
                            if (subjectModel.getResource(subject.getURI())
                                    .hasProperty(RDFS.domain)
                                    || subjectModel.getResource(subject.getURI())
                                            .hasProperty(RDFS.range)) {
                                logger.debug("Misplace Class Found in Subject::"
                                        + subject);
                                this.misplacedClassesCount++;
                                QualityProblem reportProblems = new QualityProblem(quad, misplacedClass);
                                problems.add(reportProblems);
                            }
                        }
                    }
                }

                if (predicate.isURI()) { // check if predicate is URI
                    this.totalPropertiesCount++;
                    // load model
                    Model predicateModel = VocabularyReader
                            .read(predicate.getURI());
                    if (!predicateModel.isEmpty()) { // check if system is able to
                                                    // retrieve model
                        // search for URI resource from Model
                        if (predicateModel.getResource(predicate.getURI())
                                .isURIResource()) {
                            // search for its domain and range properties
                            // if it does NOT have some domain and range than its
                            // NOT a property
                            if (!(predicateModel.getResource(predicate.getURI())
                                    .hasProperty(RDFS.domain) && predicateModel
                                    .getResource(predicate.getURI()).hasProperty(
                                            RDFS.range))) {
                                logger.debug("Misplace Property Found in Predicate ::"
                                        + predicate);
                                this.misplacedPropertiesCount++;
                                QualityProblem reportProblems = new QualityProblem(quad, misplacedClass);
                                problems.add(reportProblems);
                            }
                        }
                    }
                }

                if (object.isURI()) { // check if object is URI (not blank or
                                        // literal)
                    this.totalClassesCount++;
                    // load model
                    Model objectModel = VocabularyReader.read(object.getURI());
                    if (!objectModel.isEmpty()) { // check if system is able to
                                                // retrieve model
                        // search for URI resource from Model
                        if (objectModel.getResource(object.getURI())
                                .isURIResource()) {
                            // search for its domain and range properties
                            // if it has one then it is a property not class.
                            if (objectModel.getResource(object.getURI())
                                    .hasProperty(RDFS.domain)
                                    || objectModel.getResource(object.getURI())
                                            .hasProperty(RDFS.range)) {
                                logger.debug("Misplace Class Found in Object ::"
                                        + object);
                                this.misplacedClassesCount++;
                                QualityProblem reportProblems = new QualityProblem(quad, misplacedClass);
                                problems.add(reportProblems);
                            }
                        }
                    }
                }

            } catch (Exception exception) {
                logger.debug(exception);
                logger.error(exception.getMessage());
            }

            logger.trace("compute() --Ended--");
        }
        
        /**
         * This method computes metric value for the object of this class.
         * 
         * @return (total number of undefined classes or properties) / (total number
         *         of classes or properties)
         */
        @Override
        public double metricValue() {
            logger.trace("metricValue() --Started--");
            logger.debug("Number of Misplaced Classes :: "
                    + this.misplacedClassesCount);
            logger.debug("Number of Classes :: " + this.totalClassesCount);
            logger.debug("Number of Misplaced Properties :: "
                    + this.misplacedPropertiesCount);
            logger.debug("Number of Properties :: " + this.totalPropertiesCount);

            long tmpTotalUndefinedClassesAndUndefinedProperties = this.misplacedClassesCount
                    + this.misplacedPropertiesCount;
            long tmpTotalClassesAndProperties = this.totalClassesCount
                    + this.totalPropertiesCount;
            // return ZERO if total number of RDF literals are ZERO [WARN]
            if (tmpTotalClassesAndProperties <= 0) {
                logger.warn("Total number of classes and properties in given document is found to be zero.");
                return 0.0;
            }

            double metricValue = (double) tmpTotalUndefinedClassesAndUndefinedProperties
                    / tmpTotalClassesAndProperties;
            logger.debug("Metric Value :: " + metricValue);
            logger.trace("metricValue() --Ended--");
            return metricValue;
        }

}
