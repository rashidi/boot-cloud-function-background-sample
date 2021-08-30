# Spring Cloud GCP Background Function Example
Demonstrate updating user's status through GCP Background Function

## Dependencies:
- [JDK](https://openjdk.java.net/) 11 or later
- [Project Lombok](https://projectlombok.org/)
- [Spring Cloud Function GCP Adapter](https://cloud.spring.io/spring-cloud-function/reference/html/spring-cloud-function.html#_google_cloud_functions_alpha)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [MongoDB TestContainers](https://www.testcontainers.org/modules/databases/mongodb/)
- [LocalServerTestSupport.java from Spring Cloud Function samples](https://github.com/spring-cloud/spring-cloud-function/blob/main/spring-cloud-function-samples/function-sample-gcp-http/src/test/java/com/example/LocalServerTestSupport.java)

Full dependencies can be found in [pom.xml](pom.xml)

## Implementations

### Test Implementation
We will start by implementing test which will verify that User's status will be updated. Our test will be based on 
the following scenario:

```gherkin
Given user status is ACTIVE
When a message is published with user status DORMANT
Then user status in database should be DORMANT
```

This is demonstrated in the following implementation:

```java
class UpdateUserStatusApplicationTests {

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
```

Full implementation can be found in [UpdateUserStatusApplicationTests](src/test/java/scratches/boot/cloud/user/UpdateUserStatusApplicationTests.java).

### Repository Implementation
[UserRepository](src/main/java/scratches/boot/cloud/user/UserRepository.java) will be responsible to update [User](src/main/java/scratches/boot/cloud/user/User.java) 
based on provided [UserStatus](src/main/java/scratches/boot/cloud/user/UserStatus.java)

```java
@Repository
@AllArgsConstructor
public class UserRepository {

    private final MongoTemplate template;

    public void updateStatusById(ObjectId id, UserStatus newStatus) {
        template.updateFirst(
                query(where("_id").is(id)),
                update("status", newStatus),
                User.class
        );
    }

}
```

### Function Implementation
Next, we will implement a [Background Function](https://cloud.google.com/functions/docs/writing/background) that will be triggered through 
[PubSubMessage](src/main/java/scratches/boot/cloud/user/PubSubMessage.java). `PubSubMessage` is a `class` that contains the structure of 
[GCP Pubsub Message](https://cloud.google.com/pubsub/docs/reference/rest/v1/PubsubMessage) structure.

```java
@Component
@AllArgsConstructor
public class UpdateUserStatusFunction implements Consumer<PubSubMessage> {

    private final UserRepository repository;

    @Override
    public void accept(PubSubMessage message) {
        var attributes = message.getAttributes();

        var id = new ObjectId(attributes.get("id"));
        var newStatus = UserStatus.valueOf(attributes.get("status"));

        repository.updateStatusById(id, newStatus);
    }

}
```

### Define Configuration Main Class
Finally, we will need to specify our configuration main class. This is defined in [MANIFEST.MF](src/main/resources/META-INF/MANIFEST.MF)

```manifest
Main-Class: scratches.boot.cloud.user.UpdateUserStatusApplication

```

## Verify
Once all are implemented, we will verify that our implementation works by executing `UpdateUserStatusApplicationTests.contextLoads`. If all 
implementation is correct, our verification that asserts `UserStatus` is now `DORMANT` will pass.

```java
class UpdateUserStatusApplicationTests {

    @Test
    @DisplayName("User status will be updated based on provided value in message")
    void contextLoads() throws IOException, InterruptedException {
        var updatedUser = findById(user.getId());

        assertThat(updatedUser.getStatus()).isEqualTo(DORMANT);
    }

}
```

## Running the Function Locally
We can also run the function locally through the following command:

```shell
mvn function:run
```

You can find more information at [Spring Cloud Function - Google Cloud Functions](https://cloud.spring.io/spring-cloud-function/reference/html/spring-cloud-function.html#_google_cloud_functions_alpha)
