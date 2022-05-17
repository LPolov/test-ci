package polovinko.leontii.caloriescalculator.services.authorization.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import polovinko.leontii.caloriescalculator.dao.UserRepository;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.services.authorization.UserService;

@Service
@AllArgsConstructor
public class DefaultUserService implements UserService {

  private static final String USER_EMAIL_NOT_FOUND_MSG = "User with email '%s' not found";
  private static final String ACTIVE_USER_NOT_FOUND_MSG = "Active user with email '%s' not found. " +
      "Either user does not exist, or user is not active.";

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) {
    return userRepository.findUserByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_EMAIL_NOT_FOUND_MSG, email)));
  }

  @Override
  public User loadActiveUserByEmail(String email) {
    return userRepository.findActiveUserByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(String.format(ACTIVE_USER_NOT_FOUND_MSG, email)));
  }
}
