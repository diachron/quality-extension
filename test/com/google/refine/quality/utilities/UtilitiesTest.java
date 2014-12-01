package com.google.refine.quality.utilities;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.vocabulary.RDFS;

public class UtilitiesTest {

  @Test
  public void isURL() {
    Assert.assertFalse(Utilities.isUrl(null));
    Assert.assertFalse(Utilities.isUrl(""));
    Assert.assertFalse(Utilities.isUrl("http:"));
    Assert.assertFalse(Utilities.isUrl("hello.com/#"));
    Assert.assertFalse(Utilities.isUrl("http://"));
    Assert.assertFalse(Utilities.isUrl("ttp://"));
    Assert.assertFalse(Utilities.isUrl("htp://hello"));
    Assert.assertTrue(Utilities.isUrl("http://hello"));
    Assert.assertTrue(Utilities.isUrl("http://hello.com"));
    Assert.assertTrue(Utilities.isUrl("http://hello.com?"));
    Assert.assertTrue(Utilities.isUrl("http://hello.com/hello#hello"));
    Assert.assertTrue(Utilities.isUrl(RDFS.label.toString()));
  }
}
