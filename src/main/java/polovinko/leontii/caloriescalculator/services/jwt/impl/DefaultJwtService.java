package polovinko.leontii.caloriescalculator.services.jwt.impl;

import static polovinko.leontii.caloriescalculator.utils.JwtUtils.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.services.authorization.UserService;
import polovinko.leontii.caloriescalculator.services.jwt.JwtService;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
public class DefaultJwtService implements JwtService {

  private final UserService userService;
  private final String secretKey;
  private final Duration accessTokenValidity;
  private final Duration refreshTokenValidity;

  public DefaultJwtService(@Value("${jwt.secretKey}") String secretKey,
                           @Value("${jwt.validity.accessToken}") Duration accessTokenValidity,
                           @Value("${jwt.validity.refreshToken}") Duration refreshTokenValidity,
                           UserService userService) {
    this.secretKey = secretKey;
    this.accessTokenValidity = accessTokenValidity;
    this.refreshTokenValidity = refreshTokenValidity;
    this.userService = userService;
  }

  public String createAccessToken(User user, String issuer) {
    return JwtUtils.buildJwtToken(user, issuer,  accessTokenValidity, secretKey);
  }

  public String createRefreshToken(User user, String issuer) {
    return JwtUtils.buildJwtToken(user, issuer, refreshTokenValidity, secretKey);
  }

  public Claims getClaimsFromRequest(HttpServletRequest request) {
    Claims claims;
    try {
      String jwt = JwtUtils.getJwtFromRequest(request);
      claims = JwtUtils.getClaimsFromJwt(jwt, secretKey);
    } catch (RuntimeException e) {
      throw new JwtException(e.getMessage(), e.getCause());
    }
    return claims;
  }

  @Override
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
    Claims claims = getClaimsFromRequest(request);
    User user = userService.loadActiveUserByEmail(claims.getSubject());
    matchClaimsAndUserData(claims, user);
    String refreshToken = createRefreshToken(user, request.getRequestURI());
    String accessToken = createAccessToken(user, request.getRequestURI());
    response.setHeader(ACCESS_TOKEN_RESPONSE_HEADER, accessToken);
    response.setHeader(REFRESH_TOKEN_RESPONSE_HEADER, refreshToken);
  }

  private void matchClaimsAndUserData(Claims claims, User user) {
    if (!isClaimsMatchingUserData(claims, user)) {
      throw new AuthenticationServiceException(INVALID_JWT_ERROR_MESSAGE);
    }
  }

  public boolean isClaimsMatchingUserData(Claims claims, User user) {
    return claims.get(ROLE_CLAIM).equals(user.getRole().name()) &&
        claims.get(USER_ID_CLAIM).equals(user.getId().toString());
  }
}
