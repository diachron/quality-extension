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

// TODO simplify getResourcePropertyValue method 
public class QualityReportModelLoader {
  private static final Logger LOG = Logger.getLogger(QualityReportModelLoader.class);

  private static Model qualityReportModel = null;

  public static String getResourcePropertyValue(Resource resource, Property property) {
    if (resource == null || property == null) {
      return "";
    }

    if (QualityReportModelLoader.qualityReportModel == null) {
      QualityReportModelLoader.qualityReportModel = ModelFactory.createDefaultModel();

      assert new File(Constants.QPROB_FILE).exists();

      qualityReportModel = FileManager.get().loadModel(Constants.QPROB_FILE);
      LOG.info("QPROB data model is loaded.");
    }
    if (QualityReportModelLoader.qualityReportModel.getProperty(resource, property) != null) {
      return QualityReportModelLoader.qualityReportModel.getProperty(resource, property).getObject()
          .toString();
    } else {
      StmtIterator iter = QualityReportModelLoader.qualityReportModel.listStatements();
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
