package com.google.refine.quality.utilities;

import java.io.File;

public class Constants {

  private static final String USER_DIR = System.getProperty("user.dir");
  
  public static final String ANNOTATION_PROPERTIES_FILE =  USER_DIR + File.separator +
      extensionPathHandler() + "resources/AnnotationPropertiesList";
  public final static String LABEL_PROPERTIES_FILE = USER_DIR + File.separator +
      extensionPathHandler() + "resources/LabelPropertiesList";

  public static final String QPROB_FILE = USER_DIR + File.separator + extensionPathHandler() +
    "resources/vocabularies/qprob/qprob.rdf";
  
  public static final String QR_FILE = USER_DIR + File.separator + extensionPathHandler() +
		    "resources/vocabularies/qr/qr.rdf";

  public static final String CAMEL_CASE_REGEX = "[A-Z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*[A-Z]"
    + "[A-Z0-9]*[a-z])[A-Za-z0-9]*";

  public static final String COLUMN_SPLITER = "|&SPLITCOLUMN&|";
  public static final String ROW_SPLITER = "|&SPLITROW&|";
  public static final String METRICS_PACKAGE = "com.google.refine.quality.metrics";
  public static final int PROBLEM_CELL = 5;
  
  public static final String QR_VOCAB = "QR";
  public static final String QPROB_VOCAB = "QPROB";
  
  
  private static String extensionPathHandler() {
    return USER_DIR.contains("extensions/quality-extension") ? "" : "extensions/quality-extension"
      + File.separator;
  }
}
