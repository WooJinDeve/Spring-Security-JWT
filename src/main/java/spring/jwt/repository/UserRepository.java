package spring.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.jwt.entity.Users;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
}
