package polovinko.leontii.caloriescalculator.filters;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = "classpath:scripts/default_users_creation.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    scripts = "classpath:scripts/truncate_tables.sql")
class JwtAuthenticationFilterIntegrationTest {

  private static final String REQUEST_BODY = "{\"email\":\"%s\", \"password\":\"%s\"}";

  @Autowired
  private MockMvc mockMvc;
  private MockHttpServletRequestBuilder request;

  @BeforeEach
  void setUp() {
    request = MockMvcRequestBuilders.post("/api/auth/sign-in");
    request.contentType(MediaType.APPLICATION_JSON);
  }

  @Test
  void authenticationTest_whenValidCredentialsPassed_thenAuthenticationSucceeds() throws Exception {
    request.content(String.format(REQUEST_BODY, "validUser", "user"));

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertNotNull(response.getHeader(JwtUtils.ACCESS_TOKEN_RESPONSE_HEADER));
    assertNotNull(response.getHeader(JwtUtils.REFRESH_TOKEN_RESPONSE_HEADER));
  }

  @Test
  void authenticationTest_whenNonRegisteredEmailPassed_thenErrorMessageIsReturned() throws Exception {
    request.content(String.format(REQUEST_BODY, "user", "user"));

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("User with email 'user' not found"));
  }

  @Test
  void authenticationTest_whenEmailIsNotPassed_thenErrorMessageIsReturned() throws Exception {
    request.content("{\"password\":\"user\"}");

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("User with email 'NONE_PROVIDED' not found"));
  }

  @Test
  void authenticationTest_whenInvalidPasswordPassed_thenErrorMessageIsReturned() throws Exception {
    request.content(String.format(REQUEST_BODY, "validUser", "userr"));

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("Bad credentials"));
  }

  @Test
  void authenticationTest_whenPasswordIsNotPassed_thenErrorMessageIsReturned() throws Exception {
    request.content("{\"email\":\"validUser\"}");

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("Bad credentials"));
  }

  @Test
  void authenticationTest_whenJsonFormatIsIncorrect_thenErrorMessageIsReturned() throws Exception {
    request.content("{email:validUser}");

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("Login request was not deserialized from json"));
  }

  @Test
  void authenticationTest_whenPayloadIsEmpty_thenErrorMessageIsReturned() throws Exception {
    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("Login request was not deserialized from json"));
  }

  @Test
  void authenticationTest_whenUserIsLocked_thenErrorMessageIsReturned() throws Exception {
    request.content(String.format(REQUEST_BODY, "lockedUser", "user"));

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("User account is locked"));
  }

  @Test
  void authenticationTest_whenUserIsDisabled_thenErrorMessageIsReturned() throws Exception {
    request.content(String.format(REQUEST_BODY, "disabledUser", "user"));

    MockHttpServletResponse response = mockMvc.perform(request)
        .andReturn()
        .getResponse();

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
    assertTrue(response.getContentAsString().contains("User is disabled"));
  }
}
