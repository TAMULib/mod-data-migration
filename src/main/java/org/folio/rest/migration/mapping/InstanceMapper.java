package org.folio.rest.migration.mapping;

import org.folio.Instance;
import org.folio.processing.mapping.defaultmapper.MarcToInstanceMapper;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;

import io.vertx.core.json.JsonObject;

public class InstanceMapper {

  private final MarcToInstanceMapper marcToInstanceMapper = new MarcToInstanceMapper();

  private final MappingParameters mappingParameters;
  private final JsonObject mappingRules;

  public InstanceMapper(MappingParameters mappingParameters, JsonObject mappingRules) {
    this.mappingParameters = mappingParameters;
    this.mappingRules = mappingRules;
  }

  public Instance getInstance(JsonObject parsedRecord) {
    return marcToInstanceMapper.mapRecord(parsedRecord, mappingParameters, mappingRules);
  }

}
