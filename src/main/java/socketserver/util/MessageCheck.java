package socketserver.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.socketserver.model.Message;

/**
 * Утилитный класс для проверки последнего сообщения в таблице message_in от одного отправителя
 * на неразрывность сквозной нумерации и целостности содержания по hash.
 */
@Slf4j
@UtilityClass
public class MessageCheck {

    /**
     * код сообщения прошедшего проверку
     */
    private static final String CHECKED = "Сообщение доставлено";

    /**
     * инкремент для сквозной нумерации сообщения
     */
    private static final int INCREMENT = 1;

    /**
     * Если запись в таблице первая, то предыдущий элемент будет равен 0 и проверка не производится
     */
    private static final int ZERO_LAST_NUMBER = 0;

    /**
     * код ошибки при нарушении сквозной нумерации
     */
    private static final String MESSAGE_NUM_ERROR = "Нарушена сквозная нумерация";

    /**
     * код ошибки при нарушении целостности содержимого сообщения при сравнении hash
     */
    private static final String MESSAGE_HASH_ERROR = "Нарушена целостность (не совпадает hash)";

    /**
     * Проверка последнего сообщения на неразрывность нумерации и целостность содержимого.
     */
    public static String checkMsg(long lastNumber, Message message) {
        if (!numerationCheck(message, lastNumber)) {
            return MESSAGE_NUM_ERROR;
        }
        if (!hashCheck(message)) {
            return MESSAGE_HASH_ERROR;
        }
        return CHECKED;
    }

    /**
     * Метод для проверки сквозной нумерации перед отправкой, чтобы она происходила только после подтверждения предыдущего сообщения.
     */
    public static boolean checkMsgBeforeSend(long currentMsgNum, long prevConfirmedMsgNum) {
        if (prevConfirmedMsgNum == ZERO_LAST_NUMBER) {
            return true;
        }
        return currentMsgNum - prevConfirmedMsgNum == INCREMENT;
    }

    /**
     * Проверка неразрывности нумерации поступившего сообщения с номером последнего записанного в базу
     */
    private static boolean numerationCheck(Message message, long lastNumber) {
        if (lastNumber == ZERO_LAST_NUMBER) {
            return true;
        }
        return message.getMessageNum() - lastNumber == INCREMENT;
    }


    /**
     * Проверка hash из входящего сообщения с hash содержания сообщения записанного в таблице message_in
     */
    private static boolean hashCheck(Message message) {
        return message.getOutMessageHash() == message.getMessage().hashCode();
    }
}
