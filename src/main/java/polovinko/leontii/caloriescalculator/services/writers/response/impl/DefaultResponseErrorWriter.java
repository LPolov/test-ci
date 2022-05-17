package polovinko.leontii.caloriescalculator.services.writers.response.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import polovinko.leontii.caloriescalculator.dto.ErrorMessage;
import polovinko.leontii.caloriescalculator.exception.CaloriesCalculatorServerException;
import polovinko.leontii.caloriescalculator.services.writers.response.ResponseErrorWriter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
@AllArgsConstructor
@Slf4j
public class DefaultResponseErrorWriter implements ResponseErrorWriter {

  private static final String SERIALIZING_ERROR_MSG = "Error showed up while writing data into response";

  private final ObjectMapper objectMapper;

  @Override
  public void writeErrorMessage(HttpServletResponse response, ErrorMessage errorMessage) {
    response.setContentType(MediaType.APPLICATION_JSON.toString());
    response.setStatus(errorMessage.getHttpStatus().value());
    try (PrintWriter writer = response.getWriter()) {
      String errorMessageJson = objectMapper.writeValueAsString(errorMessage);
      writer.write(errorMessageJson);
      writer.flush();
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new CaloriesCalculatorServerException(SERIALIZING_ERROR_MSG, e);
    }
  }
}
