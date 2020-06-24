package org.folio.rest.migration.mapping.function;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.folio.IssuanceMode;
import org.folio.rest.jaxrs.model.Alternativetitletype;
import org.folio.rest.jaxrs.model.Classificationtype;
import org.folio.rest.jaxrs.model.Contributornametype;
import org.folio.rest.jaxrs.model.Contributortype;
import org.folio.rest.jaxrs.model.Electronicaccessrelationship;
import org.folio.rest.jaxrs.model.Identifiertype;
import org.folio.rest.jaxrs.model.Instanceformat;
import org.folio.rest.jaxrs.model.Instancenotetype;
import org.folio.rest.jaxrs.model.Instancetype;
import org.folio.rest.migration.mapping.RuleExecutionContext;
import org.folio.rest.migration.mapping.model.RuleParameter;
import org.marc4j.marc.DataField;

/**
 * Enumeration to store normalization functions
 */
public enum NormalizationFunction implements Function<RuleExecutionContext, String> {

  CHAR_SELECT() {
    @Override
    public String apply(RuleExecutionContext context) {
      String subFieldValue = context.getSubfieldValue();
      RuleParameter ruleParameter = context.getRuleParameter();
      if (ruleParameter != null && ruleParameter.getFrom() != null && ruleParameter.getTo() != null) {
        Integer from = ruleParameter.getFrom();
        Integer to = ruleParameter.getTo();
        return subFieldValue.substring(from, to);
      } else {
        return subFieldValue;
      }
    }
  },

  REMOVE_ENDING_PUNC() {
    private static final String PUNCT_2_REMOVE = ";:,/+= ";

    @Override
    public String apply(RuleExecutionContext context) {
      String subFieldValue = context.getSubfieldValue();
      if (!StringUtils.isEmpty(subFieldValue)) {
        int lastPosition = subFieldValue.length() - 1;
        if (PUNCT_2_REMOVE.contains(String.valueOf(subFieldValue.charAt(lastPosition)))) {
          return subFieldValue.substring(INTEGER_ZERO, lastPosition);
        }
      }
      return subFieldValue;
    }
  },

  TRIM() {
    @Override
    public String apply(RuleExecutionContext context) {
      return context.getSubfieldValue().trim();
    }
  },

  TRIM_PERIOD() {
    private static final String PERIOD = ".";

    @Override
    public String apply(RuleExecutionContext context) {
      String subFieldData = context.getSubfieldValue();
      if (subFieldData.endsWith(PERIOD)) {
        return subFieldData.substring(INTEGER_ZERO, subFieldData.length() - 1);
      }
      return subFieldData;
    }
  },

  REMOVE_SUBSTRING() {
    @Override
    public String apply(RuleExecutionContext context) {
      String subFieldValue = context.getSubfieldValue();
      RuleParameter ruleParameter = context.getRuleParameter();
      if (ruleParameter != null && ruleParameter.getSubstring() != null) {
        String substring = ruleParameter.getSubstring();
        return StringUtils.remove(subFieldValue, substring);
      } else {
        return subFieldValue;
      }
    }
  },

  REMOVE_PREFIX_BY_INDICATOR() {
    @Override
    public String apply(RuleExecutionContext context) {
      String subFieldData = context.getSubfieldValue();
      DataField dataField = context.getDataField();
      int from = INTEGER_ZERO;
      int to = Character.getNumericValue(dataField.getIndicator2());
      if (0 < to && to < subFieldData.length()) {
        String prefixToRemove = subFieldData.substring(from, to);
        return StringUtils.remove(subFieldData, prefixToRemove);
      } else {
        return subFieldData;
      }
    }
  },

  CAPITALIZE() {
    @Override
    public String apply(RuleExecutionContext context) {
      return StringUtils.capitalize(context.getSubfieldValue());
    }
  },

  SET_PUBLISHER_ROLE() {
    @Override
    public String apply(RuleExecutionContext context) {
      DataField dataField = context.getDataField();
      int indicator = Character.getNumericValue(dataField.getIndicator2());
      PublisherRole publisherRole = PublisherRole.getByIndicator(indicator);
      if (publisherRole == null) {
        return EMPTY_STRING;
      } else {
        return publisherRole.getCaption();
      }
    }
  },

