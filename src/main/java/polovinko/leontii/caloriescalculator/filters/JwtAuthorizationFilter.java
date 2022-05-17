package polovinko.leontii.caloriescalculator.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import polovinko.leontii.caloriescalculator.services.jwt.JwtService;
import polovinko.leontii.caloriescalculator.utils.JwtUtils;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  public static final String LOGIN_URL = "/api/auth/sign-in";
  public static final String REFRESH_TOKEN_URL = "/api/auth/refresh-token";
  private static final String JWT_ERROR_ATTRIBUTE = "jwt_parsing_error";

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException{
    if (isRequestToAuthorize(request)) {
      Optional<Claims> claims = getClaimsFromRequest(request);
      claims.ifPresent(this::setAuthentication);
    }
    filterChain.doFilter(request, response);
  }

  private boolean isRequestToAuthorize(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    return !requestURI.equals(LOGIN_URL) && !requestURI.equals(REFRESH_TOKEN_URL);
  }

  private Optional<Claims> getClaimsFromRequest(HttpServletRequest request){
    Claims claims = null;
    try {
      claims = jwtService.getClaimsFromRequest(request);
    } catch (JwtException e) {
      request.setAttribute(JWT_ERROR_ATTRIBUTE, e.getMessage());
    }
    return Optional.ofNullable(claims);
  }

  private void setAuthentication(Claims claims) {
    UsernamePasswordAuthenticationToken authentication = createAuthenticationToken(claims);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
  
  private UsernamePasswordAuthenticationToken createAuthenticationToken(Claims claims) {
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(claims.get(JwtUtils.ROLE_CLAIM).toString());
    return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of(authority));
  }
}
