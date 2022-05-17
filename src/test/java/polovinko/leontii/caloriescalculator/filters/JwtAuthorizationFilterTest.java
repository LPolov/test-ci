package polovinko.leontii.caloriescalculator.filters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import polovinko.leontii.caloriescalculator.services.jwt.JwtService;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class JwtAuthorizationFilterTest {

  @Mock
  private JwtService jwtService;
  @Mock
  private HttpServletRequest request;
  @Mock
  private FilterChain filterChain;
  private JwtAuthorizationFilter jwtAuthorizationFilter;

  @BeforeEach
  void setUp() {
    jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtService);
  }

  @Test
  void doFilterInternal_whenLoginRequestPassed_thenFilterSkipsRequest() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/auth/sign-in");

    jwtAuthorizationFilter.doFilterInternal(request, null, filterChain);

    verifyNoInteractions(jwtService);
  }

  @Test
  void doFilterInternal_whenRefreshTokenRequestPassed_thenFilterSkipsRequest() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/auth/refresh-token");

    jwtAuthorizationFilter.doFilterInternal(request, null, filterChain);

    verifyNoInteractions(jwtService);
  }

  @Test
  void doFilterInternal_whenRequestToAuthorizePassed_thenFilterAuthorizeRequest() throws ServletException, IOException {
    List<SimpleGrantedAuthority> expectedAuthorities = List.of(new SimpleGrantedAuthority(JwtUtils.ROLE_CLAIM));
    DefaultClaims claims = mock(DefaultClaims.class);
    when(claims.get(JwtUtils.ROLE_CLAIM)).thenReturn(JwtUtils.ROLE_CLAIM);
    when(jwtService.getClaimsFromRequest(request)).thenReturn(claims);
    when(request.getRequestURI()).thenReturn("/api/home");

    jwtAuthorizationFilter.doFilterInternal(request, null, filterChain);

    verify(jwtService).getClaimsFromRequest(same(request));
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertEquals(1, authentication.getAuthorities().size());
    assertEquals(expectedAuthorities, authentication.getAuthorities());
  }

  @Test
  void doFilterInternal_whenExceptionThrownWhileGettingClaimsFromRequest_thenFilterSetsErrorRequestAttribute()
      throws ServletException, IOException {

    when(request.getRequestURI()).thenReturn("/api/home");
    when(jwtService.getClaimsFromRequest(request))
        .thenThrow(new JwtException("Server error message"));

    jwtAuthorizationFilter.doFilterInternal(request, null, filterChain);

    verify(request).setAttribute("jwt_parsing_error", "Server error message");
  }

  @Test
  void doFilterInternal_whenClaimsFromRequestAreNull_thenFilterDoesNotAuthorizeRequest()
      throws ServletException, IOException {

    SecurityContextHolder.getContext().setAuthentication(null);
    when(request.getRequestURI()).thenReturn("/api/home");

    jwtAuthorizationFilter.doFilterInternal(request, null, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
