package matgo.global.exception.dto;

import jakarta.servlet.http.HttpServletRequest;

public record ErrorLogRequest(HttpServletRequest request, Exception exception) {

    private static final String ERROR_REPORT_FORMAT = "[%s] %s";


    public String getLogMessage() {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        return String.format(ERROR_REPORT_FORMAT, requestMethod, requestUri);
    }

}
