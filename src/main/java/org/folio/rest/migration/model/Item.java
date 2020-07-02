package org.folio.rest.migration.model;

import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.model.Status.Name;
import org.folio.rest.migration.model.request.MfhdItem;

public class Item {
    private final String id;
    private final String barcode;

    private final String enumeration;
    private final String chronology;

    private final String permanentLocationId;
    private final String[] formerIds;
    private final int numberOfPieces;
    private final String materialTypeId;
    private final Status status;
    private final String permanentLoanTypeId;
    private final String temporaryLoanTypeId;
    private final String temporaryLocationId;

    public Item(String id, String barcode, MfhdItem mfhdItem, String permanentLocationId, String formerId, int numberOfPieces,
            String materialTypeId, Name status, String permanentLoanTypeId, String temporaryLoanTypeId,
            String temporaryLocationId) {
        this.id = id;
        this.barcode = barcode;
        this.enumeration = mfhdItem.getItemEnum();
        this.chronology = mfhdItem.getChron();
        this.permanentLocationId = permanentLocationId;
        this.formerIds = new String[] { formerId };
        this.numberOfPieces = numberOfPieces;
        this.materialTypeId = materialTypeId;
        this.status = new Status();
        this.status.setName(status);
        this.permanentLoanTypeId = permanentLoanTypeId;
        this.temporaryLoanTypeId = temporaryLoanTypeId;
        this.temporaryLocationId = temporaryLocationId;
    }

    public String getId() {
        return id;
    }

    public String getTemporaryLocationId() {
        return temporaryLocationId;
    }

    public String getTemporaryLoanTypeId() {
        return temporaryLoanTypeId;
    }

    public String getMaterialTypeId() {
        return materialTypeId;
    }

    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    public String[] getFormerIds() {
        return formerIds;
    }

    public String getPermanentLocationId() {
        return permanentLocationId;
    }

    public String getChronology() {
        return chronology;
    }

    public String getEnumeration() {
        return enumeration;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getPermanentLoanTypeId() {
        return permanentLoanTypeId;
    }

}
