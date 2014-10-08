package com.google.refine.quality.utilities;

import java.io.File;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;



// TODO simplify getResourcePropertyValue method 
public class VocabularyLoader {
  private static final Logger LOG = Logger.getLogger(VocabularyLoader.class);

  private static Model qrVocabulary = null;
  private static Model qprobVocabulary = null;
  
  static void loadVocabularies(){      		
	  VocabularyLoader.qrVocabulary = ModelFactory.createDefaultModel();
      assert new File(Constants.QR_FILE).exists();
      qrVocabulary = FileManager.get().loadModel(Constants.QR_FILE);
      LOG.info("QR data model is loaded."); 
      
      VocabularyLoader.qprobVocabulary = ModelFactory.createDefaultModel();
      assert new File(Constants.QPROB_FILE).exists();
      qprobVocabulary = FileManager.get().loadModel(Constants.QPROB_FILE);
      LOG.info("QPROB data model is loaded."); 
            
  }

  public static String getResourcePropertyValue(Resource resource, Property property, String modelName) {
    if (resource == null || property == null) {
      return "";
    }

    Model model = null;
    if (modelName.equals(Constants.QR_VOCAB)) {
    	 if (VocabularyLoader.qrVocabulary == null) {
    	      VocabularyLoader.qrVocabulary = ModelFactory.createDefaultModel();
    	      assert new File(Constants.QR_FILE).exists();
    	      qrVocabulary = FileManager.get().loadModel(Constants.QR_FILE);
    	      LOG.info("QR data model is loaded.");
    	    }    	
		model = qrVocabulary;
	}
    if (modelName.equals(Constants.QPROB_VOCAB)) {
    	if (VocabularyLoader.qprobVocabulary == null) {
  	      VocabularyLoader.qprobVocabulary = ModelFactory.createDefaultModel();
  	      assert new File(Constants.QR_FILE).exists();
  	      qprobVocabulary = FileManager.get().loadModel(Constants.QPROB_FILE);
  	      LOG.info("QPROB data model is loaded.");
  	    } 
		model = qprobVocabulary;
	}
    if (model.getProperty(resource, property) != null)
    {
     return  model.getProperty(resource, property)
                 .getObject().toString();
    }   
    return "";
  }
}
