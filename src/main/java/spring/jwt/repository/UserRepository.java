package spring.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.jwt.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
    public Users findByUsername(String username);
}
