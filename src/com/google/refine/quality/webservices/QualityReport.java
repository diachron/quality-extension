package com.google.refine.quality.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.refine.browsing.Engine.Mode;
import com.google.refine.quality.exceptions.MetricException;
import com.google.refine.quality.metrics.AbstractQualityMetric;
import com.google.refine.quality.problems.QualityProblem;
import com.google.refine.quality.utilities.Constants;
import com.google.refine.quality.utilities.JenaModelLoader;
import com.google.refine.quality.vocabularies.QPROB;
import com.google.refine.quality.vocabularies.QR;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
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

  private static Resource generateURI() {
    String uri = "urn:" + UUID.randomUUID().toString();
    Resource r = ModelFactory.createDefaultModel().createResource(uri);
    return r;
  }

  private static RDFNode generateRDFBlankNode() {
    return ModelFactory.createDefaultModel().asRDFNode(NodeFactory.createAnon());
  }
  
  public static String generateQualityReport(HttpServletRequest request, 
      HttpServletResponse response) throws ServletException, IOException, 
      MetricException, JSONException, ClassNotFoundException, 
      InstantiationException, IllegalAccessException {
    
    Model model = null;
    if(request.getParameter("accessMethodRadio").equals("File")) {
      Map multiPartEntries = (Map)request.getAttribute("multipart.entries");
      MultipartEntity multiPartEntry = (MultipartEntity) multiPartEntries.get("fileValue");
      InputStream fileContent = multiPartEntry.getContent();
      
      model = JenaModelLoader.getModel(fileContent);
    } else {
      model = JenaModelLoader.getModel(request.getParameter("urlValue"));
    }
    
    String []accuracySubMets = request.getParameterValues("accuracySubMet");
    String []consistencySubMets = request.getParameterValues("consistencySubMet");
    String []understandabilitySubMet = request.getParameterValues("understandabilitySubMet");
    
    JSONArray metrics = new JSONArray();
    updateMetricsArray(metrics, accuracySubMets);
    updateMetricsArray(metrics, consistencySubMets);
    updateMetricsArray(metrics, understandabilitySubMet);
    
    List<QualityProblem> qualityProblems = identifyQualityProblems(model, metrics);
    
    generateQualityReport("", model.listStatements().toList().size(), qualityProblems);
    
    return "";	  
  }
  private static String generateQualityReport(String dataModelUrl, int totalTriples, 
      List<QualityProblem> qualityProblems) {
    
    Hashtable<Resource, Integer> problemCount = new Hashtable<Resource, Integer>();
    
    Model model = ModelFactory.createDefaultModel();
    Resource report = model.createResource();
    report.addProperty(QR.computedOn, model.createResource(dataModelUrl));
    
    Resource qualityStats = model.createResource();
    qualityStats.addProperty(QR.totalNumberOfTriples, model.createTypedLiteral(totalTriples));
    qualityStats.addProperty(QR.numberOfProblems, 
        model.createTypedLiteral(qualityProblems.size()));
    
    for(QualityProblem qualityProblem : qualityProblems) {
      
      Resource qprob = model.createResource(qualityProblem.getProblemURI());
      qprob.addProperty(RDFS.label, qualityProblem.getProblemName());
      qprob.addProperty(QPROB.problemDescription, qualityProblem.getProblemDescription());
      qprob.addProperty(QPROB.cleaningSuggestion, qualityProblem.getCleaningSuggestion());
      qprob.addProperty(QPROB.qrefineRule, qualityProblem.getCleaningSuggestion());
      
      if(!problemCount.containsKey(qualityProblem.getProblemURI())) {
        problemCount.put(qualityProblem.getProblemURI(), 0);
      }
      int currentCount = problemCount.get(qualityProblem.getProblemURI()).intValue();
      problemCount.put(qualityProblem.getProblemURI(), currentCount+1);
    }
    
    updateStats(model, qualityStats, problemCount);
    return "";
  }
  private static void updateStats(Model model, Resource stats, Hashtable<Resource, Integer> problemCount) {
    
    Set<Entry<Resource, Integer>> entrySet = problemCount.entrySet();
    Iterator<Entry<Resource, Integer>> iterator = entrySet.iterator();
    
    while (iterator.hasNext()) {
      
      Entry<Resource, Integer> entry = iterator.next();
      
      Resource qProb = entry.getKey();
      qProb.addProperty(QR.numberOfAffectedTriples, 
          model.createTypedLiteral(entry.getValue().intValue()));
      
      Resource triple = model.createResource(generateURI()).
          addProperty(RDF.type, RDF.Statement).
          addProperty(RDF.subject, qProb.getProperty(QR.numberOfAffectedTriples).getSubject()).
          addProperty(RDF.predicate, model.createResource(QR.numberOfAffectedTriples)).
          addProperty(RDF.object, qProb.getProperty(QR.numberOfAffectedTriples).getObject());
      
      stats.addProperty(QR.problem, triple);
    }
  }
  private static List<QualityProblem> identifyQualityProblems(Model dataModel, 
      JSONArray metrics) throws MetricException, JSONException, ClassNotFoundException, 
      InstantiationException, IllegalAccessException { 
    
    List<QualityProblem> qualityProblems = new ArrayList<QualityProblem>();
    List<Quad> quads = JenaModelLoader.getQuads(dataModel);
    
    for(int i=0; i<metrics.length(); i++) {
      String metricName = (String) metrics.get(i);
      Class<?> cls = Class.forName(String.format("%s.%s", Constants.METRICS_PACKAGE, 
          metricName));
      AbstractQualityMetric metric = (AbstractQualityMetric) cls.newInstance();
    
      metric.before();
      metric.compute(quads);
      metric.after();
      
      qualityProblems.addAll(metric.getQualityProblems());
    }
    return qualityProblems;
  }  
  private static void updateMetricsArray(JSONArray result, String[] metrics) {
    
    if(metrics != null) {
      for(int i=0; i<metrics.length; i++) {
        result.put(metrics[i]);
      }
    }
  } 

  
  // an example function invoked in controller.js
  public static String testMetrics(HttpServletRequest request, HttpServletResponse response) {
    return request.getMethod();
  }
}
