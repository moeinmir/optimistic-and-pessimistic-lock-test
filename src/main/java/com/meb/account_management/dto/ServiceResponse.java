package com.meb.account_management.dto;
import java.util.Optional;

public class ServiceResponse<T> {
    private final boolean success;
    private final T result;  // Always contains the unwrapped value (or null)

    private ServiceResponse(boolean success, T result) {
        this.success = success;
        this.result = result;
    }

    // For successful cases with direct values
    public static <T> ServiceResponse<T> success(T result) {
        return new ServiceResponse<>(true, result);
    }

    // For failures without payload
    public static <T> ServiceResponse<T> failure() {
        return new ServiceResponse<>(false, null);
    }

    // For failures with error payload
    public static <T> ServiceResponse<T> failure(T errorResult) {
        return new ServiceResponse<>(false, errorResult);
    }

    // Auto-unwraps Optional - returns the value if present, or null if not
    public static <T> ServiceResponse<T> fromOptional(Optional<T> optional) {
        return new ServiceResponse<>(optional.isPresent(), optional.orElse(null));
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public T getResult() {
        return result;
    }
}