  SET_INSTANCE_FORMAT_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      List<Instanceformat> instanceFormats = context.getMappingParameters().getInstanceFormats();
      if (instanceFormats == null) {
        return StringUtils.EMPTY;
      }
      return instanceFormats.stream().filter(instanceFormat -> instanceFormat.getCode().equalsIgnoreCase(context.getSubfieldValue())).findFirst()
          .map(Instanceformat::getId).orElse(StringUtils.EMPTY);
    }
  },

  SET_CLASSIFICATION_TYPE_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      String typeName = context.getRuleParameter().getName();
      List<Classificationtype> types = context.getMappingParameters().getClassificationTypes();
      if (types == null || typeName == null) {
        return STUB_FIELD_TYPE_ID;
      }
      return types.stream().filter(classificationType -> classificationType.getName().equalsIgnoreCase(typeName)).findFirst().map(Classificationtype::getId).orElse(STUB_FIELD_TYPE_ID);
    }
  },

  SET_CONTRIBUTOR_TYPE_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      List<Contributortype> types = context.getMappingParameters().getContributorTypes();
      if (types == null) {
        return StringUtils.EMPTY;
      }
      return types.stream().filter(type -> type.getCode().equalsIgnoreCase(context.getSubfieldValue())).findFirst().map(Contributortype::getId).orElse(StringUtils.EMPTY);
    }
  },

  SET_CONTRIBUTOR_TYPE_TEXT() {
    @Override
    public String apply(RuleExecutionContext context) {
      List<Contributortype> types = context.getMappingParameters().getContributorTypes();
      if (types == null) {
        return context.getSubfieldValue();
      }
      return types.stream().filter(type -> type.getCode().equalsIgnoreCase(context.getSubfieldValue())).findFirst().map(Contributortype::getName).orElse(context.getSubfieldValue());
    }
  },

  SET_CONTRIBUTOR_NAME_TYPE_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      String typeName = context.getRuleParameter().getName();
      List<Contributornametype> typeNames = context.getMappingParameters().getContributorNameTypes();
      if (typeNames == null || typeName == null) {
        return STUB_FIELD_TYPE_ID;
      }
      return typeNames.stream().filter(contributorTypeName -> contributorTypeName.getName().equalsIgnoreCase(typeName)).findFirst()
          .map(Contributornametype::getId).orElse(STUB_FIELD_TYPE_ID);
    }
  },

  SET_INSTANCE_TYPE_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      List<Instancetype> types = context.getMappingParameters().getInstanceTypes();
      if (types == null) {
        return STUB_FIELD_TYPE_ID;
      }
      String unspecifiedTypeCode = context.getRuleParameter().getUnspecifiedInstanceTypeCode();
      String instanceTypeCode = context.getDataField() != null ? context.getSubfieldValue() : unspecifiedTypeCode;

      return getInstanceTypeByCode(instanceTypeCode, types).map(Instancetype::getId)
      .orElseGet(() -> getInstanceTypeByCode(unspecifiedTypeCode, types).map(Instancetype::getId).orElse(STUB_FIELD_TYPE_ID));
    }

    private Optional<Instancetype> getInstanceTypeByCode(String instanceTypeCode, List<Instancetype> instanceTypes) {
      return instanceTypes.stream().filter(instanceType -> instanceType.getCode().equalsIgnoreCase(instanceTypeCode)).findFirst();
    }
  },

  SET_ELECTRONIC_ACCESS_RELATIONS_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      List<Electronicaccessrelationship> electronicAccessRelationships = context.getMappingParameters().getElectronicAccessRelationships();
      if (electronicAccessRelationships == null || context.getDataField() == null) {
        return STUB_FIELD_TYPE_ID;
      }
      char ind2 = context.getDataField().getIndicator2();
      String name = ElectronicAccessRelationshipEnum.getNameByIndicator(ind2);
      return electronicAccessRelationships.stream().filter(electronicAccessRelationship -> electronicAccessRelationship.getName().equalsIgnoreCase(name))
          .findFirst().map(Electronicaccessrelationship::getId).orElse(STUB_FIELD_TYPE_ID);
    }
  },

  SET_IDENTIFIER_TYPE_ID_BY_NAME() {
    @Override
    public String apply(RuleExecutionContext context) {
      String typeName = context.getRuleParameter().getName();
      List<Identifiertype> identifierTypes = context.getMappingParameters().getIdentifierTypes();
      if (identifierTypes == null || typeName == null) {
        return STUB_FIELD_TYPE_ID;
      }
      return identifierTypes.stream().filter(identifierType -> identifierType.getName().trim().equalsIgnoreCase(typeName)).findFirst()
          .map(Identifiertype::getId).orElse(STUB_FIELD_TYPE_ID);
    }
  },

  SET_IDENTIFIER_TYPE_ID_BY_VALUE() {
    @Override
    public String apply(RuleExecutionContext context) {
      List<String> typeNames = context.getRuleParameter().getNames();
      List<Identifiertype> identifierTypes = context.getMappingParameters().getIdentifierTypes();
      if (identifierTypes == null || typeNames == null) {
        return STUB_FIELD_TYPE_ID;
      }
      String type = getIdentifierTypeName(context);
      return identifierTypes.stream().filter(identifierType -> identifierType.getName().equalsIgnoreCase(type)).findFirst().map(Identifiertype::getId)
          .orElse(STUB_FIELD_TYPE_ID);
    }

    private String getIdentifierTypeName(RuleExecutionContext context) {
      List<String> typeNames = context.getRuleParameter().getNames();
      String oclcRegex = context.getRuleParameter().getOclc_regex();
      String type = typeNames.get(0);
      if (oclcRegex != null && context.getSubfieldValue().matches(oclcRegex)) {
        type = typeNames.get(1);
      }
      return type;
    }
  },

  SET_NOTE_TYPE_ID() {
    private static final String DEFAULT_NOTE_TYPE_NAME = "General note";

    @Override
    public String apply(RuleExecutionContext context) {
      String noteTypeName = context.getRuleParameter().getName();
      List<Instancenotetype> instanceNoteTypes = context.getMappingParameters().getInstanceNoteTypes();
      if (instanceNoteTypes == null || noteTypeName == null) {
        return STUB_FIELD_TYPE_ID;
      }
      return getNoteTypeByName(noteTypeName, instanceNoteTypes).map(Instancenotetype::getId)
          .orElseGet(() -> getNoteTypeByName(DEFAULT_NOTE_TYPE_NAME, instanceNoteTypes).map(Instancenotetype::getId).orElse(STUB_FIELD_TYPE_ID));
    }

    private Optional<Instancenotetype> getNoteTypeByName(String noteTypeName, List<Instancenotetype> noteTypes) {
      return noteTypes.stream().filter(instanceNoteType -> instanceNoteType.getName().equalsIgnoreCase(noteTypeName)).findFirst();
    }
  },

  SET_ALTERNATIVE_TITLE_TYPE_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      String alternativeTitleTypeName = context.getRuleParameter().getName();
      List<Alternativetitletype> alternativeTitleTypes = context.getMappingParameters().getAlternativeTitleTypes();
      if (alternativeTitleTypes == null || alternativeTitleTypeName == null) {
        return STUB_FIELD_TYPE_ID;
      }
      return alternativeTitleTypes.stream()
          .filter(alternativeTitleType -> alternativeTitleType.getName().equalsIgnoreCase(alternativeTitleTypeName)).findFirst().map(Alternativetitletype::getId).orElse(STUB_FIELD_TYPE_ID);
    }
  },

  SET_ISSUANCE_MODE_ID() {
    @Override
    public String apply(RuleExecutionContext context) {
      String subFieldValue = context.getSubfieldValue();
      char seventhChar = subFieldValue.charAt(7); //Regarding "MODSOURMAN-203" is should be 7-th symbol.
      List<IssuanceMode> issuanceModes = context.getMappingParameters().getIssuanceModes();
      if (issuanceModes == null || issuanceModes.isEmpty()) {
        return StringUtils.EMPTY;
      }
      String defaultIssuanceModeId = findIssuanceModeId(issuanceModes, IssuanceModeEnum.UNSPECIFIED, StringUtils.EMPTY);
      return matchIssuanceModeIdViaLeaderSymbol(seventhChar, issuanceModes, defaultIssuanceModeId);
    }

    private String findIssuanceModeId(List<IssuanceMode> issuanceModes, IssuanceModeEnum issuanceModeType, String defaultId) {
      return issuanceModes.stream()
        .filter(issuanceMode -> issuanceMode.getName().equalsIgnoreCase(issuanceModeType.getValue()))
        .findFirst()
        .map(IssuanceMode::getId)
        .orElse(defaultId);
    }

    private String matchIssuanceModeIdViaLeaderSymbol(char seventhChar, List<IssuanceMode> issuanceModes, String defaultId) {
      IssuanceModeEnum issuanceMode = matchSymbolToIssuanceMode(seventhChar);
      return findIssuanceModeId(issuanceModes, issuanceMode, defaultId);
    }
  };

  public IssuanceModeEnum matchSymbolToIssuanceMode(char symbol) {
    for (IssuanceModeEnum issuanceMode : IssuanceModeEnum.values()) {
      for (int i = 0; i < issuanceMode.getSymbols().length; i++) {
        if (issuanceMode.getSymbols()[i] == symbol) {
          return issuanceMode;
        }
      }
    }
    return IssuanceModeEnum.UNSPECIFIED;
  }

  private static final String STUB_FIELD_TYPE_ID = "fe19bae4-da28-472b-be90-d442e2428ead";
}