package com.google.refine.quality.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.refine.model.Cell;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public final class Utilities {

  /**
   * Retrieves data from project and writes it to an InputStream.
   * @param project An OpenRefine project.
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
   * Extracts quads and rows from a project. Stores in a hash map <quad-hash, row>.
   * @param project An OpenRefine project.
   * @return A hash map containing quads hashes and rows.
   */
  public static HashMap<Integer, Integer> getQuadsAsHashes(Project project) {
    HashMap<Integer, Integer> quads = new HashMap<Integer, Integer>();
    for (int row = 0; row < project.rows.size(); row++) {
      Row rowObj = project.rows.get(row);
      if (!rowObj.isEmpty() && rowObj != null) {
        Statement statement = createStatementFromRowCells(rowObj);
        if (statement != null) {
          quads.put(Math.abs(new Quad(null, statement.asTriple()).hashCode()), row); 
        }
      } else {
        quads.put(null, row);
      }
    }
    return quads;
  }

  /**
   * Retrieves data from project as a list of quads. Data accessed at first tree
   * columns of the OpenRefine project.
   * @param project An OpenRefine project.
   * @return A list of quads.
   */
  public static List<Quad> getQuadsFromProject(Project project) {
    List<Quad> quads = new ArrayList<Quad>();
    for (int row = 0; row < project.rows.size(); row++) {
      Row rowObj = project.rows.get(row);
      if (!rowObj.isEmpty() && rowObj != null) {
        Statement stat = createStatementFromRowCells(rowObj);
        if (stat != null) {
          quads.add(new Quad(null, createStatementFromRowCells(rowObj).asTriple()));
        }
      }
    }
    return quads;
  }

  /**
   * Creates a statement by parsing cells of a certain row.
   * @param row A row containing cells.
   * @return A statement object/
   */
  private static Statement createStatementFromRowCells(Row row) {
    Statement statement = null;
    // 0 index - stared
    Cell subjectCell = row.getCell(1);
    Cell predicateCell = row.getCell(2);
    Cell objectCell = row.getCell(3);
    if (objectCell != null && predicateCell != null && subjectCell != null) {
      statement = createStatement(subjectCell.toString(), predicateCell.toString(),
          objectCell.toString());
    }
    // TODO return not null!!!
    return statement;
  }

  /**
   * Creates a statement object from a quad.
   * @param quad A Jena quad object.
   * @return A Jena statement obejct.
   */
  public static Statement createStatement(Quad quad) {
    if (quad == null) {
      throw new NullPointerException();
    }
    String object = quad.getSubject().toString();
    String predicate = quad.getPredicate().toString();
    String subject = quad.getSubject().toString();
    RDFNode objectNode = null;

    if (Utilities.isUrl(object)) {
      objectNode = ResourceFactory.createResource(object);
    } else {
      objectNode = ResourceFactory.createPlainLiteral(object.substring(1, object.length() - 1));
    }
    return ResourceFactory.createStatement(ResourceFactory.createResource(subject),
        ResourceFactory.createProperty(predicate), objectNode);
  }
  /**
   * Creates a statement object from a quad.
   * @param quad A Jena quad object.
   * @return A Jena statement obejct.
   */
  public static Statement createStatement(String subject, String predicate, String object) {
    RDFNode objectNode = null;

    if (Utilities.isUrl(object)) {
      objectNode = ResourceFactory.createResource(object);
    } else {
      objectNode = ResourceFactory.createPlainLiteral(object.substring(1, object.length() - 1));
    }
    return ResourceFactory.createStatement(ResourceFactory.createResource(subject),
        ResourceFactory.createProperty(predicate), objectNode);
  }

  /**
   * Checks if a string is a valid URL.
   * @param str A string to check.
   * @return true if the string is a valid URL.
   */
  public static boolean isUrl(String str) {
    if (str == null) {
      return false;
    }
    String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    return pattern.matcher(str).find();
  }

  /**
   * Serializes a map.
   * @param map A map to serialize.
   * @return A string containing the JSON representation of the map.
   * @throws JsonProcessingException 
   */
  public static String convertMapToString(@SuppressWarnings("rawtypes") Map map) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(map);
  }

  /**
   * Removes empty rows from OpenRefine project.
   * @param project An OpenRefine project.
   */
  public static void removeEmptyRows(Project project) {
    List<Row> rowsIndexes = new ArrayList<Row>();
    for (Row row : project.rows) {
      boolean rowIsBlank = row.isCellBlank(1) && row.isCellBlank(2) && row.isCellBlank(3);
      if (rowIsBlank || row.isEmpty()) {
        rowsIndexes.add(row);
      }
    }
    for (Row row : rowsIndexes) {
      project.rows.remove(row);
    }
  }

  /**
   * Prints a quad to stream in readable format.
   * @param quad A RDF quad.
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
   * @param quad A RDF quad.
   */
  public static void printQuad(Quad quad) {
    printQuad(quad, System.out);
  }

  /**
   * Prints s statement in a readable format.
   * @param statement A statement.
   * @param printStream A stream to print to.
   */
  public static void printStatement(Statement statement, PrintStream printStream) {
    if (statement != null) {
      Resource subject = statement.getSubject();
      Property predicate = statement.getPredicate();
      RDFNode object = statement.getObject();

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
   * @param statement A RDF statement.
   */
  public static void printStatment(Statement statement) {
    printStatement(statement, System.out);
  }

  /**
   * Prints statements in readable format
   * @param stmtIterator
   * @param printStream
   */
  public static void printStatements(StmtIterator stmtIterator, PrintStream printStream) {
    if (stmtIterator != null) {
      if (null == printStream) {
        printStream = System.out;
      }
      while (stmtIterator.hasNext()) {
        printStatement(stmtIterator.nextStatement(), printStream);
      }
    }
  }
}
