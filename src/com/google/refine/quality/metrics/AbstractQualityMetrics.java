package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import com.google.refine.quality.problems.QualityProblem;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;


public abstract class AbstractQualityMetrics {
       /**
        * Description of quality report 
        */
       protected Resource qualityReport  = null;   
       /**
        * list of problematic quad
        */
        protected List<QualityProblem> problemList = new ArrayList<QualityProblem>();
       /**
        * Returns value of the metric
        * 
        * @return 
        */
       public double metricValue() { 
               return -1;
       }
        /**
         * Computes metric Value
         * 
         * @param quad
         */
        public void compute(Integer index, Quad quad) { }
        /**
         * Computes metric Value
         * 
         * @param list of quad
         */
        public void compute (List<Quad> listQuad) {
                Integer index = 0;
                for(Quad quad : listQuad){
                    this.compute(index++, quad);
                }
        }
        /**
         * Returns list of problematic Quads
         * 
         * @return list of problematic quads
         */
        public List<QualityProblem> getQualityProblems() {
            List<QualityProblem> tmpProblemList = null;
            tmpProblemList = new ArrayList<QualityProblem>(this.problemList);
            return tmpProblemList;
        }
}
