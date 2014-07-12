package com.google.refine.quality.utilities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;


public class LoadJenaModel {
        
        private LoadJenaModel(){ }
        
        /**
         * Creates a new model and load data from input stream 
         * @param in - input stream object
         * @return
         */
        public static Model getModel(InputStream in){
                Model model = ModelFactory.createDefaultModel();
                model.read(in,null,"TURTLE");
                return model;
        }
        
        /**
         * Creates a new model, loads data from input stream and returns
         * list of Quads 
         * @param in - input steam
         * @return
         */
        public static List<Quad> getQuads(InputStream in){
                Model model = getModel(in);
                List<Quad> tmpQuads = new ArrayList<Quad>();
                StmtIterator si = model.listStatements();
                while(si.hasNext()){
                        tmpQuads.add(new Quad(null, si.next().asTriple()));
                }
                
                return tmpQuads;
        }
        
}
