package br.com.likwi.aws;

import com.google.gson.GsonBuilder;

public class ErrorResponse {
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static String build(ErrorResponse errorResponse){
        return new GsonBuilder().serializeNulls()
                .create()
                .toJson(errorResponse, ErrorResponse.class);
    }
}
