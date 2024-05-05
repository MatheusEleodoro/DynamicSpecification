package br.com.evertec.sinqia.contabil.specification.exception;

public class SQSpecificationException extends RuntimeException {
  public SQSpecificationException(String message, Throwable cause) {
    super(message, cause);
  }
  public SQSpecificationException(Throwable cause) {
    super(cause);
  }
}
