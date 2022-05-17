package polovinko.leontii.caloriescalculator.services.jwt.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static polovinko.leontii.caloriescalculator.helpers.TestConstants.*;
import static polovinko.leontii.caloriescalculator.utils.JwtUtils.BEARER_PREFIX;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import polovinko.leontii.caloriescalculator.helpers.UserGenerator;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.models.UserRole;
import polovinko.leontii.caloriescalculator.services.authorization.UserService;
import polovinko.leontii.caloriescalculator.services.authorization.impl.DefaultUserService;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class DefaultJwtServiceTest {

  @Mock
  private HttpServletRequest request;
  private DefaultJwtService jwtService;

  @BeforeEach
  void setUp() {
    UserService userService = new DefaultUserService(null);
    jwtService = new DefaultJwtService(SECRET_KEY, TOKEN_VALIDITY, TOKEN_VALIDITY, userService);
  }

  @Test
  void getClaimsFromRequest_whenValidJwtPassed_thenClaimsReturned() {
    User user = UserGenerator.createUser(UserRole.USER, USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, ISSUER, TOKEN_VALIDITY, SECRET_KEY);
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_PREFIX + jwt);

    Claims claims = jwtService.getClaimsFromRequest(request);
    Instant issuedDate = claims.getIssuedAt().toInstant();
    Instant expirationDate = claims.getExpiration().toInstant();

    assertEquals(user.getEmail(), claims.getSubject());
    assertEquals(user.getId().toString(), claims.get(JwtUtils.USER_ID_CLAIM));
    assertEquals(user.getRole().name(), claims.get(JwtUtils.ROLE_CLAIM));
    assertEquals(ISSUER, claims.getIssuer());
    assertNotNull(issuedDate);
    assertNotNull(expirationDate);
    assertTrue(issuedDate.isBefore(expirationDate));
    assertEquals(TOKEN_VALIDITY.getSeconds(), expirationDate.getEpochSecond() - issuedDate.getEpochSecond());
  }

  @Test
  void getClaimsFromRequest_whenInvalidJwtPassed_thenThrowsException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_PREFIX + FAKE_JWT);

    assertThrows(JwtException.class, () -> jwtService.getClaimsFromRequest(request));
  }

  @Test
  void getClaimsFromRequest_whenEmptyJwtPassed_thenThrowsException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_PREFIX);

    assertThrows(JwtException.class, () -> jwtService.getClaimsFromRequest(request));
  }

  @Test
  void getClaimsFromRequest_whenWhiteSpacesInsteadOfJwtPassed_thenThrowsException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_PREFIX + WHITE_SPACES);

    assertThrows(JwtException.class, () -> jwtService.getClaimsFromRequest(request));
  }

  @Test
  void getClaimsFromRequest_whenAuthorizationHeaderIsNotPassed_thenThrowsException() {
    assertThrows(JwtException.class, () -> jwtService.getClaimsFromRequest(request));
  }

  @Test
  void getClaimsFromRequest_whenIncorrectJwtPrefixPassed_thenThrowsException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(INCORRECT_BEARER_PREFIX + FAKE_JWT);

    assertThrows(JwtException.class, () -> jwtService.getClaimsFromRequest(request));
  }

  @Test
  void getClaimsFromRequest_whenJwtPrefixIsNotPassed_thenThrowsException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(FAKE_JWT);

    assertThrows(JwtException.class, () -> jwtService.getClaimsFromRequest(request));
  }
}
