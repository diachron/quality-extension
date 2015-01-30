package com.google.refine.quality.reports;

import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Utilities;
import com.google.refine.quality.vocabularies.QPROB;
import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * The QualityReport data structure creates a RDF jena model containing
 * the quality report described in D3.2.
 */
public class QualityReport {
  private Model model;
  private Resource report;
  private Bag problems;

  public QualityReport(String datasetURI) {
    model = ModelFactory.createDefaultModel();
    report = model.createResource("quality");
    problems = model.createBag();

    report.addProperty(QR.computedOn, model.createResource(datasetURI));
    report.addProperty(QR.hasProblem, problems);
  }

  public void setQualityStatistic(Model statistic) {
    model.add(statistic);
    report.addProperty(QR.statistics, statistic.getResource("statistic"));
  }
  public void setCleaningReport(Model cleaningReport) {
    model.add(cleaningReport);
    report.addProperty(QR.cleaning, cleaningReport.getResource("cleaningReport"));
  }
  public void addQualityProblem(QualityProblem problem) {
    problems.add(createQualityProblem(problem));
  }

  private Resource createQualityProblem(QualityProblem problem) {
    Resource problemResource = model.createResource(problem.getProblemURI());
    problemResource.addProperty(RDFS.label, problem.getProblemName());
    problemResource.addProperty(QPROB.problemDescription, problem.getProblemDescription());
    problemResource.addProperty(QPROB.cleaningSuggestion, problem.getCleaningSuggestion());
    if(problem.getQRefineRule() != null && !problem.getQRefineRule().trim().equals("")) {
      problemResource.addProperty(QPROB.qrefineRule, problem.getQRefineRule());
    }
    problemResource.addProperty(QR.problematicThing, createStatementResourse(problem.getQuad()));
    return problemResource;
  }

  public Bag getQualityProblemsBag() {
    return problems;
  }

  public Resource getQualityStatistic() {
    return report.getPropertyResourceValue(QR.statistics);
  }

  public String getUri() {
    return report.getPropertyResourceValue(QR.computedOn).toString();
  }

  public Model getQualityReportModel() {
    return model;
  }

  private Resource createStatementResourse(Quad quad) {
    Resource statement = model.createResource().addProperty(RDF.type, RDF.Statement)
      .addProperty(RDF.subject, quad.getSubject().toString())
      .addProperty(RDF.predicate, quad.getPredicate().toString())
      .addProperty(RDF.object, createObject(quad));
    return statement;
  }

  private RDFNode createObject(Quad quad) {
    RDFNode objectNode = null;
    String object = quad.getObject().toString();
    if (Utilities.isUrl(object)) {
      objectNode = ResourceFactory.createResource(object);
    } else {
      objectNode = ResourceFactory.createPlainLiteral(object.substring(1, object.length() - 1));
    }
    return objectNode;
  }
}
