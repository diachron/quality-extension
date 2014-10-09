package com.google.refine.quality.utilities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

public class JenaModelLoader {

  /**
   * Creates a Jena model from an InputStream.
   * @param in InputStream object.
   * @return Jena model.
   */
  //TODO add support for different rdf serializations.
  public static Model getModel(InputStream in) {
    Model model = ModelFactory.createDefaultModel();
    model.read(in, null, "TURTLE");
    return model;
  }

  /**
   * Reads RDF quads from an InputStream.
   * @param in InputStream object.
   * @return A list of RDF quads.
   */
  public static List<Quad> getQuads(InputStream in) {
    Model model = getModel(in);
    List<Quad> quads = new ArrayList<Quad>();
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
    return quads;
  }
  
  
  /**
   * Creates a Jena model from an url.
   * @param url A File URL.
   * @return Jena model.
   */
  public static Model getModel(String url) {
    Model model = ModelFactory.createDefaultModel();
    if (url != null && !url.isEmpty()) {
      model.read(url);
    }
    return model;
  }

  /**
   * Reads RDF quads from an url.
   * @param model A Jena model.
   * @return A list of RDF quads.
   */
  public static List<Quad> getQuads(String url) {
    Model model = getModel(url);
    List<Quad> quads = new ArrayList<Quad>();
    StmtIterator si = model.listStatements();
    while (si.hasNext()) {
      quads.add(new Quad(null, si.next().asTriple()));
    }
    return quads;
  }
}
