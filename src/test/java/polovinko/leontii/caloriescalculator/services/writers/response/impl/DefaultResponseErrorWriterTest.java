package polovinko.leontii.caloriescalculator.services.writers.response.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import polovinko.leontii.caloriescalculator.dto.ErrorMessage;
import polovinko.leontii.caloriescalculator.exception.CaloriesCalculatorServerException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class DefaultResponseErrorWriterTest {

  @Mock
  private HttpServletResponse response;
  private DefaultResponseErrorWriter defaultResponseErrorWriter;
  private ObjectMapper objectMapper;
  private ErrorMessage errorMessage;


  @BeforeEach
  void setUp() {
    errorMessage = new ErrorMessage("ErrorMessage", LocalDateTime.now(), HttpStatus.FORBIDDEN);
    objectMapper = new ObjectMapper();
    defaultResponseErrorWriter = new DefaultResponseErrorWriter(objectMapper);
  }

  @Test
  void writeErrorMessage_whenErrorMessageIsPassed_thenErrorMessageIsWrittenIntoResponse() throws IOException {
    objectMapper.findAndRegisterModules();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);


    defaultResponseErrorWriter.writeErrorMessage(response, errorMessage);
    ErrorMessage result = objectMapper.readValue(stringWriter.toString(), ErrorMessage.class);

    verify(response).setContentType(MediaType.APPLICATION_JSON.toString());
    verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    assertEquals(errorMessage.getMessage(), result.getMessage());
    assertEquals(errorMessage.getHappenedAt().truncatedTo(ChronoUnit.SECONDS), result.getHappenedAt());
  }

  @Test
  void writeErrorMessage_whenObjectMapperIsNotConfigured_thenThrowsException() {
    CaloriesCalculatorServerException exception = assertThrows(
        CaloriesCalculatorServerException.class,
        () -> defaultResponseErrorWriter.writeErrorMessage(response, errorMessage)
    );
    assertEquals("Error showed up while writing data into response", exception.getMessage());
  }
}
