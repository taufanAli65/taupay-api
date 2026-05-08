package com.example.demo.dtos.responses;

import lombok.Data;

import java.util.UUID;

@Data
public class BaseResponse<T> {
    private UUID reqId = UUID.randomUUID();
    private String status = "T";
    private String message = "Success";
    private T data;
    private ResPaginationDto pagination;

    public static <T> BaseResponse<T> success(String message, T data, ResPaginationDto pagination) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setStatus("T");
        response.setMessage(message);
        response.setData(data);
        response.setPagination(pagination);
        return response;
    }

    public static <T> BaseResponse<T> error(String message, T data, ResPaginationDto pagination) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setStatus("F");
        response.setMessage(message);
        response.setData(data);
        response.setPagination(pagination);
        return response;
    }

    public static <T> BaseResponse<T> error(String message, T data) {
        return error(message, data, null);
    }

}
