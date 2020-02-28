package org.folio.rest.migration.utility;

public class TimingUtility {

  public static double getDeltaInMilliseconds(long startTime) {
    return ((double) (System.nanoTime() - startTime) / (double) 1000000);
  }

}
