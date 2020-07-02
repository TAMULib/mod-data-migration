package org.folio.rest.migration.model.request;

public class MfhdItem {
    private final String chron;
    private final String itemEnum;

    public MfhdItem(String chron, String itemEnum) {
        this.chron = chron;
        this.itemEnum = itemEnum;
    }

    public String getChron() {
        return chron;
    }

    public String getItemEnum() {
        return itemEnum;
    }

}
