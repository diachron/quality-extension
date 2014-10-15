package com.google.refine.quality.utilities;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class VocabluaryReaderTest {

  @Test
  public void correctUrl() {
    Model model = VocabularyReader.read(RDFS.label.toString());
    Assert.assertFalse(model.isEmpty());
    Assert.assertTrue(model.getResource(RDFS.label.toString()).hasProperty(RDFS.domain));
    Assert.assertTrue(model.getResource(RDFS.label.toString()).hasProperty(RDFS.range));
    Assert.assertFalse(model.getResource(RDFS.label.toString()).hasProperty(OWL.allValuesFrom));
  }

  @Test
  public void wrongUrl() {
    Model model1 = ModelFactory.createDefaultModel();
    Assert.assertTrue(model1.isEmpty());
    Model model = VocabularyReader.read("http://example.com/hello#hello");
    Assert.assertTrue(model.isEmpty());
  }
  
  @Test
  public void emptyUrl() {
    Model model = VocabularyReader.read("");
    Assert.assertTrue(model.isEmpty());
  }
  
  @Test
  public void nullUrl() {
    Model model = VocabularyReader.read(null);
    Assert.assertTrue(model.isEmpty());
  }
  
  @AfterClass
  public static void tearDown() {
    VocabularyReader.clear();
  }
}
