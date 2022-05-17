package polovinko.leontii.caloriescalculator.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeController {

  @GetMapping("/user")
  public String home() {
    return "user";
  }

  @GetMapping("/admin")
  public String admin() {
    return "admin";
  }
}
