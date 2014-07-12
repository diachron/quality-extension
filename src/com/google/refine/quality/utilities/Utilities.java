package com.google.refine.quality.utilities;

import java.io.PrintStream;

import com.hp.hpl.jena.sparql.core.Quad;


public final class Utilities {

    /**
     * Prints quad to stream in read able format
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
    
}
