package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

public class ItemExtraction extends AbstractExtraction {
    
    @NotNull
    private String mfhdSql;

    @NotNull
    private String barcodeSql;

    public ItemExtraction() {
        super();
    }

    public String getMfhdSql() {
        return mfhdSql;
    }

    public void setMfhdSql(String mfhdSql) {
        this.mfhdSql = mfhdSql;
    }

    public String getBarcodeSql() {
        return barcodeSql;
    }

    public void setBarcodeSql(String barcodeSql) {
        this.barcodeSql = barcodeSql;
    }

}
