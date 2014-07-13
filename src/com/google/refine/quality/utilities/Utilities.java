package com.google.refine.quality.utilities;

import java.io.PrintStream;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;


public final class Utilities {

    /**
     * Prints quad to stream in readable format
     * @param quad
     * @param printStream
     */
    public static void printQuad(Quad quad, PrintStream printStream) {
        try {
            
        if (null == quad) throw new IllegalArgumentException("Quad cannot be null.");
        if (null == printStream) printStream = System.out;
        
        printStream.print("Subject : " + quad.getSubject());
        printStream.print("-- Predicate : " + quad.getPredicate());
        printStream.println("--> Object : " + quad.getObject());
        
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Prints statement in readable format
     * 
     * @param statement
     * @param printStream
     */
    public static void printStatment(Statement statement, PrintStream printStream) {
        try {
            
            if (null == statement) throw new IllegalArgumentException("Statement cannot be null.");
            if (null == printStream) printStream = System.out;
            
            Resource  subject   = statement.getSubject();     // get the subject
            Property  predicate = statement.getPredicate();   // get the predicate
            RDFNode   object    = statement.getObject();      // get the object

            printStream.print(subject.toString());
            printStream.print(" " + predicate.toString() + " ");
            
            if (object instanceof Resource) {
                printStream.print(object.toString());
            } else {
                // object is a literal
                printStream.print(" \"" + object.toString() + "\"");
            }

            printStream.println(" .");
            
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Prints statements in readable format
     * 
     * @param stmtIterator
     * @param printStream
     */
    public static void printStatements(StmtIterator stmtIterator, PrintStream printStream){
     
        try {
            
            if (null == stmtIterator) throw new IllegalArgumentException("StmtIterator cannot be null.");
            if (null == printStream) printStream = System.out;
            
            while (stmtIterator.hasNext()){
                printStatment(stmtIterator.nextStatement(), printStream);
            }
            
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    
}
