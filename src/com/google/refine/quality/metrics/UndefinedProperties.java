package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.google.refine.quality.utilities.LoadQualityReportModel;
import com.google.refine.quality.utilities.VocabularyReader;
import com.google.refine.quality.vocabularies.QR;

public class UndefinedProperties extends AbstractQualityMetrics {
                /**
                 * Description of quality report 
                 */
                protected Resource qualityReport  = QR.UndefinedPropertiesProblem;
		/**
		 * static logger object
		 */
		static Logger logger = Logger.getLogger(UndefinedClasses.class);
		
		/**
		 * total number of undefined properties
		 */
		protected long undefinedPropertiesCount = 0;
		/**
		 * total number of properties
		 */
		protected long totalPropertiesCount = 0;

		/**
		 * This method identifies whether a component (subject, predicate or object)
		 * of the given quad references an undefined class or property.
		 * 
		 * @param quad
		 *            - to be identified
		 */
		@Override
		public void compute(Integer index, Quad quad) {

			logger.trace("compute() --Started--");

			try {
				Node predicate = quad.getPredicate(); // retrieve predicate
				Node object = quad.getObject(); // retrieve object
				
				if (predicate.isURI()) { // check if predicate is URI
					this.totalPropertiesCount++;
					// load model
					Model predicateModel = VocabularyReader
							.read(predicate.getURI());
					if (predicateModel == null) { // check if system is able to
													// retrieve model
	                        this.undefinedPropertiesCount++;
	                        ReportProblems reportProblems = new ReportProblems(index, quad, qualityReport);
	                        this.problemList.add(reportProblems);
					} else {
						// search for URI resource from Model
						if (predicateModel.getResource(predicate.getURI())
								.isURIResource()) {
							// search for its domain and range properties
							if (!(predicateModel.getResource(predicate.getURI())
									.hasProperty(RDFS.domain) && predicateModel
									.getResource(predicate.getURI()).hasProperty(
											RDFS.range))) {
						        System.out.println("predicate : " + predicate);    
								this.undefinedPropertiesCount++;
								ReportProblems reportProblems = new ReportProblems(index, quad, qualityReport);
								this.problemList.add(reportProblems);
							}
						}
					}
					
					URI tmpURI = new URI(predicate.getURI());
					

	               if (tmpURI != null && (tmpURI.equals(RDFS.subPropertyOf.toString())||
	            		   tmpURI.equals(OWL.onProperty.toString())||
	            		   tmpURI.equals(OWL.equivalentProperty.toString())||
	            		   tmpURI.equals(OWL.NS + "propertyDisjointWith")||
	            		   tmpURI.equals(OWL.NS + "assertionProperty"))            		   
	            		   ) {
	                    if (object.isURI()) { // check if object is URI (not blank or
	                            // literal)
	                        this.totalPropertiesCount++;
	                        // load model
	                        Model objectModel = VocabularyReader.read(object.getURI());
	                        if (objectModel == null) { // check if system is able to
	                                                    // retrieve model
	                                this.undefinedPropertiesCount++;
	                                ReportProblems reportProblems = new ReportProblems(index, quad, qualityReport);
	                                this.problemList.add(reportProblems);
	                        } else {
	                             // search for URI resource from Model
	                                if (!objectModel.getResource(object.getURI())
	                                                .isURIResource()) {
	                                    this.undefinedPropertiesCount++;
	                                    ReportProblems reportProblems = new ReportProblems(index, quad, qualityReport);
	                                    this.problemList.add(reportProblems);
	                                }      
	                        }      
	                    }
	                }
				}

			} catch (MalformedURIException exception){
		        logger.debug(exception);
	            logger.error(exception.getMessage());
			} catch (Exception exception) {
				logger.debug(exception);
				logger.error(exception.getMessage());
			}

			logger.trace("compute() --Ended--");
		}

		/**
		 * This method returns metric value for the object of this class
		 * 
		 * @return (total number of undefined classes and undefined properties) / (
		 *         total number of classes and properties)
		 */
		@Override
		public double metricValue() {

			logger.trace("metricValue() --Started--");
			
			
			logger.debug("Number of Undefined Properties :: "
					+ this.undefinedPropertiesCount);
			logger.debug("Number of Properties :: " + this.totalPropertiesCount);		
			if (this.totalPropertiesCount <= 0) {
				logger.warn("Total number of properties in given document is found to be zero.");
				return 0.0;
			}
			double metricValue = (double) this.undefinedPropertiesCount
					/ this.totalPropertiesCount;
			logger.debug("Metric Value :: " + metricValue);
			logger.trace("metricValue() --Ended--");
			return metricValue;
		}
	}



