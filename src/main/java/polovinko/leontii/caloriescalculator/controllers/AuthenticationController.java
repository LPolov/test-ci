package polovinko.leontii.caloriescalculator.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import polovinko.leontii.caloriescalculator.services.jwt.JwtService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationController {

  private final JwtService jwtService;

  @GetMapping("/refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
    jwtService.refreshToken(request, response);
  }
}
