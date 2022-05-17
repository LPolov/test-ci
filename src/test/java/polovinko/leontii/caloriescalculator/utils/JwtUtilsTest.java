package polovinko.leontii.caloriescalculator.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static polovinko.leontii.caloriescalculator.helpers.TestConstants.*;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationServiceException;
import polovinko.leontii.caloriescalculator.helpers.UserGenerator;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.models.UserRole;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

  @Mock
  private HttpServletRequest request;

  @Test
  void isJwtPresent_whenJwtStringContainsBearerPrefix_thenReturnsTrue() {
    assertTrue(JwtUtils.isJwtPresent(JwtUtils.BEARER_PREFIX + FAKE_JWT));
  }

  @Test
  void isJwtPresent_whenJwtStringContainsInvalidPrefix_thenReturnsFalse() {
    assertFalse(JwtUtils.isJwtPresent(INCORRECT_BEARER_PREFIX + FAKE_JWT));
  }

  @Test
  void isJwtPresent_whenJwtStringWithoutBearerPrefix_thenReturnsFalse() {
    assertFalse(JwtUtils.isJwtPresent(FAKE_JWT));
  }

  @Test
  void isJwtPresent_whenEmptyStringPassed_thenReturnsFalse() {
    assertFalse(JwtUtils.isJwtPresent(""));
  }

  @Test
  void isJwtPresent_whenWhitespacesPassed_thenReturnsFalse() {
    assertFalse(JwtUtils.isJwtPresent(WHITE_SPACES));
  }

  @Test
  void isJwtPresent_whenNullPassed_thenReturnsFalse() {
    assertFalse(JwtUtils.isJwtPresent(null));
  }

  @Test
  void getJwtFromRequest_whenValidAuthorizationHeaderPassed_thenJwtReturned() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(JwtUtils.BEARER_PREFIX + FAKE_JWT);

    String jwt = JwtUtils.getJwtFromRequest(request);

    assertTrue(StringUtils.isNotBlank(jwt));
    assertEquals(FAKE_JWT, jwt);
  }

  @Test
  void getJwtFromRequest_whenAuthorizationHeaderWithoutBearerPrefixPassed_thenThrowsException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(FAKE_JWT);

    AuthenticationServiceException exception =
        assertThrows(AuthenticationServiceException.class, () -> JwtUtils.getJwtFromRequest(request));
    assertEquals(JwtUtils.JWT_NOT_FOUND_MSG, exception.getMessage());
  }

  @Test
  void getJwtFromRequest_whenAuthorizationHeaderWithIncorrectPrefixPassed_thenThrowsException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(INCORRECT_BEARER_PREFIX + FAKE_JWT);

    AuthenticationServiceException exception =
        assertThrows(AuthenticationServiceException.class, () -> JwtUtils.getJwtFromRequest(request));
    assertEquals(JwtUtils.JWT_NOT_FOUND_MSG, exception.getMessage());
  }

  @Test
  void getJwtFromRequest_whenAuthorizationHeaderIsNotPassed_thenThrowsException() {
    AuthenticationServiceException exception =
        assertThrows(AuthenticationServiceException.class, () -> JwtUtils.getJwtFromRequest(request));
    assertEquals(JwtUtils.JWT_NOT_FOUND_MSG, exception.getMessage());
  }

  @Test
  void getClaimsFromJwt_whenValidJwtAndSecretKeyPassed_thenClaimsReturned() {
    User user = UserGenerator.createUser(UserRole.USER, USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, ISSUER, TOKEN_VALIDITY, SECRET_KEY);

    Claims claims = JwtUtils.getClaimsFromJwt(jwt, SECRET_KEY);
    Instant issuedDate = claims.getIssuedAt().toInstant();
    Instant expirationDate = claims.getExpiration().toInstant();

    assertEquals(USER_EMAIL, claims.getSubject());
    assertEquals(UserRole.USER.name(), claims.get(JwtUtils.ROLE_CLAIM));
    assertNotNull(claims.get(JwtUtils.USER_ID_CLAIM));
    assertEquals(ISSUER, claims.getIssuer());
    assertNotNull(issuedDate);
    assertNotNull(expirationDate);
    assertTrue(issuedDate.isBefore(expirationDate));
    assertEquals(TOKEN_VALIDITY.getSeconds(), expirationDate.getEpochSecond() - issuedDate.getEpochSecond());
  }

  @Test
  void getClaimsFromJwt_whenSecretKeyIsNotPassed_thenThrowsException() {
    User user = UserGenerator.createUser(UserRole.USER, USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, ISSUER, TOKEN_VALIDITY, SECRET_KEY);

    assertThrows(IllegalArgumentException.class, () -> JwtUtils.getClaimsFromJwt(jwt, ""));
    assertThrows(SignatureException.class, () -> JwtUtils.getClaimsFromJwt(jwt, WHITE_SPACES));
  }

  @Test
  void getClaimsFromJwt_whenSecretKeyIsCorrectButJwtIsIncorrect_thenThrowsException() {
    User user = UserGenerator.createUser(UserRole.USER, USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, ISSUER, TOKEN_VALIDITY, SECRET_KEY);

    assertThrows(SignatureException.class, () -> JwtUtils.getClaimsFromJwt(jwt + "!", SECRET_KEY));
    assertThrows(MalformedJwtException.class, () -> JwtUtils.getClaimsFromJwt(FAKE_JWT, SECRET_KEY));
  }

  @Test
  void getClaimsFromJwt_whenDifferentSecretKeyIsPassed_thenThrowsException() {
    User user = UserGenerator.createUser(UserRole.USER, USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, ISSUER, TOKEN_VALIDITY, SECRET_KEY);
    String anotherSecretKey = "AnotherTestSecretKey";

    assertThrows(SignatureException.class, () -> JwtUtils.getClaimsFromJwt(jwt, anotherSecretKey));
  }

  @Test
  void getClaimsFromJwt_whenJwtIsNotPassed_thenThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> JwtUtils.getClaimsFromJwt("", SECRET_KEY));
    assertThrows(IllegalArgumentException.class, () -> JwtUtils.getClaimsFromJwt(WHITE_SPACES, SECRET_KEY));
  }

  @Test
  void buildJwtToken_whenValidDataIsPassed_thenValidJwtIsReturned() {
    User user = UserGenerator.createUser(UserRole.USER, USER_EMAIL);
    UUID userId = user.getId();

    String jwt = JwtUtils.buildJwtToken(user, ISSUER, TOKEN_VALIDITY, SECRET_KEY);
    Claims claims = JwtUtils.getClaimsFromJwt(jwt, SECRET_KEY);
    Instant issuedDate = claims.getIssuedAt().toInstant();
    Instant expirationDate = claims.getExpiration().toInstant();

    assertEquals(USER_EMAIL, claims.getSubject());
    assertEquals(userId.toString(), claims.get(JwtUtils.USER_ID_CLAIM));
    assertEquals(UserRole.USER.name(), claims.get(JwtUtils.ROLE_CLAIM));
    assertEquals(ISSUER, claims.getIssuer());
    assertNotNull(issuedDate);
    assertNotNull(expirationDate);
    assertTrue(issuedDate.isBefore(expirationDate));
    assertEquals(TOKEN_VALIDITY.getSeconds(), expirationDate.getEpochSecond() - issuedDate.getEpochSecond());
  }
}
