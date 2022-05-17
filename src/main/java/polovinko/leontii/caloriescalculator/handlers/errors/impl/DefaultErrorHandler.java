package polovinko.leontii.caloriescalculator.handlers.errors.impl;

import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import polovinko.leontii.caloriescalculator.dto.ErrorMessage;
import polovinko.leontii.caloriescalculator.exception.CaloriesCalculatorServerException;
import polovinko.leontii.caloriescalculator.handlers.errors.ErrorHandler;
import polovinko.leontii.caloriescalculator.services.writers.response.ResponseErrorWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;

@ControllerAdvice
@AllArgsConstructor
public class DefaultErrorHandler implements ErrorHandler {

  private static final String JWT_ERROR_ATTRIBUTE = "jwt_parsing_error";

  private final ResponseErrorWriter errorWriter;

  @Override
  public void handleError(HttpServletRequest request, HttpServletResponse response,
                          Exception exception, HttpStatus status) {

    ErrorMessage errorMessage = new ErrorMessage(
        getMessage(request, exception),
        LocalDateTime.now(),
        status
    );
    errorWriter.writeErrorMessage(response, errorMessage);
  }

  private String getMessage(HttpServletRequest request, Exception exception) {
    return Optional.ofNullable((String) request.getAttribute(JWT_ERROR_ATTRIBUTE))
        .orElse(exception.getMessage());
  }

  @ExceptionHandler(JwtException.class)
  public void handleJwtExceptions(HttpServletRequest request,
                                  HttpServletResponse response,
                                  JwtException exception) {
    handleError(request, response, exception, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(CaloriesCalculatorServerException.class)
  public void handleServerExceptions(HttpServletRequest request,
                                     HttpServletResponse response,
                                     CaloriesCalculatorServerException exception) {
    handleError(request, response, exception, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException exception) {
    handleError(request, response, exception, HttpStatus.UNAUTHORIZED);
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     AccessDeniedException exception) {
    handleError(request, response, exception, HttpStatus.FORBIDDEN);
  }

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) {
    handleError(request, response, exception, HttpStatus.UNAUTHORIZED);
  }
}
