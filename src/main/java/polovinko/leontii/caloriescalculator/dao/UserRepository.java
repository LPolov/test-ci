package polovinko.leontii.caloriescalculator.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import polovinko.leontii.caloriescalculator.models.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findUserByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true AND u.locked = false")
  Optional<User> findActiveUserByEmail(String email);
}
