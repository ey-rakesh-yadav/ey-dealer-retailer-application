package com.scl.occ.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({@Parameter(
		   name = "baseSiteId",
		   description = "Base site identifier",
		   required = true,
		   schema = @Schema(type = "string"),
		   in = ParameterIn.PATH
		),@Parameter(name = "territory", description = "Sub area pk", required = false, schema = @Schema(type = "string"),  in = ParameterIn.QUERY)})
public @interface ApiBaseSiteIdAndTerritoryParam {
}
