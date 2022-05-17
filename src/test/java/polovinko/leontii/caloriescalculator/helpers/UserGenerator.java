package polovinko.leontii.caloriescalculator.helpers;

import polovinko.leontii.caloriescalculator.models.User;
import polovinko.leontii.caloriescalculator.models.UserRole;
import java.util.UUID;

public class UserGenerator {

  public static User createUser(UserRole role, String email) {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(email);
    user.setRole(role);
    user.setEnabled(true);
    return user;
  }
}
