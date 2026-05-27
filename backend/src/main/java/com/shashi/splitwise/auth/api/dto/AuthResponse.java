package com.shashi.splitwise.auth.api.dto;

public record AuthResponse(String token, long expiresInSeconds, UserDto user) {}
