package com.google.refine.quality.metrics;


import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.QualityReportModelLoader;
import com.google.refine.quality.utilities.VocabularyReader;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 *  * TODO refactor
 * The Ontology Hijacking detects the redefinition by analyzing defined classes or 
 * properties in data set and looks of same definition in its respective vocabulary. 
 * 
 * Note: This class uses utilities.VocabularyReader to download models (vocabularies) from
 * web. Though VocabularyReader has it own cache but it has some inherit performance/scalability issues.   
 *  
 * @author Muhammad Ali Qasmi
 * @date 10th June 2014
 * 
 */
public class OntologyHijacking extends AbstractQualityMetric {
        /**
         * Description of quality report 
         */
        protected Resource qualityReport  = null;
        /**
         * logger static object
         */
        static Logger logger = Logger.getLogger(OntologyHijacking.class);
        /**
         * total number of locally defined classes or properties count 
         */
        protected double totalLocallyDefinedClassesOrPropertiesCount = 0;
        /**
         * total number of hijacked classes or properties found 
         */
        @Override
        public void before(Object... args) {}
        
        @Override
        public void after() {}
        protected double hijackedClassesOrPropertiesCount = 0;
        /**
         * Check if the given quad has predicate of URI with given fragment
         * 
         * @param predicate
         * @param fragment
         * @return true if predicate is URI with given fragment
         */
        protected boolean isDefinedClassOrProperty(Quad quad, String fragment){
                try {
                       if (quad.getPredicate().isURI()) { // predicate is a URI
                           URI tmpURI = new URI(quad.getPredicate().getURI());
                           return (tmpURI.getFragment() != null && tmpURI.getFragment().toLowerCase().equals(fragment)) ?  true : false;
                       }
                       
                } catch (MalformedURIException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                }
                return false;
        }
        
        /**
         * Detects if given node is defined in vocabulary or not
         * 
         * @param node
         * @return true - if given node is found in the vocabulary with property of RDF.type
         */
        protected boolean isHijacked(Node node){
                Model model = VocabularyReader.read(node.getURI());
                if (model != null){
                        if (model.getResource(node.getURI()).isURIResource()){
                                if ( model.getResource(node.getURI()).hasProperty(RDF.type)) {
                                     return true;
                                }
                        }
                }
                return false;
        }
        
        /**
         * Filters quad triples that are locally defined in the data set.
         * Detects if filtered triples are already defined in vocabulary
         */
        @Override
        public void compute(Integer index, Quad quad) {
                try {
                    if (isDefinedClassOrProperty(quad, "type")){ // quad represent a locally defined statement
                            this.totalLocallyDefinedClassesOrPropertiesCount++; // increments defined class or property count
                            Node subject = quad.getSubject(); // retrieve subject
                            if (isHijacked(subject)){ 
                                    this.hijackedClassesOrPropertiesCount++; // increments redefined class or property count
                                    QualityProblem reportProblems = new QualityProblem(index, quad, qualityReport);
                                    this.problemList.add(reportProblems);
                            }
                    }
                    else if (isDefinedClassOrProperty(quad, "domain")){ // quad represent a locally defined statement
                            this.totalLocallyDefinedClassesOrPropertiesCount++; // increments defined class or property count
                            Node object = quad.getObject(); // retrieve object
                            if (isHijacked(object)){ 
                                    this.hijackedClassesOrPropertiesCount++; // increments redefined class or property count
                                    QualityProblem reportProblems = new QualityProblem(index, quad, qualityReport);
                                    this.problemList.add(reportProblems);
                            }
                    }
                }
                catch (Exception e) {
                     logger.debug(e.getStackTrace());
                     logger.error(e.getMessage());
                }
        }
        
        /**
         * Returns metric value for between 0 to 1. Where 0 as the best case and 1 as worst case 
         * @return double - range [0 - 1] 
         */
        @Override
        public double metricValue() {
                if (this.totalLocallyDefinedClassesOrPropertiesCount <= 0) {
                        logger.warn("Total classes or properties count is ZERO");
                        return 0;
                }
                return (this.hijackedClassesOrPropertiesCount / this.totalLocallyDefinedClassesOrPropertiesCount);
        }
}
