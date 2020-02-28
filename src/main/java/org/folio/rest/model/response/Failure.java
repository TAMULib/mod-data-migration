package org.folio.rest.model.response;

public class Failure {

  private final Object payload;

  private final String reason;

  public Failure(Object paylaod, String reason) {
    this.payload = paylaod;
    this.reason = reason;
  }

  public Object getPayload() {
    return payload;
  }

  public String getReason() {
    return reason;
  }

  public static Failure of(Object payload, String reason) {
    return new Failure(payload, reason);
  }

}