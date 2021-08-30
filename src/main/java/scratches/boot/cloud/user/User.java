package scratches.boot.cloud.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Rashidi Zin
 */
@Getter
@Document
@RequiredArgsConstructor
public class User {

    @Id
    private ObjectId id;

    private final UserStatus status;

}
