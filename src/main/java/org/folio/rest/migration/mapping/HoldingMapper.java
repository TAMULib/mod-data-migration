package org.folio.rest.migration.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.folio.rest.jaxrs.model.Holdingsrecord;

import io.vertx.core.json.JsonObject;

public class HoldingMapper {

  public HoldingMapper() { }

  public Holdingsrecord getHolding(JsonObject parsedRecord) throws JsonProcessingException {
    return new Holdingsrecord();
  }

}
