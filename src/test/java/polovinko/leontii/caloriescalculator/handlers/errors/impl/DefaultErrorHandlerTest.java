package polovinko.leontii.caloriescalculator.handlers.errors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import polovinko.leontii.caloriescalculator.dto.ErrorMessage;
import polovinko.leontii.caloriescalculator.exception.CaloriesCalculatorServerException;
import polovinko.leontii.caloriescalculator.services.writers.response.ResponseErrorWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class DefaultErrorHandlerTest {

  private static final String ERROR_MESSAGE = "Error message";
  private static final String JWT_PARSING_ERROR_MESSAGE = "Server error message";
  private static final String JWT_ERROR_ATTRIBUTE = "jwt_parsing_error";

  @Captor
  private ArgumentCaptor<ErrorMessage> errorMessageCaptor;
  @Mock
  private ResponseErrorWriter errorWriter;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @InjectMocks
  private DefaultErrorHandler defaultErrorHandler;


  @Test
  void handleError_whenExceptionIsPassed_thenResponseIsReturned() {
    RuntimeException exception = new UsernameNotFoundException(ERROR_MESSAGE);

    defaultErrorHandler.handleError(request, response, exception, HttpStatus.UNAUTHORIZED);

    verify(errorWriter).writeErrorMessage(same(response), errorMessageCaptor.capture());
    ErrorMessage errorMessage = errorMessageCaptor.getValue();
    assertEquals(ERROR_MESSAGE, errorMessage.getMessage());
    assertEquals(HttpStatus.UNAUTHORIZED, errorMessage.getHttpStatus());
    assertNotNull(errorMessage.getHappenedAt());
  }

  @Test
  void handleError_whenJwtErrorAttributeIsPassed_thenResponseIsReturned() {
    RuntimeException exception = new UsernameNotFoundException(ERROR_MESSAGE);
    when(request.getAttribute(JWT_ERROR_ATTRIBUTE)).thenReturn(JWT_PARSING_ERROR_MESSAGE);

    defaultErrorHandler.handleError(request, response, exception, HttpStatus.UNAUTHORIZED);

    verify(errorWriter).writeErrorMessage(same(response), errorMessageCaptor.capture());
    ErrorMessage errorMessage = errorMessageCaptor.getValue();
    assertEquals(JWT_PARSING_ERROR_MESSAGE, errorMessage.getMessage());
    assertEquals(HttpStatus.UNAUTHORIZED, errorMessage.getHttpStatus());
    assertNotNull(errorMessage.getHappenedAt());
  }

  @Test
  void handleJwtExceptions_whenExceptionIsPassed_thenResponseIsReturned() {
    JwtException exception = new JwtException(ERROR_MESSAGE);

    defaultErrorHandler.handleJwtExceptions(request, response, exception);

    verify(errorWriter).writeErrorMessage(same(response), errorMessageCaptor.capture());
    ErrorMessage errorMessage = errorMessageCaptor.getValue();
    assertEquals(ERROR_MESSAGE, errorMessage.getMessage());
    assertEquals(HttpStatus.UNAUTHORIZED, errorMessage.getHttpStatus());
    assertNotNull(errorMessage.getHappenedAt());
  }

  @Test
  void handleServerExceptions_whenServerExceptionIsPassed_thenResponseIsReturned() {
    CaloriesCalculatorServerException exception = new CaloriesCalculatorServerException(ERROR_MESSAGE);

    defaultErrorHandler.handleServerExceptions(request, response, exception);

    verify(errorWriter).writeErrorMessage(same(response), errorMessageCaptor.capture());
    ErrorMessage errorMessage = errorMessageCaptor.getValue();
    assertEquals(ERROR_MESSAGE, errorMessage.getMessage());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage.getHttpStatus());
    assertNotNull(errorMessage.getHappenedAt());
  }

  @Test
  void handle_whenAccessDeniedExceptionIsPassed_thenErrorMessageIsPassedToResponseWriter() {
    AccessDeniedException exception = new AccessDeniedException(ERROR_MESSAGE);

    defaultErrorHandler.handle(request, response, exception);

    verify(errorWriter).writeErrorMessage(same(response), errorMessageCaptor.capture());
    ErrorMessage errorMessage = errorMessageCaptor.getValue();
    assertEquals(ERROR_MESSAGE, errorMessage.getMessage());
    assertEquals(HttpStatus.FORBIDDEN, errorMessage.getHttpStatus());
    assertNotNull(errorMessage.getHappenedAt());
  }

  @Test
  void commence_whenAuthenticationExceptionIsPassed_thenResponseIsReturned() {
    AuthenticationException exception = new AuthenticationServiceException(ERROR_MESSAGE);

    defaultErrorHandler.commence(request, response, exception);

    verify(errorWriter).writeErrorMessage(same(response), errorMessageCaptor.capture());
    ErrorMessage errorMessage = errorMessageCaptor.getValue();
    assertEquals(ERROR_MESSAGE, errorMessage.getMessage());
    assertEquals(HttpStatus.UNAUTHORIZED, errorMessage.getHttpStatus());
    assertNotNull(errorMessage.getHappenedAt());
  }

  @Test
  void onAuthenticationFailure_whenAuthenticationExceptionIsPassed_thenResponseIsReturned() {
    AuthenticationException exception = new AuthenticationServiceException(ERROR_MESSAGE);

    defaultErrorHandler.onAuthenticationFailure(request, response, exception);

    verify(errorWriter).writeErrorMessage(same(response), errorMessageCaptor.capture());
    ErrorMessage errorMessage = errorMessageCaptor.getValue();
    assertEquals(ERROR_MESSAGE, errorMessage.getMessage());
    assertEquals(HttpStatus.UNAUTHORIZED, errorMessage.getHttpStatus());
    assertNotNull(errorMessage.getHappenedAt());
  }
}
