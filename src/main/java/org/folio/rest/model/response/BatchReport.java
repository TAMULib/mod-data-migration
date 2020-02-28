package org.folio.rest.model.response;

import java.util.ArrayList;
import java.util.List;

public class BatchReport {

  private final long total;

  private final List<Failure> failures;

  public BatchReport(long total) {
    this.total = total;
    this.failures = new ArrayList<Failure>();
  }

  public long getTotal() {
    return total;
  }

  public List<Failure> getFailures() {
    return failures;
  }

  public void fail(Object payload, String reason) {
    failures.add(Failure.of(payload, reason));
  }

}
