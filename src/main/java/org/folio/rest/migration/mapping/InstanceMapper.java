package org.folio.rest.migration.mapping;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.rest.migration.mapping.function.NormalizationFunctionRunner;
import org.folio.rest.migration.mapping.model.MappingEntity;
import org.folio.rest.migration.mapping.model.MappingField;
import org.folio.rest.migration.mapping.model.MappingRule;
import org.folio.rest.migration.mapping.model.RuleCondition;
import org.folio.rest.migration.mapping.model.SubfieldDelimiter;
import org.folio.rest.migration.mapping.model.SubfieldSplit;
import org.folio.rest.migration.model.generated.inventory_storage.Instance;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class InstanceMapper {

  private static final Set<String> CONTROL_FIELDS = new HashSet<>(Arrays.asList(new String[] { "001", "003", "004", "005", "007", "008" }));

  private static final String COMMA = ",";
  private static final String PERIOD = ".";
  private static final String PERIOD_REGEX = Pattern.quote(PERIOD);

  private final Map<String, List<MappingField>> controlMappingFields;
  private final Map<String, List<MappingField>> dataMappingFields;

  private final MappingParameters mappingParameters;
  private final ObjectMapper objectMapper;

  public InstanceMapper(MappingParameters mappingParameters, ObjectMapper objectMapper, JsonNode rules) {
    this.controlMappingFields = new HashMap<String, List<MappingField>>();
    this.dataMappingFields = new HashMap<String, List<MappingField>>();
    this.objectMapper = objectMapper;
    this.mappingParameters = mappingParameters;
    try {
      loadRules(rules);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Instance getInstance(Record record) throws JsonProcessingException {
    ObjectNode instanceObject = objectMapper.createObjectNode();
    RuleExecutionContext ruleExecutionContext = RuleExecutionContext.with(mappingParameters, instanceObject, record.getLeader());
    // @formatter:off
    record.getControlFields().stream()
        .filter(field -> controlMappingFields.keySet().contains(field.getTag()))
        .filter(field -> StringUtils.isNotEmpty(field.getData()))
        .map(field -> ruleExecutionContext.withControlField(field)
            .withMappingFields(controlMappingFields.get(field.getTag())))
        .filter(context -> CollectionUtils.isNotEmpty(context.getMappingFields()))
        .forEach(context -> processControlFieldRules(context));

    record.getDataFields().stream()
        .filter(field -> dataMappingFields.keySet().contains(field.getTag()))
        .map(field -> ruleExecutionContext.withDataField(field)
            .withMappingFields(dataMappingFields.get(field.getTag())))
        .filter(context -> CollectionUtils.isNotEmpty(context.getMappingFields()))
        .forEach(context -> processDataFieldRules(context));
    // @formatter:on
    return objectMapper.treeToValue(ruleExecutionContext.getInstanceObject(), Instance.class);
  }

  private void processControlFieldRules(RuleExecutionContext ruleExecutionContext) {
    ObjectNode instanceObject = ruleExecutionContext.getInstanceObject();
    ControlField controlField = ruleExecutionContext.getControlField();
    String data = controlField.getData();

    ruleExecutionContext.getMappingFields().forEach(mappingField -> {

      // NOTE: entityPerRepeatedSubfield ignored when processing control fields

      Map<String, List<MappingEntity>> mappingEntitiesPerComplexObject = new HashMap<>();

      mappingField.getEntity().stream().forEach(mappingEntity -> {

        // NOTE: applyRulesOnConcatenatedData ignored when processing control fields

        // NOTE: ignoreSubsequentFields ignored when processing control fields

        String target = mappingEntity.getTarget();
        if (target.contains(PERIOD)) {
          String[] path = splitPath(target);
          appendMappingEntity(mappingEntitiesPerComplexObject, path[0], mappingEntity);
        } else {
          String processedData = processRules(mappingEntity, ruleExecutionContext.withSubfieldValue(data));
          processSimpleData(instanceObject, target, processedData);
        }
      });

      Map<String, List<Map<String, String>>> complexDataTargetsMap = new HashMap<>();

      mappingEntitiesPerComplexObject.entrySet().stream().forEach(entry -> {
        String property = entry.getKey();
        List<Map<String, String>> complexDataTargets = getComplexDataTargets(complexDataTargetsMap, property);
        Map<String, String> complexData = new HashMap<String, String>();

        entry.getValue().stream().forEach(mappingEntity -> {
          String processedData = processRules(mappingEntity, ruleExecutionContext.withSubfieldValue(data));
          String[] path = splitPath(mappingEntity.getTarget());
          String subProperty = path[1];
          complexData.put(subProperty, processedData);
        });
        if (MapUtils.isNotEmpty(complexData)) {
          complexDataTargets.add(complexData);
        }
      });
      processComplexData(instanceObject, complexDataTargetsMap);
    });
  }

  private void processDataFieldRules(RuleExecutionContext ruleExecutionContext) {
    ObjectNode instanceObject = ruleExecutionContext.getInstanceObject();
    DataField dataField = ruleExecutionContext.getDataField();
    String tag = dataField.getTag();

    final Set<String> ignoredSubsequentFields = new HashSet<>();

    ruleExecutionContext.getMappingFields().forEach(mappingField -> {

      Map<String, List<MappingEntity>> mappingEntitiesPerComplexObject = new HashMap<>();
      Map<String, List<MappingEntity>> mappingEntitiesPerSimpleProperty = new HashMap<>();

      Iterator<MappingEntity> mappingEntities = mappingField.getEntity().iterator();
      while (mappingEntities.hasNext()) {
        MappingEntity mappingEntity = mappingEntities.next();
        if (mappingEntity.isIgnoreSubsequentFields()) {
          if (ignoredSubsequentFields.contains(tag)) {
            continue;
          } else {
            ignoredSubsequentFields.add(tag);
          }
        }
        if (recordHasAllRequiredSubfields(dataField, mappingEntity.getRequiredSubfield())) {
          Optional<SubfieldSplit> subfieldSplit = mappingEntity.getSubFieldSplit();
          if (subfieldSplit.isPresent()) {
            expandSubfields(dataField.getSubfields(), subfieldSplit.get());
          }
          String target = mappingEntity.getTarget();
          if (target.contains(PERIOD)) {
            String[] path = splitPath(target);
            appendMappingEntity(mappingEntitiesPerComplexObject, path[0], mappingEntity);
          } else {
            appendMappingEntity(mappingEntitiesPerSimpleProperty, target, mappingEntity);
          }
        }
      }

      boolean entityPerRepeatedSubfield = mappingField.isEntityPerRepeatedSubfield();

      mappingEntitiesPerSimpleProperty.entrySet().forEach(entry -> {
        Map<String, String> simpleData = new HashMap<String, String>();
        entry.getValue().stream().forEach(mappingEntity -> processSubfields(mappingEntity, ruleExecutionContext, entityPerRepeatedSubfield, simpleData));
        simpleData.entrySet().forEach(simpleDataEntry -> processSimpleData(instanceObject, simpleDataEntry.getKey(), simpleDataEntry.getValue()));
      });

      Map<String, List<Map<String, String>>> complexDataTargetsMap = new HashMap<>();

      mappingEntitiesPerComplexObject.entrySet().stream().forEach(entry -> {
        String property = entry.getKey();
        List<Map<String, String>> complexDataTargets = getComplexDataTargets(complexDataTargetsMap, property);
        Map<String, String> complexData = new HashMap<String, String>();
        entry.getValue().stream().forEach(mappingEntity -> processSubfields(mappingEntity, ruleExecutionContext, entityPerRepeatedSubfield, complexData));
        if (MapUtils.isNotEmpty(complexData)) {
          complexDataTargets.add(complexData);
        }
      });
      processComplexData(instanceObject, complexDataTargetsMap);
    });
  }

  private String[] splitPath(String target) {
    String[] path = target.split(PERIOD_REGEX);
    if (path.length > 2) {
      throw new RuntimeException("Rules target path only supports single level of nesting!");
    }
    return path;
  }

  private void appendMappingEntity(Map<String, List<MappingEntity>> mappingEntitiesPerProperty, String property, MappingEntity mappingEntity) {
    List<MappingEntity> complexObjectMappingEntities;
    if (mappingEntitiesPerProperty.containsKey(property)) {
      complexObjectMappingEntities = mappingEntitiesPerProperty.get(property);
    } else {
      complexObjectMappingEntities = new ArrayList<>();
      mappingEntitiesPerProperty.put(property, complexObjectMappingEntities);
    }
    complexObjectMappingEntities.add(mappingEntity);
  }

  private List<Map<String, String>> getComplexDataTargets(Map<String, List<Map<String, String>>> complexDataTargetsMap, String property) {
    List<Map<String, String>> complexDataTargets;
    if (complexDataTargetsMap.containsKey(property)) {
      complexDataTargets = complexDataTargetsMap.get(property);
    } else {
      complexDataTargets = new ArrayList<>();
      complexDataTargetsMap.put(property, complexDataTargets);
    }
    return complexDataTargets;
  }

  private boolean recordHasAllRequiredSubfields(DataField dataField, List<String> requiredSubfields) {
    if (CollectionUtils.isNotEmpty(requiredSubfields)) {
      // @formatter:off
      return dataField.getSubfields().stream()
        .map(subfield -> String.valueOf(subfield.getCode()))
        .collect(Collectors.toSet())
        .containsAll(requiredSubfields);
      // @formatter:on
    }
    return true;
  }

  private void expandSubfields(List<Subfield> subFields, SubfieldSplit subfieldSplit) {
    List<Subfield> expandedSubs = new ArrayList<>();
    String func = subfieldSplit.getType();
    String param = subfieldSplit.getValue();
    for (Subfield subField : subFields) {
      String data = subField.getData();
      Iterator<?> splitData = NormalizationFunctionRunner.runSplitFunction(func, data, param);
      // NOTE: ignoring custom functions
      while (splitData.hasNext()) {
        String newData = (String) splitData.next();
        Subfield expandedSub = new SubfieldImpl(subField.getCode(), newData);
        expandedSubs.add(expandedSub);
      }
    }
    subFields.clear();
    subFields.addAll(expandedSubs);
  }

  private void processSubfields(MappingEntity mappingEntity, RuleExecutionContext ruleExecutionContext, boolean entityPerRepeatedSubfield,
      Map<String, String> dataMap) {
    String target = mappingEntity.getTarget();
    String property = target.contains(PERIOD) ? target.split(PERIOD_REGEX)[1] : target;

    boolean applyRulesOnConcatenatedData = mappingEntity.isApplyRulesOnConcatenatedData();

    List<SubfieldDelimiter> subfieldDelimiters = mappingEntity.getSubFieldDelimiter();

    String sfSpec = String.join(COMMA, mappingEntity.getSubfield().stream().map(code -> String.valueOf(code)).collect(Collectors.toList()));

    LinkedHashSet<DataAndDelimiter> concatedDataAndDelimiter = new LinkedHashSet<>();

    Set<Character> processedSubfields = new HashSet<Character>();
    Iterator<Subfield> subfields = ruleExecutionContext.getDataField().getSubfields(sfSpec).iterator();
    while (subfields.hasNext()) {
      Subfield subfield = subfields.next();
      // NOTE: preventing duplicate subfields
      if (processedSubfields.contains(subfield.getCode())) {
        continue;
      } else {
        processedSubfields.add(subfield.getCode());
      }

      DataAndDelimiter dataAndDelimiter = new DataAndDelimiter();

      dataAndDelimiter.setData(subfield.getData());

      if (!applyRulesOnConcatenatedData) {
        dataAndDelimiter.setData(processRules(mappingEntity, ruleExecutionContext.withSubfieldValue(dataAndDelimiter.getData())));
      }

      Optional<SubfieldDelimiter> subfieldDelimiter = subfieldDelimiters.stream().filter(sd -> sd.getSubfields().contains(subfield.getCode())).findFirst();

      if (subfields.hasNext() && subfieldDelimiter.isPresent()) {
        dataAndDelimiter.setDelimiter(subfieldDelimiter.get().getValue());
      }

      concatedDataAndDelimiter.add(dataAndDelimiter);

      if (entityPerRepeatedSubfield) {
        if (StringUtils.isNotEmpty(dataAndDelimiter.getData())) {
          dataMap.put(property, dataAndDelimiter.getData());
        }
      }

    }

    if (!entityPerRepeatedSubfield) {

      String data = concatedDataAndDelimiter.stream().map(dd -> dd.getData() + dd.getDelimiter()).reduce(StringUtils.EMPTY, String::concat).trim();

      if (applyRulesOnConcatenatedData) {
        data = processRules(mappingEntity, ruleExecutionContext.withSubfieldValue(data));
      }

      if (StringUtils.isNotEmpty(data)) {
        dataMap.put(property, data);
      }

    }
  }

  private String processRules(MappingEntity mappingEntity, RuleExecutionContext ruleExecutionContext) {

    // NOTE: for conditional rules checking for value exists and setting to a
    // constant value does redundant checks over all subfields entity

    String originalData = ruleExecutionContext.getSubfieldValue();

    // each rule can contain multiple conditions that need to be met and
    // a value to inject in case all the conditions are met
    Iterator<MappingRule> mappingRules = mappingEntity.getRules().iterator();

    while (mappingRules.hasNext()) {
      MappingRule mappingRule = mappingRules.next();

      // constant value to inject if conditions are met
      String ruleConstVal = mappingRule.getValue();

      boolean conditionsMet = true;

      Iterator<RuleCondition> mappingConditions = mappingRule.getConditions().iterator();

      while (mappingConditions.hasNext()) {
        RuleCondition mappingCondition = mappingConditions.next();

        String valueParam = mappingCondition.getValue();

        if (mappingCondition.getLDR() != null) {
          ruleExecutionContext.setSubfieldValue(ruleExecutionContext.getLeader().toString());
        }

        ruleExecutionContext.setRuleParameter(mappingCondition.getParameter());

        String[] functions = mappingCondition.getType().split(COMMA);

        for (int i = 0; i < functions.length; i++) {
          // NOTE: ignoring custom functions

          String c = NormalizationFunctionRunner.runFunction(functions[i].trim(), ruleExecutionContext);

          if (StringUtils.isNotEmpty(valueParam) && !c.equals(valueParam)) {
            conditionsMet = false;
            break;
          } else if (StringUtils.isEmpty(ruleConstVal)) {
            ruleExecutionContext.setSubfieldValue(c);
          }
        }

        if (!conditionsMet) {
          ruleExecutionContext.setSubfieldValue(originalData);
          break;
        }
      }

      if (conditionsMet && StringUtils.isNotEmpty(ruleConstVal)) {
        ruleExecutionContext.setSubfieldValue(ruleConstVal);
        break;
      }
    }

    return ruleExecutionContext.getSubfieldValue();
  }

  private void processComplexData(ObjectNode instanceObject, Map<String, List<Map<String, String>>> complexDataTargetsMap) {
    complexDataTargetsMap.entrySet().stream().forEach(entry -> {
      String target = entry.getKey();
      entry.getValue().forEach(complexData -> {
        try {
          Field field = Instance.class.getDeclaredField(target);
          boolean isCollection = Collection.class.isAssignableFrom(field.getType());
          ObjectNode complextObject = isCollection ? objectMapper.createObjectNode() : instanceObject.with(target);
          complexData.entrySet().stream().forEach(complexEntry -> {
            String complexTarget = complexEntry.getKey();
            String complexValue = complexEntry.getValue();
            complextObject.put(complexTarget, complexValue);
          });
          if (isCollection) {
            addToCollection(instanceObject, target, complextObject);
          }
        } catch (NoSuchFieldException e) {
          e.printStackTrace();
        } catch (SecurityException e) {
          e.printStackTrace();
        }
      });
    });
  }

  private void processSimpleData(ObjectNode instanceObject, String target, String data) {
    try {
      Field field = Instance.class.getDeclaredField(target);
      if (Collection.class.isAssignableFrom(field.getType())) {
        addToCollection(instanceObject, target, data);
      } else if (String.class.isAssignableFrom(field.getType())) {
        instanceObject.put(target, data);
      }
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
  }

  private void addToCollection(ObjectNode instanceObject, String target, String data) {
    getCollection(instanceObject, target).add(data);
  }

  private void addToCollection(ObjectNode instanceObject, String target, ObjectNode complextObject) {
    getCollection(instanceObject, target).add(complextObject);
  }

  private ArrayNode getCollection(ObjectNode instanceObject, String target) {
    // TODO: if node exists with target, check if array node
    return instanceObject.has(target) ? (ArrayNode) instanceObject.get(target) : instanceObject.withArray(target);
  }

  private void loadRules(JsonNode rulesNode) throws IOException {
    // @formatter:off
    ObjectReader fieldReader = objectMapper.readerFor(new TypeReference<MappingField>() {});
    ObjectReader entityReader = objectMapper.readerFor(new TypeReference<MappingEntity>() {});
    // @formatter:on
    Iterator<String> tags = rulesNode.fieldNames();
    while (tags.hasNext()) {
      String tag = tags.next();
      ArrayNode rules = (ArrayNode) rulesNode.get(tag);
      List<MappingField> mappingFields = new ArrayList<>();
      List<MappingEntity> mappingEntities = new ArrayList<>();
      Iterator<JsonNode> rulesIterator = rules.iterator();
      while (rulesIterator.hasNext()) {
        JsonNode ruleNode = rulesIterator.next();
        if (ruleNode.has("entity")) {
          mappingFields.add(fieldReader.readValue(ruleNode));
        } else {
          mappingEntities.add(entityReader.readValue(ruleNode));
        }
      }
      if (CollectionUtils.isNotEmpty(mappingFields)) {
        addFields(tag, mappingFields);
      }
      if (CollectionUtils.isNotEmpty(mappingEntities)) {
        addEntities(tag, mappingEntities);
      }
    }
  }

  private void addFields(String key, List<MappingField> fields) {
    if (CONTROL_FIELDS.contains(key)) {
      this.controlMappingFields.put(key, fields);
    } else {
      this.dataMappingFields.put(key, fields);
    }
  }

  private void addEntities(String key, List<MappingEntity> entities) {
    List<MappingField> mappingFields = Arrays.asList(new MappingField[] { new MappingField(entities) });
    if (CONTROL_FIELDS.contains(key)) {
      this.controlMappingFields.put(key, mappingFields);
    } else {
      this.dataMappingFields.put(key, mappingFields);
    }
  }

  private class DataAndDelimiter {

    private String data;

    private String delimiter;

    public DataAndDelimiter() {
      delimiter = StringUtils.SPACE;
    }

    public String getData() {
      return data;
    }

    public void setData(String data) {
      this.data = data;
    }

    public String getDelimiter() {
      return delimiter;
    }

    public void setDelimiter(String delimiter) {
      this.delimiter = delimiter;
    }

    @Override
    public boolean equals(Object other) {
      return ObjectUtils.isNotEmpty(other) && StringUtils.isNoneEmpty(data) && data.equals(((DataAndDelimiter) other).getData());
    }

    @Override
    public int hashCode() {
      return data.hashCode();
    }

  }

}
