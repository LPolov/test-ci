package polovinko.leontii.caloriescalculator.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import polovinko.leontii.caloriescalculator.dto.UserLoginRequest;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.services.jwt.JwtService;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;
import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private static final String DESERIALIZATION_ERROR_MSG = "Login request was not deserialized from json";

  private ObjectMapper objectMapper;
  private JwtService jwtService;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
                                              HttpServletResponse response) throws AuthenticationException {
    Authentication authenticationToken = getAuthenticationToken(request);
    return super.getAuthenticationManager().authenticate(authenticationToken);
  }

  private Authentication getAuthenticationToken(HttpServletRequest request) {
    UserLoginRequest userLoginRequest;
    try (ServletInputStream requestInputStream = request.getInputStream()) {
       userLoginRequest = objectMapper.readValue(requestInputStream, UserLoginRequest.class);
    } catch (Exception e) {
      throw new AuthenticationServiceException(DESERIALIZATION_ERROR_MSG, e);
    }
    return new UsernamePasswordAuthenticationToken(userLoginRequest.getEmail(), userLoginRequest.getPassword());
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain chain,
                                          Authentication authResult) {
    User user = (User) authResult.getPrincipal();
    String issuer = request.getRequestURL().toString();
    response.setHeader(JwtUtils.ACCESS_TOKEN_RESPONSE_HEADER, jwtService.createAccessToken(user, issuer));
    response.setHeader(JwtUtils.REFRESH_TOKEN_RESPONSE_HEADER, jwtService.createRefreshToken(user, issuer));
  }

  @Autowired
  public void setJwtUtils(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  @Autowired
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    super.setAuthenticationManager(authenticationManager);
  }
}
