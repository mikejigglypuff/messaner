package messaner.config;

import messaner.model.Chat;
import messaner.model.User;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OtherBeanConfig {
    @Bean
    public List<Chat> chatList() {
        return new ArrayList<>();
    }

    @Bean
    public List<User> userList() {
        return new ArrayList<>();
    }
}
