package info.matsumana.armeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Backend1Application {

    private static final Logger logger = LoggerFactory.getLogger(Backend1Application.class);

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Start shutting down");
        }));

        SpringApplication.run(Backend1Application.class, args);
    }
}
