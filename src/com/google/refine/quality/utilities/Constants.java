package com.google.refine.quality.utilities;

import java.io.File;

public class Constants {

  private static final String USER_DIR = System.getProperty("user.dir");
  
  public static final String ANNOTATION_PROPERTIES_FILE =  USER_DIR + File.separator + "extensions/"
    + "quality-extension/resources/AnnotationPropertiesList";
  public final static String LABEL_PROPERTIES_FILE = USER_DIR + File.separator +
      "extensions/quality-extension/resources/LabelPropertiesList";

  public static final String QPROB_FILE = USER_DIR + File.separator + "extensions/"
    + "quality-extension/resources/vocabularies/qprob/qprob.rdf";

  public static final String CAMEL_CASE_REGEX = "[A-Z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*"
    + "[A-Z][A-Z0-9]*[a-z])[A-Za-z0-9]*";

  public static final String COLUMN_SPLITER = "|&SPLITCOLUMN&|";
  public static final String ROW_SPLITER = "|&SPLITROW&|";
  public static final String METRICS_PACKAGE = "com.google.refine.quality.metrics";
  

}
