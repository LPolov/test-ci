package polovinko.leontii.caloriescalculator.services.jwt;

import io.jsonwebtoken.Claims;
import polovinko.leontii.caloriescalculator.models.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface JwtService {

  String createAccessToken(User user, String issuer);

  String createRefreshToken(User user, String issuer);

  Claims getClaimsFromRequest(HttpServletRequest request);

  void refreshToken(HttpServletRequest request, HttpServletResponse response);
}
