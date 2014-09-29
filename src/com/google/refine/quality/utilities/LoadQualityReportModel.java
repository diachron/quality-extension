package com.google.refine.quality.utilities;

import java.io.File;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class LoadQualityReportModel {
  private static final Logger LOG = Logger.getLogger(LoadQualityReportModel.class);

  private static Model qualityReportModel = null;

  public static String getResourcePropertyValue(Resource resource, Property property) {
    if (resource == null || property == null) {
      return "";
    }

    if (LoadQualityReportModel.qualityReportModel == null) {
      LoadQualityReportModel.qualityReportModel = ModelFactory.createDefaultModel();

      assert new File(Constants.QPROB_FILE).exists();

      qualityReportModel = FileManager.get().loadModel(Constants.QPROB_FILE);
      LOG.info("QPROB data model is loaded.");
      // LoadQualityReportModel.qualityReportModel.read(filePathName);

      // /* -- For Debug Only
      // StmtIterator iter1 =
      // LoadQualityReportModel.qualityReportModel.listStatements();
      // while(iter1.hasNext()){
      // Statement s = iter1.nextStatement();
      // System.out.print("Subject :: " + s.getSubject());
      // System.out.print("  --  Predicate :: " + s.getPredicate());
      // System.out.println("  --> Object :: " + s.getObject());
      // LOG.info("Subject :: " + s.getSubject() + "  --  Predicate :: " +
      // s.getPredicate() +"  --> Object :: " + s.getObject() );
      //
      // }
    }
    if (LoadQualityReportModel.qualityReportModel.getProperty(resource, property) != null) {
      return LoadQualityReportModel.qualityReportModel.getProperty(resource, property).getObject()
          .toString();
    } else {
      StmtIterator iter = LoadQualityReportModel.qualityReportModel.listStatements();
      while (iter.hasNext()) {
        Statement s = iter.nextStatement();
        if (s.getSubject().getURI().toString()
            .contains(resource.getURI().toString().replace("Problem", "Recommendation"))
            && s.getPredicate().getURI().toString().contains(property.getURI().toString())) {
          return s.getObject().toString();
        }

      }
    }
    return "";
  }

}
