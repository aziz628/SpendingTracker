package com.example.budgetmanager.dto.results;
/**
 * GENERIC RESULT CLASS - DEVELOPER GUIDE
 *
 * PURPOSE: Universal container for service method responses
 * allow a consistent error handling across entire app
 *
 * DESIGN: Inspired by ResponseEntity<T> in Spring Boot
 * - success: Quick boolean check
 * - data: Actual result object (User, List<Transaction>, etc)
 * - error: Descriptive message when things go wrong
 *
 * USAGE PATTERN:
 * Result<User> result = authService.login(request);
 * if (result.isSuccess()) { handle result.getData() }
 * else { handle result.getError() }
 */
public class Result<T> {
    private final T data;
    private final String error;
    private final boolean success;

    // Private constructor - use factory methods
    private Result(T data, String error, boolean success) {
        this.data = data;
        this.error = error;
        this.success = success;
    }

    /**
     * Success factory method - wraps successful data
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, true);
    }

    /**
     * Error factory method - wraps error message
     */
    public static <T> Result<T> error(String error) {
        return new Result<>(null, error, false);
    }

    // Getters
    public T getData() { return data; }
    public String getError() { return error; }
    public boolean isSuccess() { return success; }
}