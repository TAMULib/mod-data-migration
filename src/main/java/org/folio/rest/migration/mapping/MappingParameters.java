package org.folio.rest.migration.mapping;

import org.apache.commons.collections4.list.UnmodifiableList;
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

public class MappingParameters {

  private UnmodifiableList<Identifiertype> identifierTypes;
  private UnmodifiableList<Classificationtype> classificationTypes;
  private UnmodifiableList<Instancetype> instanceTypes;
  private UnmodifiableList<Instanceformat> instanceFormats;
  private UnmodifiableList<Contributortype> contributorTypes;
  private UnmodifiableList<Contributornametype> contributorNameTypes;
  private UnmodifiableList<Electronicaccessrelationship> electronicAccessRelationships;
  private UnmodifiableList<Instancenotetype> instanceNoteTypes;
  private UnmodifiableList<Alternativetitletype> alternativeTitleTypes;
  private UnmodifiableList<IssuanceMode> issuanceModes;

  public MappingParameters() {

  }

  public UnmodifiableList<Identifiertype> getIdentifierTypes() {
    return identifierTypes;
  }

  public void setIdentifierTypes(UnmodifiableList<Identifiertype> identifierTypes) {
    this.identifierTypes = identifierTypes;
  }

  public UnmodifiableList<Classificationtype> getClassificationTypes() {
    return classificationTypes;
  }

  public void setClassificationTypes(UnmodifiableList<Classificationtype> classificationTypes) {
    this.classificationTypes = classificationTypes;
  }

  public UnmodifiableList<Instancetype> getInstanceTypes() {
    return instanceTypes;
  }

  public void setInstanceTypes(UnmodifiableList<Instancetype> instanceTypes) {
    this.instanceTypes = instanceTypes;
  }

  public UnmodifiableList<Instanceformat> getInstanceFormats() {
    return instanceFormats;
  }

  public void setInstanceFormats(UnmodifiableList<Instanceformat> instanceFormats) {
    this.instanceFormats = instanceFormats;
  }

  public UnmodifiableList<Contributortype> getContributorTypes() {
    return contributorTypes;
  }

  public void setContributorTypes(UnmodifiableList<Contributortype> contributorTypes) {
    this.contributorTypes = contributorTypes;
  }

  public UnmodifiableList<Contributornametype> getContributorNameTypes() {
    return contributorNameTypes;
  }

  public void setContributorNameTypes(UnmodifiableList<Contributornametype> contributorNameTypes) {
    this.contributorNameTypes = contributorNameTypes;
  }

  public UnmodifiableList<Electronicaccessrelationship> getElectronicAccessRelationships() {
    return electronicAccessRelationships;
  }

  public void setElectronicAccessRelationships(UnmodifiableList<Electronicaccessrelationship> electronicAccessRelationships) {
    this.electronicAccessRelationships = electronicAccessRelationships;
  }

  public UnmodifiableList<Instancenotetype> getInstanceNoteTypes() {
    return instanceNoteTypes;
  }

  public void setInstanceNoteTypes(UnmodifiableList<Instancenotetype> instanceNoteTypes) {
    this.instanceNoteTypes = instanceNoteTypes;
  }

  public UnmodifiableList<Alternativetitletype> getAlternativeTitleTypes() {
    return alternativeTitleTypes;
  }

  public void setAlternativeTitleTypes(UnmodifiableList<Alternativetitletype> alternativeTitleTypes) {
    this.alternativeTitleTypes = alternativeTitleTypes;
  }

  public UnmodifiableList<IssuanceMode> getIssuanceModes() {
    return issuanceModes;
  }

  public void setIssuanceModes(UnmodifiableList<IssuanceMode> issuanceModes) {
    this.issuanceModes = issuanceModes;
  }

}