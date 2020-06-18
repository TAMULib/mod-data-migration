package org.folio.rest.migration.mapping;

import org.folio.rest.jaxrs.model.Holdingsrecord;
import org.marc4j.marc.Record;

import com.fasterxml.jackson.core.JsonProcessingException;

public class HoldingMapper {

  public HoldingMapper() {
  }

  public Holdingsrecord getHolding(Record record) throws JsonProcessingException {
    return new Holdingsrecord();
  }

}
