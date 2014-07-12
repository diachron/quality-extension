package com.google.refine.quality.metrics;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Quad;


public abstract class AbstractQualityMetrics {
       
       /**
        * list of problematic quad
        */
        protected List<Quad> problemList = new ArrayList<Quad>();
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
        public void compute(Quad quad) { }
        /**
         * Computes metric Value
         * 
         * @param list of quad
         */
        public void compute (List<Quad> listQuad) {
                for(Quad quad : listQuad){
                    this.compute(quad);
                }
        }
        /**
         * Returns list of problematic Quads
         * 
         * @return list of problematic quads
         */
        public List<Quad> getQualityProblems() {
            List<Quad> tmpProblemList = null;
            tmpProblemList = new ArrayList<Quad>(this.problemList);
            return tmpProblemList;
        }
}
