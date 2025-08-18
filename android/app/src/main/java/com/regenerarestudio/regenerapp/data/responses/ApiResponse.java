package com.regenerarestudio.regenerapp.data.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Respuesta genérica de API con estado y mensaje
 */
public class ApiResponse<T> {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private String error;

    // Constructor vacío
    public ApiResponse() {}

    // Getters y Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public boolean isSuccessful() {
        return success != null && success;
    }
}