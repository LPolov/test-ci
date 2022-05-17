package polovinko.leontii.caloriescalculator.filters;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import polovinko.leontii.caloriescalculator.helpers.TestConstants;
import polovinko.leontii.caloriescalculator.helpers.UserGenerator;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.models.UserRole;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;
import java.time.Duration;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class JwtAuthorizationFilterIntegrationTest {

  private static final String USER_API = "/api/user";
  private static final String ADMIN_API = "/api/admin";

  @Autowired
  private MockMvc mockMvc;
  private MockHttpServletRequestBuilder request;
  @Value("${jwt.secretKey}")
  private String secretKey;
  @Value("${jwt.validity.accessToken}")
  private Duration tokenValidity;

  @Test
  void doFilterInternal_whenUserTriesToGetUsersContent_thenUsersContentIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.USER, TestConstants.USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, TestConstants.ISSUER, tokenValidity, secretKey);
    setUpRequest(USER_API, jwt);

    mockMvc.perform(request)
        .andExpect(status().isOk())
        .andExpect(content().string("user"));
  }

  @Test
  void doFilterInternal_whenAdminTriesToGetUsersContent_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.ADMIN, TestConstants.USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, TestConstants.ISSUER, tokenValidity, secretKey);
    setUpRequest(USER_API, jwt);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains("Access is denied"));
  }

  @Test
  void doFilterInternal_whenAdminTriesToGetAdminsContent_thenAdminsContentIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.ADMIN, TestConstants.USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, TestConstants.ISSUER, tokenValidity, secretKey);
    setUpRequest(ADMIN_API, jwt);

    mockMvc.perform(request)
        .andExpect(status().isOk())
        .andExpect(content().string("admin"));
  }

  @Test
  void doFilterInternal_whenUserTriesToGetAdminsContent_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.USER, TestConstants.USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, TestConstants.ISSUER, tokenValidity, secretKey);
    setUpRequest(ADMIN_API, jwt);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains("Access is denied"));
  }

  @Test
  void doFilterInternal_whenJwtIsExpired_thenErrorMessageIsReturned() throws Exception {
    User user = UserGenerator.createUser(UserRole.ADMIN, TestConstants.USER_EMAIL);
    String jwt = JwtUtils.buildJwtToken(user, TestConstants.ISSUER, Duration.ofMillis(1), secretKey);
    setUpRequest(USER_API, jwt);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains("JWT expired at"));
  }

  @Test
  void doFilterInternal_whenAuthorizationHeaderIsAbsent_thenErrorMessageIsReturned() throws Exception {
    request = MockMvcRequestBuilders.get(USER_API);
    request.contentType(MediaType.APPLICATION_JSON);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains(JwtUtils.JWT_NOT_FOUND_MSG));
  }

  @Test
  void doFilterInternal_whenJwtIsInvalid_thenErrorMessageIsReturned() throws Exception {
    setUpRequest(USER_API, TestConstants.FAKE_JWT);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains("JWT strings must contain exactly 2 period characters."));
  }

  @Test
  void doFilterInternal_whenJwtIsWhiteSpaces_thenErrorMessageIsReturned() throws Exception {
    setUpRequest(USER_API, TestConstants.WHITE_SPACES);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains(JwtUtils.JWT_NOT_FOUND_MSG));
  }

  @Test
  void doFilterInternal_whenJwtPrefixIsIncorrect_thenErrorMessageIsReturned() throws Exception {
    request = MockMvcRequestBuilders.get("/api/user");
    request.contentType(MediaType.APPLICATION_JSON);
    request.header(HttpHeaders.AUTHORIZATION, TestConstants.INCORRECT_BEARER_PREFIX + TestConstants.FAKE_JWT);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains(JwtUtils.JWT_NOT_FOUND_MSG));
  }

  @Test
  void doFilterInternal_whenJwtDoesNotHavePrefix_thenErrorMessageIsReturned() throws Exception {
    request = MockMvcRequestBuilders.get("/api/user");
    request.contentType(MediaType.APPLICATION_JSON);
    request.header(HttpHeaders.AUTHORIZATION, TestConstants.FAKE_JWT);

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertTrue(response.getContentAsString().contains(JwtUtils.JWT_NOT_FOUND_MSG));
  }

  private void setUpRequest(String endPoint, String jwt) {
    request = MockMvcRequestBuilders.get(endPoint);
    request.contentType(MediaType.APPLICATION_JSON);
    request.header(HttpHeaders.AUTHORIZATION, JwtUtils.BEARER_PREFIX + jwt);
  }
}
