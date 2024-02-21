/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.swaggerintegration.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

/**
 * One customisation implementation of {@link OpenApiCustomiser} that removes the name suffix "WsDTO"
 * from the output schema types. The suffix may appear in requestBody, responses, parameter, and schema definitions.
 *
 * @since 2211
 */
public class CommerceTypeNameProvider implements OpenApiCustomiser
{
	private static final String TYPE_NAME_SUFFIX = "WsDTO";

	@Override
	public void customise(final OpenAPI openApi)
	{
		sortAndRemoveSuffixFromSchemaDefinitions(openApi);

		final Paths paths = openApi.getPaths();
		if (paths == null || paths.isEmpty())
		{
			return;
		}
		removeSuffixFromOperationResponses(paths);
		removeSuffixFromOperationRequests(paths);
		removeSuffixFromParameters(paths);
	}

	private String getNameWithoutSuffix(final String name)
	{
		return StringUtils.removeEndIgnoreCase(name, TYPE_NAME_SUFFIX);
	}

	private boolean containsSuffix(final String name)
	{
		return StringUtils.endsWithIgnoreCase(name, TYPE_NAME_SUFFIX);
	}

	private void removeSuffixFromOperationResponses(final Paths paths)
	{
		final Stream<Content> contents = getOperations(paths)
				.map(Operation::getResponses)
				.filter(Objects::nonNull)
				.filter(apiResponses -> !apiResponses.isEmpty())
				.flatMap(apiResponses -> apiResponses.values().stream())
				.map(ApiResponse::getContent);
		removeSuffixFromContents(contents);
	}

	private void removeSuffixFromOperationRequests(final Paths paths)
	{
		final Stream<Content> contents = getOperations(paths)
				.map(Operation::getRequestBody)
				.filter(Objects::nonNull)
				.map(RequestBody::getContent);
		removeSuffixFromContents(contents);
	}

	private void removeSuffixFromParameters(final Paths paths)
	{
		final Stream<Schema> schemas = getOperations(paths)
				.filter(item -> item.getParameters() != null)
				.flatMap(item -> item.getParameters().stream())
				.map(Parameter::getSchema);
		replaceSchemas(schemas);
	}

	private Stream<Operation> getOperations(final Paths paths)
	{
		return paths.values().stream().flatMap(item -> item.readOperations().stream());
	}

	private void removeSuffixFromContents(final Stream<Content> contents)
	{
		final Stream<Schema> schemas = contents.filter(Objects::nonNull)
		                                       .flatMap(content -> content.values().stream())
		                                       .filter(Objects::nonNull)
		                                       .map(MediaType::getSchema)
		                                       .filter(Objects::nonNull);
		replaceSchemas(schemas);
	}

	private void replaceSchemas(final Stream<Schema> schemas)
	{
		schemas.forEach(this::doReplace);
	}

	private void doReplace(final Schema schema)
	{
			if (schema == null) {
		        return;
		    }
		    if (schema instanceof ArraySchema){
		        doReplace(schema.getItems());
		    }

		    final String ref = schema.get$ref();
		    if (ref != null && containsSuffix(ref))
		    {
		        schema.set$ref(getNameWithoutSuffix(ref));
		    }
	}

	private void sortAndRemoveSuffixFromSchemaDefinitions(final OpenAPI openApi)
	{
		final Components components = openApi.getComponents();
		if (components == null)
		{
			return;
		}

		final Map<String, Schema> schemas = components.getSchemas();
		if (schemas == null || schemas.isEmpty())
		{
			return;
		}

		final List<String> sortedKeys = schemas.keySet().stream().collect(Collectors.toList());
		sortedKeys.sort(String::compareTo);

		final Map<String, Schema> sortedSchemas = new LinkedHashMap<>();
		sortedKeys.forEach(name -> {
			final String nameWithoutSuffix = getNameWithoutSuffix(name);
			final Schema item = schemas.get(name);

			final Map<String, Schema> properties = item.getProperties();
			if (properties != null && !properties.isEmpty())
			{
				replaceSchemas(properties.values().stream());
				final Stream<Schema> schemasInProperties = properties.values().stream();
				replaceSchemas(schemasInProperties
						.filter(Objects::nonNull)
						.filter(s -> "array".equals(s.getType())
						).map(Schema::getItems));

				item.setName(nameWithoutSuffix);
				sortedSchemas.put(nameWithoutSuffix, item);
			}
		});
		components.setSchemas(sortedSchemas);
	}
}
