package com.google.refine.quality.webservices;

import java.util.ArrayList;
import java.util.List;

import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * The QualityStatistic data structure creates a RDF jena model containing
 * the quality statistic report described in D3.2.
 */
public class QualityStatistic {
  private Model model;
  private Resource stat;
  private Bag problemsBag;

  public QualityStatistic(long triples, long problems) {
    model = ModelFactory.createDefaultModel();
    stat = model.createResource("statistic");
    problemsBag = model.createBag();

    stat.addProperty(QR.hasProblem, problemsBag);
    stat.addLiteral(QR.totalNumberOfTriples, ResourceFactory.createTypedLiteral(triples));
    stat.addLiteral(QR.numberOfProblems, ResourceFactory.createTypedLiteral(problems));
  }

  public long getNumberOfTriples() {
    return stat.getProperty(QR.totalNumberOfTriples).getObject().asLiteral().getLong();
  }

  public long getNumberOfProblems() {
    return stat.getProperty(QR.numberOfProblems).getObject().asLiteral().getLong();
  }

  public Bag getProblemsCountBag() {
    return problemsBag;
  }

  public void incrementProblemCounter(Resource problem) {
    NodeIterator iter = problemsBag.iterator();
    List<String> probs = new ArrayList<String>();
    while (iter.hasNext()) {
      Resource bagItem = (Resource) iter.next();
      probs.add(bagItem.getProperty(RDF.type).getObject().toString());
    }

    if (!probs.contains(problem.toString())) {
      problemsBag.add(model.createResource(problem).addProperty(QR.numberOfAffectedTriples,
          model.createTypedLiteral(1)));
    } else {
      iter = problemsBag.iterator();
      while (iter.hasNext()) {
        Resource bagItem = (Resource) iter.next();
        String problemName = bagItem.getProperty(RDF.type).getObject().toString();
        if (problemName.equals(problem.toString())) {
          Statement st = bagItem.getProperty(QR.numberOfAffectedTriples);
          long count = st.getObject().asLiteral().getLong();
          bagItem.getProperty(QR.numberOfAffectedTriples).changeLiteralObject(++count);
        }
      }
    }
  }

  public Model getQualityStatisticModel() {
    return model;
  }
}
