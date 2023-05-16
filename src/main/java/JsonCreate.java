import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonCreate {

    public static void main(String[] args) throws JsonProcessingException {
        TestMessage testMessage = new TestMessage("dog", "cat", "test");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = objectMapper.writeValueAsString(testMessage);
        System.out.println(jsonMessage);
    }
}
