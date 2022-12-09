package com.testrigor.selfhealingselenium.application.utils;

import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class JsonHelpers {
	private static final String SERIALIZE_ERROR_MESSAGE = "Failed to serialize";
	private static final String DESERIALIZE_ERROR_MESSAGE = "Failed to deserialize";

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
		.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
		.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.registerModule(new JavaTimeModule())
		.registerModule(new JodaModule());

	public static String serializeJson(Object obj) {
		try {
			return JSON_MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(SERIALIZE_ERROR_MESSAGE, e);
		}
	}

	public static <T> T deserializeJson(byte[] json, Class<T> clazz) {
		try {
			return JSON_MAPPER.readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException(DESERIALIZE_ERROR_MESSAGE, e);
		}
	}

	public static <T> T deserializeJson(String json, Class<T> clazz) {
		try {
			return JSON_MAPPER.readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException(DESERIALIZE_ERROR_MESSAGE, e);
		}
	}

	public static <T> T deserializeJson(String json, TypeReference<T> typeReference) {
		try {
			return JSON_MAPPER.readValue(json, typeReference);
		} catch (IOException e) {
			throw new RuntimeException(DESERIALIZE_ERROR_MESSAGE, e);
		}
	}
}
