package scratches.boot.cloud.user;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static scratches.boot.cloud.user.LocalServerTestSupport.startServer;
import static scratches.boot.cloud.user.UserStatus.ACTIVE;
import static scratches.boot.cloud.user.UserStatus.DORMANT;

@Testcontainers
class UpdateUserStatusApplicationTests {

    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo"));

    private static MongoTemplate template;
    private static User user;

    @BeforeAll
    static void setup() {
        template = setupMongo();

        user = template.insert(new User(ACTIVE));
    }

    @Test
    @DisplayName("User status will be updated based on provided value in message")
    void contextLoads() throws IOException, InterruptedException {
        var restTemplate = new TestRestTemplate();
        var message = messageWithNewUserStatus();

        try(var process = startServer(UpdateUserStatusApplication.class, MONGO_DB_CONTAINER)) {
            restTemplate.postForObject("http://localhost:8080/", message, String.class);
        }

        var updatedUser = findById(user.getId());

        assertThat(updatedUser.getStatus()).isEqualTo(DORMANT);
    }

    private static MongoTemplate setupMongo() {
        var client = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(
                                new ConnectionString(MONGO_DB_CONTAINER.getReplicaSetUrl())
                        )
                        .build()
        );

        return new MongoTemplate(client, "test");
    }

    private User findById(ObjectId id) {
        return template.findById(id, User.class);
    }

    private PubSubMessage messageWithNewUserStatus() {
        var message = new PubSubMessage();

        message.setAttributes(Map.of(
                "id", user.getId().toString(),
                "status", DORMANT.name()
        ));

        return message;
    }

}
