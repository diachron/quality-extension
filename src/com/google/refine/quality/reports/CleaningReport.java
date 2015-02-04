package com.google.refine.quality.reports;

import java.util.HashSet;

import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class CleaningReport {

  private Model model;
  private Resource cleaningReport;
  private Bag cleanedProblems;

  public CleaningReport() {
    model = ModelFactory.createDefaultModel();
    cleaningReport = model.createResource("cleaningReport");
    cleanedProblems = model.createBag();
    cleaningReport.addProperty(QR.cleanedProblem, cleanedProblems);
  }
  
  public void addCleanedProblem(Resource problem) {
    NodeIterator itr = cleanedProblems.iterator();
    HashSet<String> probs = new HashSet<String>();

    while (itr.hasNext()) {
      Resource bagItem = (Resource)itr.next();
      probs.add(bagItem.getProperty(RDF.type).getObject().toString());
    }

    if(probs.contains(problem.toString())) {
      itr = cleanedProblems.iterator();
      while (itr.hasNext()) {

        Resource bagItem = (Resource) itr.next();
        String cleanedProblemName = bagItem.getProperty(RDF.type).getObject().toString();
        if(cleanedProblemName.equals(problem.toString())) {
          Statement st = bagItem.getProperty(QR.numberOfAffectedTriples);
          long count = st.getObject().asLiteral().getLong();
          st.changeLiteralObject(++count);
        }
      }
    } else {
      cleanedProblems.add(model.createResource(problem).addProperty(QR.numberOfAffectedTriples,
          model.createTypedLiteral(1)));
    }
  }

  public Model getModel() {
    return model;
  }
}
