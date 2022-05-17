package polovinko.leontii.caloriescalculator.services.authorization;

import org.springframework.security.core.userdetails.UserDetailsService;
import polovinko.leontii.caloriescalculator.models.User;

public interface UserService extends UserDetailsService {

  User loadActiveUserByEmail(String email);
}
