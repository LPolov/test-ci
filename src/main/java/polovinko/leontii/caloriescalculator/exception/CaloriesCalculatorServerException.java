package polovinko.leontii.caloriescalculator.exception;

public class CaloriesCalculatorServerException extends RuntimeException{

  public CaloriesCalculatorServerException(String message) {
    super(message);
  }

  public CaloriesCalculatorServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
