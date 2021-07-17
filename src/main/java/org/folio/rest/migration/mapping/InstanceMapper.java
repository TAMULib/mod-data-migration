package org.folio.rest.migration.mapping;

import java.util.Objects;

import org.folio.processing.mapping.defaultmapper.MarcToInstanceMapper;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.rest.jaxrs.model.inventory.Instance;
import org.springframework.beans.BeanUtils;

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
    Instance instance = null;
    org.folio.Instance mappedInstance = marcToInstanceMapper.mapRecord(parsedRecord, mappingParameters, mappingRules);
    if (Objects.nonNull(mappedInstance)) {
      instance = new Instance();
      BeanUtils.copyProperties(mappedInstance, instance);
    }
    return instance;
  }

}
