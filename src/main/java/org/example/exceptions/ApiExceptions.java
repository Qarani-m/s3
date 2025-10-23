package org.example.exceptions;

public class ApiExceptions extends Throwable {

    // ------------------- Base Exception -------------------
    public static class ApiException extends RuntimeException {
        private final int status;
        private final String responseBody;
        private final boolean retryable;

        public ApiException(int status, String message, String responseBody, boolean retryable) {
            super(message);
            this.status = status;
            this.responseBody = responseBody;
            this.retryable = retryable;
        }

        public int getStatus() {
            return status;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public boolean isRetryable() {
            return retryable;
        }
    }

    // ------------------- Retryable Base Classes -------------------

    /**
     * Base class for all 4xx client errors (generally not retryable)
     */
    public static abstract class ClientErrorException extends ApiException {
        public ClientErrorException(int status, String message, String responseBody) {
            super(status, message, responseBody, false);
        }
    }

    /**
     * Base class for all 5xx server errors (generally retryable)
     */
    public static abstract class ServerErrorException extends ApiException {
        public ServerErrorException(int status, String message, String responseBody) {
            super(status, message, responseBody, true);
        }
    }

    /**
     * Exception factory method
     */
    public static ApiException fromStatus(int code, String body) {
        return switch (code) {
            // 4xx Client Errors (mostly non-retryable)
            case 400 -> new BadRequestException(body);
            case 401 -> new UnauthorizedException(body);
            case 403 -> new ForbiddenException(body);
            case 404 -> new NotFoundException(body);
            case 405 -> new MethodNotAllowedException(body);
            case 408 -> new RequestTimeoutException(body); // Retryable!
            case 409 -> new ConflictException(body);
            case 410 -> new GoneException(body);
            case 422 -> new UnprocessableEntityException(body);
            case 425 -> new TooEarlyException(body); // Retryable!
            case 429 -> new RateLimitException(body); // Retryable!

            // 5xx Server Errors (all retryable)
            case 500 -> new InternalServerErrorException(body);
            case 501 -> new NotImplementedException(body);
            case 502 -> new BadGatewayException(body);
            case 503 -> new ServiceUnavailableException(body);
            case 504 -> new GatewayTimeoutException(body);

            default -> new ApiException(code, "Unexpected HTTP Error", body, code >= 500);
        };
    }

    // ------------------- 4xx Client Errors (Non-Retryable) -------------------

    public static class BadRequestException extends ClientErrorException {
        public BadRequestException(String details) {
            super(400, "Bad Request", details);
        }
    }

    public static class UnauthorizedException extends ClientErrorException {
        public UnauthorizedException(String details) {
            super(401, "Unauthorized", details);
        }
    }

    public static class ForbiddenException extends ClientErrorException {
        public ForbiddenException(String details) {
            super(403, "Forbidden", details);
        }
    }

    public static class NotFoundException extends ClientErrorException {
        public NotFoundException(String details) {
            super(404, "Not Found", details);
        }
    }

    public static class MethodNotAllowedException extends ClientErrorException {
        public MethodNotAllowedException(String details) {
            super(405, "Method Not Allowed", details);
        }
    }

    public static class ConflictException extends ClientErrorException {
        public ConflictException(String details) {
            super(409, "Conflict", details);
        }
    }

    public static class GoneException extends ClientErrorException {
        public GoneException(String details) {
            super(410, "Gone", details);
        }
    }

    public static class UnprocessableEntityException extends ClientErrorException {
        public UnprocessableEntityException(String details) {
            super(422, "Unprocessable Entity", details);
        }
    }

    // ------------------- 4xx Client Errors (Retryable) -------------------

    /**
     * 408 Request Timeout - Server didn't receive complete request in time
     * This is retryable as it's often a transient network issue
     */
    public static class RequestTimeoutException extends ApiException {
        public RequestTimeoutException(String details) {
            super(408, "Request Timeout", details, true);
        }
    }

    /**
     * 425 Too Early - Server unwilling to risk processing a request that might be replayed
     * Retryable after a short delay
     */
    public static class TooEarlyException extends ApiException {
        public TooEarlyException(String details) {
            super(425, "Too Early", details, true);
        }
    }

    /**
     * 429 Too Many Requests - Rate limit exceeded
     * Retryable with exponential backoff
     */
    public static class RateLimitException extends ApiException {
        public RateLimitException(String details) {
            super(429, "Too Many Requests", details, true);
        }
    }

    // ------------------- 5xx Server Errors (All Retryable) -------------------

    public static class InternalServerErrorException extends ServerErrorException {
        public InternalServerErrorException(String details) {
            super(500, "Internal Server Error", details);
        }
    }

    public static class NotImplementedException extends ServerErrorException {
        public NotImplementedException(String details) {
            super(501, "Not Implemented", details);
        }
    }

    public static class BadGatewayException extends ServerErrorException {
        public BadGatewayException(String details) {
            super(502, "Bad Gateway", details);
        }
    }

    public static class ServiceUnavailableException extends ServerErrorException {
        public ServiceUnavailableException(String details) {
            super(503, "Service Unavailable", details);
        }
    }

    public static class GatewayTimeoutException extends ServerErrorException {
        public GatewayTimeoutException(String details) {
            super(504, "Gateway Timeout", details);
        }
    }
}