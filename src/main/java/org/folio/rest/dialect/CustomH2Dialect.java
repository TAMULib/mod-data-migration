package org.folio.rest.dialect;

import org.folio.rest.dialect.type.StringArrayUserType;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

public class CustomH2Dialect extends H2Dialect {

	public CustomH2Dialect() {
		super();
		Type arrayType = new CustomType(StringArrayUserType.INSTANCE);
		registerFunction("array_agg", new SQLFunctionTemplate(arrayType, "array_agg(?1)"));
	}

}
