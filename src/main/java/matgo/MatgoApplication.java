package matgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MatgoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatgoApplication.class, args);
    }

}
