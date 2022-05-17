package polovinko.leontii.caloriescalculator.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.StringUtils;
import polovinko.leontii.caloriescalculator.models.User;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class JwtUtils {

  public static final String ACCESS_TOKEN_RESPONSE_HEADER = "accessToken";
  public static final String REFRESH_TOKEN_RESPONSE_HEADER = "refreshToken";
  public static final String ROLE_CLAIM = "role";
  public static final String USER_ID_CLAIM = "userId";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String JWT_NOT_FOUND_MSG = "Authorization token not found";
  public static final String INVALID_JWT_ERROR_MESSAGE = "JWT is invalid";

  public static String getJwtFromRequest(HttpServletRequest request) {
    Optional<String> authorizationHeader = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION));
    return authorizationHeader.filter(JwtUtils::isJwtPresent)
        .map(token -> token.substring(BEARER_PREFIX.length()))
        .filter(StringUtils::hasText)
        .orElseThrow(() -> new AuthenticationServiceException(JWT_NOT_FOUND_MSG));
  }

  public static boolean isJwtPresent(String header) {
    return StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX);
  }

  public static Claims getClaimsFromJwt(String jwt, String secretKey) {
    return Jwts.parser()
        .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
        .parseClaimsJws(jwt)
        .getBody();
  }

  public static String buildJwtToken(User user, String issuer, Duration tokenValidity, String secretKey) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim(ROLE_CLAIM, user.getRole().name())
        .claim(USER_ID_CLAIM, user.getId().toString())
        .setIssuer(issuer)
        .setIssuedAt(new Date())
        .setExpiration(getTokenExpirationDate(tokenValidity))
        .signWith(SignatureAlgorithm.HS512, secretKey.getBytes(StandardCharsets.UTF_8))
        .compact();
  }

  private static Date getTokenExpirationDate(Duration validity) {
    return Date.from(Instant.now().plusMillis(validity.toMillis()));
  }
}
