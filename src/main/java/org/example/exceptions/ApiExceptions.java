package org.example.exceptions;


// ApiExceptions.java

public class ApiExceptions extends Throwable {

    // ------------------- Base Exception -------------------
    public static class ApiException extends RuntimeException {
        private final int status;
        private final String responseBody;

        public ApiException(int status, String message, String responseBody) {
            super(message);
            this.status = status;
            this.responseBody = responseBody;
        }

        public int getStatus() {
            return status;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }
    public static ApiExceptions.ApiException fromStatus(int code, String body) {
        return switch (code) {
            case 400 -> new ApiExceptions.BadRequestException(body);
            case 401 -> new ApiExceptions.UnauthorizedException(body);
            case 403 -> new ApiExceptions.ForbiddenException(body);
            case 404 -> new ApiExceptions.NotFoundException(body);
            case 405 -> new ApiExceptions.MethodNotAllowedException(body);
            case 409 -> new ApiExceptions.ConflictException(body);
            case 410 -> new ApiExceptions.GoneException(body);
            case 422 -> new ApiExceptions.UnprocessableEntityException(body);
            case 429 -> new ApiExceptions.RateLimitException(body);
            case 500 -> new ApiExceptions.InternalServerErrorException(body);
            case 501 -> new ApiExceptions.NotImplementedException(body);
            case 502 -> new ApiExceptions.BadGatewayException(body);
            case 503 -> new ApiExceptions.ServiceUnavailableException(body);
            case 504 -> new ApiExceptions.GatewayTimeoutException(body);
            default -> new ApiExceptions.ApiException(code, "Unexpected HTTP Error", body);
        };
    }
    // ------------------- 4xx Client Errors -------------------
    public static class BadRequestException extends ApiException {
        public BadRequestException(String details) { super(400, "Bad Request", details); }
    }

    public static class UnauthorizedException extends ApiException {
        public UnauthorizedException(String details) { super(401, "Unauthorized", details); }
    }

    public static class ForbiddenException extends ApiException {
        public ForbiddenException(String details) { super(403, "Forbidden", details); }
    }

    public static class NotFoundException extends ApiException {
        public NotFoundException(String details) { super(404, "Not Found", details); }
    }

    public static class MethodNotAllowedException extends ApiException {
        public MethodNotAllowedException(String details) { super(405, "Method Not Allowed", details); }
    }

    public static class ConflictException extends ApiException {
        public ConflictException(String details) {
            super(409, details, details);

        }

    }

    public static class GoneException extends ApiException {
        public GoneException(String details) { super(410, "Gone", details); }
    }

    public static class UnprocessableEntityException extends ApiException {
        public UnprocessableEntityException(String details) { super(422, "Unprocessable Entity", details); }
    }

    public static class RateLimitException extends ApiException {
        public RateLimitException(String details) { super(429, "Too Many Requests", details); }
    }

    // ------------------- 5xx Server Errors -------------------
    public static class InternalServerErrorException extends ApiException {
        public InternalServerErrorException(String details) { super(500, "Internal Server Error", details); }
    }

    public static class NotImplementedException extends ApiException {
        public NotImplementedException(String details) { super(501, "Not Implemented", details); }
    }

    public static class BadGatewayException extends ApiException {
        public BadGatewayException(String details) { super(502, "Bad Gateway", details); }
    }

    public static class ServiceUnavailableException extends ApiException {
        public ServiceUnavailableException(String details) { super(503, "Service Unavailable", details); }
    }

    public static class GatewayTimeoutException extends ApiException {
        public GatewayTimeoutException(String details) { super(504, "Gateway Timeout", details); }
    }
}
