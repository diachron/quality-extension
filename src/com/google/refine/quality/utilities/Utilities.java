package com.google.refine.quality.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

import com.google.refine.model.Cell;
import com.google.refine.model.Project;


public final class Utilities {

  /**
   * Retrieves data from project and writes it to an InputStream.
   * @param project OpenRefine project.
   * @return InputStream containing an OpenRefine project.
   * @throws IOException when project can not be written to an InputStream.
   */
  public static InputStream projectToInputStream(Project project) throws IOException {
    StringBuilder tmp = new StringBuilder();
    for (int i = 0; i < project.rows.size(); i++) {
      Cell cell = project.rows.get(i).getCell(0);
      if (cell == null) {
        tmp.append("\n");
      } else {
        tmp.append(cell.toString());
        tmp.append("\n");
      }
    }
    return IOUtils.toInputStream(tmp.toString(), "UTF-8");
  }

  /**
   * Prints a quad to stream in readable format.
   * @param quad RDF quad.
   * @param printStream
   */
  public static void printQuad(Quad quad, PrintStream printStream) {
    if (quad != null) {
      printStream.print("Subject : " + quad.getSubject());
      printStream.print("-- Predicate : " + quad.getPredicate());
      printStream.println("--> Object : " + quad.getObject());
    }
  }

  /**
   * Prints a quad to a standard output stream in readable format.
   * @param quad RDF quad.
   */
  public static void printQuad(Quad quad) {
    printQuad(quad, System.out);
  }

  /**
   * Prints s statement in a readable format.
   * @param statement RDF statement.
   * @param printStream Stream to print.
   */
  public static void printStatement(Statement statement, PrintStream printStream) {
    if (statement != null) {
      Resource  subject   = statement.getSubject();
      Property  predicate = statement.getPredicate();
      RDFNode   object    = statement.getObject();

      printStream.print(subject.toString());
      printStream.print(String.format(" %s ", predicate.toString()));

      if (object instanceof Resource) {
        printStream.print(object.toString());
      } else {
        printStream.print(String.format(" \" %s \"", object.toString()));
      }
      printStream.println(" .");
    }
  }

  /**
   * Prints a statement to a standard output stream in a readable format.
   * @param statement RDF statement.
   */
  public static void printStatment(Statement statement) {
    printStatement(statement, System.out);
  }

  /**
   * Prints statements in readable format
   * @param stmtIterator
   * @param printStream
   */
  public static void printStatements(StmtIterator stmtIterator, PrintStream printStream){
    if (stmtIterator != null) {
      if (null == printStream) { 
        printStream = System.out;
      }
      while (stmtIterator.hasNext()){
        printStatement(stmtIterator.nextStatement(), printStream);
      }
    }
  }
}
