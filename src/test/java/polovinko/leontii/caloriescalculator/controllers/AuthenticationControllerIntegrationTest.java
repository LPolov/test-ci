package polovinko.leontii.caloriescalculator.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import polovinko.leontii.caloriescalculator.helpers.TestConstants;
import polovinko.leontii.caloriescalculator.helpers.UserGenerator;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.models.UserRole;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;
import java.time.Duration;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = "classpath:scripts/default_users_creation.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    scripts = "classpath:scripts/truncate_tables.sql")
class AuthenticationControllerIntegrationTest {

  private static final UUID VALID_USER_ID = UUID.fromString("46a4f382-fafb-494c-a5ce-b14acbc307c4");
  private static final UUID LOCKED_USER_ID = UUID.fromString("46a4f382-fafb-494c-a5ce-b14acbc307c6");
  private static final UUID DISABLED_USER_ID = UUID.fromString("46a4f382-fafb-494c-a5ce-b14acbc307c5");

  @Autowired
  private MockMvc mockMvc;
  @Value("${jwt.validity.refreshToken}")
  private Duration refreshTokenValidity;
  @Value("${jwt.secretKey}")
  private String secretKey;
  private MockHttpServletRequestBuilder request;

  @BeforeEach
  void setUp() {
    request = MockMvcRequestBuilders.get("/api/auth/refresh-token");
  }

  @Test
  void refreshToken_whenJwtContainsValidUserClaims_thenRefreshAndAccessTokensReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.USER, "validUser");
    user.setId(VALID_USER_ID);
    setUpRefreshToken(user);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertNotNull(response.getHeader(JwtUtils.ACCESS_TOKEN_RESPONSE_HEADER));
    assertNotNull(response.getHeader(JwtUtils.REFRESH_TOKEN_RESPONSE_HEADER));
  }

  @Test
  void refreshToken_whenRefreshTokenHeaderIsAbsent_thenErrorMessageIsReturned() throws Exception {
    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains(JwtUtils.JWT_NOT_FOUND_MSG));
  }

  @Test
  void refreshToken_whenUserIsDisabled_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.USER, "disabledUser");
    user.setId(DISABLED_USER_ID);
    setUpRefreshToken(user);
    String expectedErrorMessage = "Active user with email 'disabledUser' not found. " +
        "Either user does not exist, or user is not active.";

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains(expectedErrorMessage));
  }

  @Test
  void refreshToken_whenUserIsLocked_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.USER, "lockedUser");
    user.setId(LOCKED_USER_ID);
    setUpRefreshToken(user);
    String expectedErrorMessage = "Active user with email 'lockedUser' not found. " +
        "Either user does not exist, or user is not active.";

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains(expectedErrorMessage));
  }

  @Test
  void refreshToken_whenRefreshTokenIsMalformed_thenErrorMessageIsReturned() throws Exception {
    request.header(HttpHeaders.AUTHORIZATION, JwtUtils.BEARER_PREFIX + TestConstants.FAKE_JWT);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("JWT strings must contain exactly 2 period characters."));
  }

  @Test
  void refreshToken_whenRefreshTokenContainsIncorrectUserId_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.USER, "validUser");
    user.setId(DISABLED_USER_ID);
    setUpRefreshToken(user);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("JWT is invalid"));
  }

  @Test
  void refreshToken_whenRefreshTokenContainsIncorrectUserRole_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.ADMIN, "validUser");
    user.setId(VALID_USER_ID);
    setUpRefreshToken(user);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("JWT is invalid"));
  }

  @Test
  void refreshToken_whenRefreshTokenIsExpired_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.USER, "validUser");
    user.setId(VALID_USER_ID);
    String refreshToken = JwtUtils.buildJwtToken(user, TestConstants.ISSUER, Duration.ofMillis(1), secretKey);
    request.header(HttpHeaders.AUTHORIZATION, JwtUtils.BEARER_PREFIX + refreshToken);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("JWT expired at"));
  }

  @Test
  void refreshToken_whenJwtPrefixIsAbsent_thenErrorMessageIsReturned() throws Exception {
    request.header(HttpHeaders.AUTHORIZATION, TestConstants.FAKE_JWT);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains(JwtUtils.JWT_NOT_FOUND_MSG));
  }

  @Test
  void refreshToken_whenJwtPrefixIsIncorrect_thenErrorMessageIsReturned() throws Exception {
    request.header(HttpHeaders.AUTHORIZATION, TestConstants.INCORRECT_BEARER_PREFIX + TestConstants.FAKE_JWT);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains(JwtUtils.JWT_NOT_FOUND_MSG));
  }

  private void setUpRefreshToken(User user) {
    String refreshToken = JwtUtils.buildJwtToken(user, TestConstants.ISSUER, refreshTokenValidity, secretKey);
    request.header(HttpHeaders.AUTHORIZATION, JwtUtils.BEARER_PREFIX + refreshToken);
  }
}

