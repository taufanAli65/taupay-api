package com.example.demo.dtos.responses;

import lombok.Data;

import java.util.UUID;

@Data
public class BaseResponse<T> {
    private UUID reqId = UUID.randomUUID();
    private String status = "T";
    private String message = "Success";
    private T data;

    public static <T> BaseResponse<T> success(String message, T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setStatus("T");
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> BaseResponse<T> error(String message, T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setStatus("F");
        response.setMessage(message);
        response.setData(data);
        return response;
    }

}
