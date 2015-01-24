package com.google.refine.quality.utilities;

import java.io.File;

public class Constants {

  private static final String USER_DIR = System.getProperty("user.dir");
  
  public static final String ANNOTATION_PROPERTIES_FILE =  USER_DIR + File.separator +
    extensionPathHandler() + "resources/AnnotationPropertiesList";
  public final static String LABEL_PROPERTIES_FILE = USER_DIR + File.separator +
    extensionPathHandler() + "resources/LabelPropertiesList";
  public final static String UNDEFINED_CLASS_PROPERTIES_FILE = USER_DIR + File.separator +
    extensionPathHandler() + "resources/UndefinedClassPropertiesList";
  public final static String UNDEFINED_PROPERTIES_FILE = USER_DIR + File.separator +
    extensionPathHandler() + "resources/UndefinedPropertiesList";

  public static final String QPROB_FILE = USER_DIR + File.separator + extensionPathHandler() +
    "resources/vocabularies/qprob/qprob.rdf";

  public static final String QR_FILE = USER_DIR + File.separator + extensionPathHandler() +
    "resources/vocabularies/qr/qr.rdf";

  public static String LOADED_MODELS = USER_DIR + File.separator + extensionPathHandler()
    + "resources/models";

  public static final String CAMEL_CASE_REGEX = "[A-Z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*[A-Z]"
    + "[A-Z0-9]*[a-z])[A-Za-z0-9]*";

  public static final String COLUMN_SPLITER = "|&SPLITCOLUMN&|";
  public static final String ROW_SPLITER = "|&SPLITROW&|";
  public static final String METRICS_PACKAGE = "com.google.refine.quality.metrics";
  public static final int PROBLEM_CELL = 4;

  public static final String QR_VOCAB = "QR";
  public static final String QPROB_VOCAB = "QPROB";
  
  private static String extensionPathHandler() {
    return USER_DIR.contains("extensions/quality-extension") ? "" : "extensions/quality-extension"
      + File.separator;
  }

  // Web Service constans
  public static final String METHOD_GET = "GET";
  public static final int SC_BAD_REQUEST = 400;
  public static final int SC_OK = 200;
  public static final String SERIALIZATION = "Turtle";
  public static final String CLEANING_SUGGESTION = "Cleaning_suggestion.ttl";
  public static final String CLEANED_RESULT = "Cleaned.zip";
  public static final String CLEANED_DATASET = "cleanedModel.ttl";
  public static final String DELTA_MODEL = "deltaModel.ttl";
}
