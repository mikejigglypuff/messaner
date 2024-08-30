package messaner.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${spring.data.mongodb.dbname}")
  private String dbName;
  @Value("${spring.data.mongodb.uri}")
  private String uri;

  private final String envDBName;
  private final String envURI;

  MongoConfig() {
    envURI = System.getenv("MONGO_URI");
    envDBName = System.getenv("MONGO_DBNAME");
  }

  @Override
  public String getDatabaseName() {
    return (envDBName != null) ? envDBName : dbName;
  }

  @Override
  public MongoClient mongoClient() {
    if (uri != null) {
      return MongoClients.create(uri);
    }
    return MongoClients.create(envURI);
  }

  @Bean
  public MongoTemplate MongoTemplate() throws Exception {
    MongoTemplate template = new MongoTemplate(mongoClient(), getDatabaseName());
    template.setSessionSynchronization(SessionSynchronization.ALWAYS);
    return template;
  }

  @Bean
  public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
    return new MongoTransactionManager(mongoDatabaseFactory);
  }
}
