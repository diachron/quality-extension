package com.google.refine.quality.exceptions;

public class MetricException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MetricException(String message) {
    super(message);
  }

  public MetricException(String message, Throwable cause) {
    super(message + ": " + cause.getMessage(), cause);
  }
}
