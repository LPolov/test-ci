package polovinko.leontii.caloriescalculator.handlers.errors;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ErrorHandler extends AccessDeniedHandler, AuthenticationEntryPoint, AuthenticationFailureHandler {

  void handleError(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus status);
}
