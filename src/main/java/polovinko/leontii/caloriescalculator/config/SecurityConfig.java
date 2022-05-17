package polovinko.leontii.caloriescalculator.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import polovinko.leontii.caloriescalculator.filters.JwtAuthenticationFilter;
import polovinko.leontii.caloriescalculator.filters.JwtAuthorizationFilter;
import polovinko.leontii.caloriescalculator.handlers.errors.ErrorHandler;
import polovinko.leontii.caloriescalculator.services.authorization.impl.DefaultUserService;

@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final DefaultUserService userService;
  private final JwtAuthorizationFilter jwtAuthorizationFilter;
  private final ErrorHandler errorHandler;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().disable()
        .csrf().disable()
        .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .exceptionHandling()
          .accessDeniedHandler(errorHandler)
          .authenticationEntryPoint(errorHandler).and()
        .authorizeRequests()
          .antMatchers("/api/auth/**").permitAll()
          .antMatchers("/api/user/**").hasAnyAuthority("USER")
          .antMatchers("/api/admin/**").hasAnyAuthority("ADMIN")
          .anyRequest().authenticated().and()
        .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(jwtAuthenticationFilter(), JwtAuthorizationFilter.class);
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter();
    jwtAuthenticationFilter.setFilterProcessesUrl("/api/auth/sign-in");
    jwtAuthenticationFilter.setAuthenticationFailureHandler(errorHandler);
    return jwtAuthenticationFilter;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(daoAuthenticationProvider());
  }

  @Bean
  public AuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    daoAuthenticationProvider.setUserDetailsService(userService);
    daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
    daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
    return daoAuthenticationProvider;
  }

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
