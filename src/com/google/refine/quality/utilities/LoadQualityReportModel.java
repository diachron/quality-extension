package com.google.refine.quality.utilities;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Retrieve problems messages from file
 * @author Muhammad Ali Qasmi
 *
 */
public class LoadQualityReportModel {
    /**
     * Default file path name
     */
    protected static String filePathName = "extensions/quality-extension/resources/vocabularies/qr/qr.rdf";
    protected static Model qualityReportModel = null;
    public static String getResourcePropertyValue(Resource resource ,Property property) {
        
        if (resource == null || property == null) {
            return "";
        }
        
        if (LoadQualityReportModel.qualityReportModel == null) {
            LoadQualityReportModel.qualityReportModel = ModelFactory.createDefaultModel();
            LoadQualityReportModel.qualityReportModel.read(filePathName);
            /* -- For Debug Only
            StmtIterator iter = LoadQualityReportModel.qualityReportModel.listStatements();
            while(iter.hasNext()){
                Statement s = iter.nextStatement();
                System.out.print("Subject :: " + s.getSubject());
                System.out.print("  --  Predicate :: " + s.getPredicate());
                System.out.println("  --> Object :: " + s.getObject());
            }*/
        }
        if (LoadQualityReportModel.qualityReportModel.getProperty(resource, property) != null)
        {
            return LoadQualityReportModel.qualityReportModel
                     .getProperty(resource, property)
                     .getObject().toString();
        } else {
            StmtIterator iter = LoadQualityReportModel.qualityReportModel.listStatements();
            while(iter.hasNext()){
                Statement s = iter.nextStatement();
                if (s.getSubject().getURI().toString().contains(resource.getURI().toString().replace("Problem", "Recommendation")) &&
                    s.getPredicate().getURI().toString().contains(property.getURI().toString())
                    )
                {
                    return s.getObject().toString();
                }
                    
            }
        }
        return "";
    }
        
}
