package socketserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.socketserver.model.Message;
import org.socketserver.model.Receipt;

/**
 * Конвертирует передаваемый объект в Json и обратно
 */
@UtilityClass
public class JsonMapper {
    static ObjectMapper mapper = new ObjectMapper();

    public static String getJsonObjectType(String json) {
        try {
            return mapper.readTree(json)
                    .get("objectType")
                    .toString()
                    .replaceAll("[^\\w+]", "");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String messageToJson(Message message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Message jsonToMessage(String json) {
        try {
            return mapper.readValue(json, Message.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String receiptToJson(Receipt receipt) {
        try {
            return mapper.writeValueAsString(receipt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Receipt jsonToReceipt(String json) {
        try {
            return mapper.readValue(json, Receipt.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}