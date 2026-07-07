package com.gateway_service;

public record ErrorResponse(
		int status,
		String message,
		long timestamp
) {}
