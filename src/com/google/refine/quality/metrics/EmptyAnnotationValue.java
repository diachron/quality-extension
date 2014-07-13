package com.google.refine.quality.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xerces.util.URI;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * EmptyAnnotationValue consider the following widely used annotation 
 * properties (labels, comments, notes, etc.) and identifies triples 
 * whose property is from a pre-configured list of annotation properties, 
 * and whose object is an empty string.
 *
 * @author Muhammad Ali Qasmi
 * @date 19th June 2014
 */
public class EmptyAnnotationValue extends AbstractQualityMetrics{
        /**
         * logger static object
         */
        static Logger logger = Logger.getLogger(EmptyAnnotationValue.class);
        /**
         * Default file path and name for the file that contains
         * list of annotation properties
         */
        protected static String defaultFilePathName = "extensions/quality-extension/resources/AnnotationPropertiesList";
        /**
         * Number of literals count
         */
        protected long totalNumberOfLiterals = 0;
        /**
         * Number of empty literals count
         */
        protected long totalNumberOfEmptyLiterals = 0;
        /**
         * list of annotation properties to be evaluated.        
         */
        protected static Set<String> annotationPropertiesSet = new HashSet<String>();
        
        /**
         * loads list of annotation properties in set
         * 
         * if filePathName is null then default path will be used.
         * 
         * @param filePathName - file Path and name, default : src/main/resources/AnnotationPropertiesList.txt 
         */
        public static void loadAnnotationPropertiesSet(String filePathName) {
                File file = null;
                String tmpFilePathName = (filePathName == null) ? EmptyAnnotationValue.defaultFilePathName : filePathName;
                
                try {
                    if (!tmpFilePathName.isEmpty()){
                            file = new File(tmpFilePathName);
                            if (file.exists() && file.isFile()){
                                String strLine = null;
                                BufferedReader in = new BufferedReader(new FileReader(file));
                                while ((strLine = in.readLine()) != null) {
                                        URI tmpURI = new URI(strLine);
                                        if (tmpURI != null) {
                                                EmptyAnnotationValue.annotationPropertiesSet.add(strLine);
                                        }
                                      }
                               in.close();    
                            }
                    }
                } catch (FileNotFoundException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                } catch (IOException e) {
                        logger.debug(e.getStackTrace());
                        logger.error(e.getMessage());
                }
        }
        
        /**
         * clears list of annotation properties in set
         */
        public static void clearAnnotationPropertiesSet() {
                EmptyAnnotationValue.annotationPropertiesSet.clear();
        }
        
        /**
         * Checks whether given quad has predicate with URI found in annotation properties set
         * if true then checks the object's value in that quad; whether it is empty or not.
         *    
         */
        @Override
        public void compute(Quad quad) {
            try {
                Node predicate = quad.getPredicate();
                if (predicate.isURI()){ // check is the predicate is URI or not
                    if (EmptyAnnotationValue.annotationPropertiesSet.contains(predicate.getURI())){ // check if given predicate is found in annotation properties list
                            boolean isEmptyLiteral = false; // set empty literal to false
                            Node object = quad.getObject();
                            this.totalNumberOfLiterals++; // increment total number of literals
                            if (object.isBlank()) { // check blank object
                                    isEmptyLiteral = true; // set empty literal to true
                            }
                            else if (object.isLiteral()){ // check whether object is literal or not
                                    String  value = object.getLiteralValue().toString(); // retrieve object's value
                                    value = value.trim(); // removes whitespace from both ends
                                    if (value == null) { // check if object's value is null or not
                                            isEmptyLiteral = true; // set empty literal to true
                                    } else if (value.isEmpty()){ // check is object's value is empty 
                                            isEmptyLiteral = true; // set empty literal to true
                                    }
                            } else {
                                    isEmptyLiteral = true;
                            }
                            
                            if (isEmptyLiteral) {
                                    this.totalNumberOfEmptyLiterals++; // increment empty literal count
                                    this.problemList.add(quad); // add invalid quad in problem list
                            }
                    }
                }
            }  catch (Exception e){
                logger.debug(e.getStackTrace());
                logger.debug(e.getMessage());
            }
        }
        
        /**
         * metric value  = total number of empty literals / total number of literals
         * 
         * @return ( (total number of empty literals) / (total number of literals) )
         */
        @Override
        public double metricValue() {
                logger.debug("Total number of empty literals : " + this.totalNumberOfEmptyLiterals);
                logger.debug("Total total number of literals : " + this.totalNumberOfLiterals);
                if (this.totalNumberOfLiterals <= 0) {
                        logger.warn("Total total number of literals are ZERO");
                        return 0;
                }
                return ((double) this.totalNumberOfEmptyLiterals / (double) this.totalNumberOfLiterals);
        }
}
