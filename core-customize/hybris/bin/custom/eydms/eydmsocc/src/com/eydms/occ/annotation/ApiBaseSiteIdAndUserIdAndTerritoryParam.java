package com.eydms.occ.annotation;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({@Parameter(
   name = "baseSiteId",
   description = "Base site identifier",
   required = true,
   schema = @Schema(type = "string"),
   in = ParameterIn.PATH
), @Parameter(
   name = "userId",
   description = "User identifier or one of the literals : 'current' for currently authenticated user, 'anonymous' for anonymous user",
   required = true,
   schema = @Schema(type = "string"),
   in = ParameterIn.PATH
), @Parameter(name = "territory", description = "Sub area pk", required = false, schema = @Schema(type = "string"),  in = ParameterIn.QUERY)
, @Parameter(name = "district", description = "District Code", required = false, schema = @Schema(type = "string"),  in = ParameterIn.QUERY)
, @Parameter(name = "region", description = "region Code", required = false, schema = @Schema(type = "string"),  in = ParameterIn.QUERY)})
public @interface ApiBaseSiteIdAndUserIdAndTerritoryParam {
}
