package com.artvsart.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class VoterCookieManager {

    public static final String COOKIE_NAME =
            "artvsart_voter";

    public boolean isValid(String voterId) {
        if (voterId == null) {
            return false;
        }

        try {
            UUID.fromString(voterId);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String getOrCreate(
            String voterId,
            HttpServletResponse response
    ) {
        if (isValid(voterId)) {
            return voterId;
        }

        String newVoterId = UUID.randomUUID().toString();

        ResponseCookie voterCookie = ResponseCookie
                .from(COOKIE_NAME, newVoterId)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(365))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                voterCookie.toString()
        );

        return newVoterId;
    }
}