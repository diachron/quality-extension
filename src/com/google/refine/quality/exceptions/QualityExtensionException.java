package com.google.refine.quality.exceptions;

public class QualityExtensionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public QualityExtensionException(String message) {
    super(message);
  }

  public QualityExtensionException(String message, Throwable cause) {
    super(message + ": " + cause.getMessage(), cause);
  }
}
