package schultz.thomas.schub.connector.premadelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PremadeLabConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PremadeLabConnectorApplication.class, args);
    }
}
