package org.folio.rest.migration.mapping.function;

import java.util.Arrays;
import java.util.Iterator;

import org.folio.rest.migration.mapping.RuleExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run a splitter on a string or run a function.
 */
public class NormalizationFunctionRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizationFunctionRunner.class);

  private static final String SPLIT_FUNCTION_SPLIT_EVERY = "split_every";

  private NormalizationFunctionRunner() {
    throw new UnsupportedOperationException("Cannot instantiate utility class.");
  }

  /**
   * Split val into chunks of param characters if funcName is "split_every".
   * Return null if val is null or funcName is not "split_every".
   *
   * @return the chunks
   */
  public static Iterator<String> runSplitFunction(String funcName, String subFieldData, String param) {
    if (SPLIT_FUNCTION_SPLIT_EVERY.equalsIgnoreCase(funcName) && subFieldData != null) {
      int size = Integer.parseInt(param);
      String[] tokens = subFieldData.split("(?<=\\G.{" + size + "})");
      return Arrays.stream(tokens).iterator();
    }
    return null;
  }

  /**
   * Run the function funcName on val and param.
   *
   * @return the function's result
   */
  public static String runFunction(String functionName, RuleExecutionContext ruleExecutionContext) {
    try {
      return NormalizationFunction.valueOf(functionName.trim().toUpperCase()).apply(ruleExecutionContext);
    } catch (RuntimeException e) {
      LOGGER.error("Error while running normalization functions", e);
      return ruleExecutionContext.getSubfieldValue();
    }
  }

}