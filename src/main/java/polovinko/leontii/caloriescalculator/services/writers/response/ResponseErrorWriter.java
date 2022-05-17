package polovinko.leontii.caloriescalculator.services.writers.response;

import polovinko.leontii.caloriescalculator.dto.ErrorMessage;
import javax.servlet.http.HttpServletResponse;

public interface ResponseErrorWriter {

  void writeErrorMessage(HttpServletResponse response, ErrorMessage errorMessage);
}
