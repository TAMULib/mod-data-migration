package org.folio.rest.dialect;

import org.folio.rest.dialect.type.StringArrayUserType;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

public class CustomPostgreSQLDialect extends PostgreSQL82Dialect {

	public CustomPostgreSQLDialect() {
		super();
		Type arrayType = new CustomType(StringArrayUserType.INSTANCE);
		registerFunction("array_agg", new SQLFunctionTemplate(arrayType, "array_agg(?1)"));
	}

}