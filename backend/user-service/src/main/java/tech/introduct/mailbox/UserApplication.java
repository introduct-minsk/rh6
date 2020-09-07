package tech.introduct.mailbox;

import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UserApplication {

    @Generated
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
