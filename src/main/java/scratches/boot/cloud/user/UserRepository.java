package scratches.boot.cloud.user;

import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

/**
 * @author Rashidi Zin
 */
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
