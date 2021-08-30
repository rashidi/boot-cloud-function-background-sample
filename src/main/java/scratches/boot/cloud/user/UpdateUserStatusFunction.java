package scratches.boot.cloud.user;

import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @author Rashidi Zin
 */
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
