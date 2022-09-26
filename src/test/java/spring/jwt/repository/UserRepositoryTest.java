package spring.jwt.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.jwt.entity.Users;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRepositoryTest {
    
    @Autowired
    UserRepository userRepository;
    
    @Test
    void find(){
        String name = "woojin";

        Optional<Users> byUsername = userRepository.findByUsername(name);
        Assertions.assertThat(name).isEqualTo(byUsername.get().getUsername());
    }

}