package com.biapay.accountmanagement.dto.response;

public enum ResponseCode {
    SUCCESS(200, "Success"),
    CREATED(201, "Successfully Created"),
    ACCEPTED(202, "Your request is processing"),
    NO_CONTENT(204, "No content found!"),

    BAD_REQUEST(400, "Invalid or bad request!"),
    UNAUTHORIZED(401, "You are not authorize for this operation!"),
    FORBIDDEN(403, "Forbidden! You are not authorize for this operation!"),
    NOT_FOUND(404, "Resource not Found!"),
    CONFLICT(409, "Conflict"),
    INTERNAL_ERROR(500, "Error");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
