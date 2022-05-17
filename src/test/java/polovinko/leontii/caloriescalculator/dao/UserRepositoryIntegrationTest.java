package polovinko.leontii.caloriescalculator.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.models.UserRole;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = "classpath:scripts/default_users_creation.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    scripts = "classpath:scripts/truncate_tables.sql")
public class UserRepositoryIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  void findUserByEmail_whenExistingInDbUsersEmailIsPassed_thenThisUserIsReturned() {
    Optional<User> result = userRepository.findUserByEmail("disabledUser");

    assertTrue(result.isPresent());
    User user = result.get();
    assertEquals(UUID.fromString("46a4f382-fafb-494c-a5ce-b14acbc307c5"), user.getId());
    assertEquals("disabledUser", user.getEmail());
    assertFalse(user.isEnabled());
    assertFalse(user.isLocked());
    assertEquals("user", user.getFirstName());
    assertEquals("user", user.getLastName());
    assertEquals("$2a$10$SvCQN97uYNbi2PS11l3xfu/nPFdhuvCmiRwnDgAToyzWL0wldX8eq", user.getPassword());
    assertEquals(UserRole.USER, user.getRole());
  }

  @Test
  void findUserByEmail_whenNonExistingInDbUsersEmailIsPassed_thenNothingIsReturned() {
    Optional<User> result = userRepository.findUserByEmail("invalidUserEmail");

    assertFalse(result.isPresent());
  }

  @Test
  void findActiveUserByEmail_whenExistingInDbAndActiveUsersEmailIsPassed_thenThisUserIsReturned() {
    Optional<User> result = userRepository.findActiveUserByEmail("validUser");

    assertTrue(result.isPresent());
    User user = result.get();
    assertEquals(UUID.fromString("46a4f382-fafb-494c-a5ce-b14acbc307c4"), user.getId());
    assertEquals("validUser", user.getEmail());
    assertTrue(user.isEnabled());
    assertFalse(user.isLocked());
    assertEquals("user", user.getFirstName());
    assertEquals("user", user.getLastName());
    assertEquals("$2a$10$SvCQN97uYNbi2PS11l3xfu/nPFdhuvCmiRwnDgAToyzWL0wldX8eq", user.getPassword());
    assertEquals(UserRole.USER, user.getRole());
  }

  @Test
  void findActiveUserByEmail_whenExistingInDbAndDisabledUsersEmailIsPassed_thenNothingIsReturned() {
    Optional<User> result = userRepository.findActiveUserByEmail("disabledUser");

    assertFalse(result.isPresent());
  }

  @Test
  void findActiveUserByEmail_whenExistingInDbAndLockedUsersEmailIsPassed_thenNothingIsReturned() {
    Optional<User> result = userRepository.findActiveUserByEmail("lockedUser");

    assertFalse(result.isPresent());
  }

  @Test
  void findActiveUserByEmail_whenNonExistingInDbUsersEmailIsPassed_thenNothingIsReturned() {
    Optional<User> result = userRepository.findActiveUserByEmail("invalidUserEmail");

    assertFalse(result.isPresent());
  }
}
