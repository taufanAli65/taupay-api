package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.slf4j.MDC;

@Data
public class BaseResponse<T> {
    @Schema(description = "Unique request identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private String reqId;

    @Schema(description = "Request status. `T` indicates success and `F` indicates failure.", example = "T")
    private String status = "T";

    @Schema(description = "Human-readable response message.", example = "Success")
    private String message = "Success";

    @Schema(description = "Endpoint-specific response payload.")
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Pagination metadata for list endpoints.")
    private ResPaginationDto pagination;

    public BaseResponse() {
        this.reqId = MDC.get("reqId");
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setStatus("T");
        response.setMessage(message);
        response.setData(data);
        return response;
    }

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
