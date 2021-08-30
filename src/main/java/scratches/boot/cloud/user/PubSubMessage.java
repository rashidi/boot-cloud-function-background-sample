package scratches.boot.cloud.user;

import lombok.Data;

import java.util.Map;

/**
 * @author Rashidi Zin
 */
@Data
public class PubSubMessage {

    private String messageId;
    private String data;
    private Map<String, String> attributes;
    private String publishTime;

}
