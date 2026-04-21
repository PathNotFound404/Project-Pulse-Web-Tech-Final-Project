package com.tcu.projectpulse.common.dto;

public record Result(boolean flag, int code, String message, Object data) {

    public static Result success(Object data) {
        return new Result(true, 200, "Success", data);
    }

    public static Result success(String message, Object data) {
        return new Result(true, 200, message, data);
    }

    public static Result error(int code, String message) {
        return new Result(false, code, message, null);
    }
}
