package polovinko.leontii.caloriescalculator.services.authorization.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import polovinko.leontii.caloriescalculator.dao.UserRepository;
import polovinko.leontii.caloriescalculator.models.User;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DefaultUserServiceTest {

  @Mock
  private UserRepository userRepository;
  @InjectMocks
  private DefaultUserService defaultUserService;

  @Test
  void loadUserByUsername_whenDbContainsUserWithPassedEmail_thenUserIsReturned() {
    User user = new User();
    when(userRepository.findUserByEmail("email")).thenReturn(Optional.of(user));

    User userDetails = (User) defaultUserService.loadUserByUsername("email");

    assertSame(user, userDetails);
  }

  @Test
  void loadUserByUsername_whenDbDoesNotContainUserWithPassedEmail_thenExceptionIsThrown() {
    UsernameNotFoundException exception =
        assertThrows(UsernameNotFoundException.class, () -> defaultUserService.loadUserByUsername("email"));
    assertEquals("User with email 'email' not found", exception.getMessage());
  }

  @Test
  void loadActiveUserByEmail_whenDbContainsActiveUserWithPassedEmail_thenUserIsReturned() {
    User user = new User();
    when(userRepository.findActiveUserByEmail("email")).thenReturn(Optional.of(user));

    User userDetails = defaultUserService.loadActiveUserByEmail("email");

    assertSame(user, userDetails);
  }

  @Test
  void loadUserByUsername_whenDbDoesNotContainActiveUserWithPassedEmail_thenExceptionIsThrown() {
    String expectedErrorMessage = "Active user with email 'email' not found. " +
        "Either user does not exist, or user is not active.";

    UsernameNotFoundException exception =
        assertThrows(UsernameNotFoundException.class, () -> defaultUserService.loadActiveUserByEmail("email"));
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
