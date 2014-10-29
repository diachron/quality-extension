package com.google.refine.quality.webservices;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.vocabularies.QPROB;
import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class QualityReport {

  public void createQualityReport(URL outputPath, URL inputDataset, List<QualityProblem> problems) {
    Model model = ModelFactory.createDefaultModel();
    Resource report = model.createResource(generateURI());
    report.addProperty(QR.computedOn, model.createResource(inputDataset.toString()));
    for (QualityProblem qualityProblem : problems) {
      Resource qprob = model.createResource(qualityProblem.getProblemURI());
      qprob.addProperty(RDFS.label, qualityProblem.getProblemName());
      qprob.addProperty(QPROB.problemDescription, qualityProblem.getProblemDescription());
      qprob.addProperty(QPROB.cleaningSuggestion, qualityProblem.getCleaningSuggestion());
      qprob.addProperty(QPROB.qrefineRule, qualityProblem.getCleaningSuggestion());
      Resource poblemTriple = model
          .createResource(generateURI())
          .addProperty(RDF.type, RDF.Statement)
          .addProperty(RDF.subject,
              model.createResource(qualityProblem.getQuad().getSubject().toString()))
          .addProperty(
              RDF.predicate,
              model.createResource(qualityProblem.getQuad().getPredicate().toString())
                  .addProperty(RDF.object,
                      model.createResource(qualityProblem.getQuad().getObject().toString())));
      report.addProperty(QR.problematicThing, poblemTriple);
      report.addProperty(QR.hasProblem, qprob);
    }

  }

  private Resource generateURI() {
    String uri = "urn:" + UUID.randomUUID().toString();
    Resource r = ModelFactory.createDefaultModel().createResource(uri);
    return r;
  }

  private static RDFNode generateRDFBlankNode() {
    return ModelFactory.createDefaultModel().asRDFNode(NodeFactory.createAnon());
  }
  
  // an example function invoked in controller.js
  public static String testMetrics(HttpServletRequest request, HttpServletResponse response) {
    return request.getMethod();
  }
}
