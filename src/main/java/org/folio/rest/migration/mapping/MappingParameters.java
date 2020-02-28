package org.folio.rest.migration.mapping;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.folio.rest.migration.model.generated.settings.AlternativeTitleType;
import org.folio.rest.migration.model.generated.settings.ClassificationType;
import org.folio.rest.migration.model.generated.settings.ContributorNameType;
import org.folio.rest.migration.model.generated.settings.ContributorType;
import org.folio.rest.migration.model.generated.settings.ElectronicAccessRelationship;
import org.folio.rest.migration.model.generated.settings.IdentifierType;
import org.folio.rest.migration.model.generated.settings.InstanceFormat;
import org.folio.rest.migration.model.generated.settings.InstanceNoteType;
import org.folio.rest.migration.model.generated.settings.InstanceType;

public class MappingParameters {

  private UnmodifiableList<IdentifierType> identifierTypes;
  private UnmodifiableList<ClassificationType> classificationTypes;
  private UnmodifiableList<InstanceType> instanceTypes;
  private UnmodifiableList<InstanceFormat> instanceFormats;
  private UnmodifiableList<ContributorType> contributorTypes;
  private UnmodifiableList<ContributorNameType> contributorNameTypes;
  private UnmodifiableList<ElectronicAccessRelationship> electronicAccessRelationships;
  private UnmodifiableList<InstanceNoteType> instanceNoteTypes;
  private UnmodifiableList<AlternativeTitleType> alternativeTitleTypes;

  public MappingParameters() {

  }

  public UnmodifiableList<IdentifierType> getIdentifierTypes() {
    return identifierTypes;
  }

  public void setIdentifierTypes(UnmodifiableList<IdentifierType> identifierTypes) {
    this.identifierTypes = identifierTypes;
  }

  public UnmodifiableList<ClassificationType> getClassificationTypes() {
    return classificationTypes;
  }

  public void setClassificationTypes(UnmodifiableList<ClassificationType> classificationTypes) {
    this.classificationTypes = classificationTypes;
  }

  public UnmodifiableList<InstanceType> getInstanceTypes() {
    return instanceTypes;
  }

  public void setInstanceTypes(UnmodifiableList<InstanceType> instanceTypes) {
    this.instanceTypes = instanceTypes;
  }

  public UnmodifiableList<InstanceFormat> getInstanceFormats() {
    return instanceFormats;
  }

  public void setInstanceFormats(UnmodifiableList<InstanceFormat> instanceFormats) {
    this.instanceFormats = instanceFormats;
  }

  public UnmodifiableList<ContributorType> getContributorTypes() {
    return contributorTypes;
  }

  public void setContributorTypes(UnmodifiableList<ContributorType> contributorTypes) {
    this.contributorTypes = contributorTypes;
  }

  public UnmodifiableList<ContributorNameType> getContributorNameTypes() {
    return contributorNameTypes;
  }

  public void setContributorNameTypes(UnmodifiableList<ContributorNameType> contributorNameTypes) {
    this.contributorNameTypes = contributorNameTypes;
  }

  public UnmodifiableList<ElectronicAccessRelationship> getElectronicAccessRelationships() {
    return electronicAccessRelationships;
  }

  public void setElectronicAccessRelationships(UnmodifiableList<ElectronicAccessRelationship> electronicAccessRelationships) {
    this.electronicAccessRelationships = electronicAccessRelationships;
  }

  public UnmodifiableList<InstanceNoteType> getInstanceNoteTypes() {
    return instanceNoteTypes;
  }

  public void setInstanceNoteTypes(UnmodifiableList<InstanceNoteType> instanceNoteTypes) {
    this.instanceNoteTypes = instanceNoteTypes;
  }

  public UnmodifiableList<AlternativeTitleType> getAlternativeTitleTypes() {
    return alternativeTitleTypes;
  }

  public void setAlternativeTitleTypes(UnmodifiableList<AlternativeTitleType> alternativeTitleTypes) {
    this.alternativeTitleTypes = alternativeTitleTypes;
  }

}