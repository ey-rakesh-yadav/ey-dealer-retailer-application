package com.eydms.occ.controllers;

import com.eydms.facades.brand.BrandFacade;
import com.eydms.facades.data.BrandData;
import com.eydms.occ.dto.BrandListWsDTO;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ws.rs.core.MediaType;
import java.util.List;

import org.springframework.stereotype.Controller;
@Controller
@RequestMapping(value = "{baseSiteId}/brands/")
@ApiVersion("v2")
@Tag(name = "Brands Management")
@PermitAll
public class BrandController
{
    @Resource
	BrandFacade brandFacade;
    @Resource(name = "dataMapper")
    private DataMapper dataMapper;
    private static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
	
	@RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllBrands", summary = " Brands List", description = "Get list of Brands")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<BrandData>> getAllBrands()
    {
        return ResponseEntity.status(HttpStatus.OK).body(brandFacade.findAllBrand());
    }
	
	@RequestMapping(value = "competitors", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getCompetitorsBrands", summary = " Competitors Brands List", description = "Get list of Competitors Brands")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<BrandData>> getCompetitorsBrands()
    {
        return ResponseEntity.status(HttpStatus.OK).body(brandFacade.getCompetitorsBrands());
    }

    @GetMapping(value = "allBrands")
    @ResponseBody
    @Operation(operationId = "getAllBrands", summary = "All Brands List", description = "Get list of All Brands")
    @ApiBaseSiteIdParam
    public BrandListWsDTO getAllBrands(@Parameter @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        var data = brandFacade.getAllBrand();
        return dataMapper.map(data, BrandListWsDTO.class, fields);
    }
}
