package messaner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

@SpringBootTest
@PropertySource("classpath:application-dev.properties")
class MessanerApplicationTests {

	@Test
	void contextLoads() {
	}

}
