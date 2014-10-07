package com.google.refine.quality.exceptions;

public class MetricInitializationException extends MetricException {
  private static final long serialVersionUID = 1L;

  public MetricInitializationException(String message) {
    super(message);
  }

  public MetricInitializationException(String message, Throwable cause) {
    super(message + ": " + cause.getMessage(), cause);
  }
}
