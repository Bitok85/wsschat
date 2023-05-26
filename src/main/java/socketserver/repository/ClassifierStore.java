package socketserver.repository;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс содержащий мапу с ключом-id клсассификатора и его текстовым значением
 */
@Getter
public class ClassifierStore {

    //todo сверить с текстовыми кодами классификаторов в бд

    /**
     * Классификаторы:
     * 629 - статус передачи сообщений
     * 634 - Тип сообщения
     * 637 - тип квитовки (receipt_content)
     */
    private final long SEND_WAITING = 630;
    private final long SENT = 631;
    private final long CONFIRMED = 632;
    private final long ERROR_MESSAGE = 633;
    private final long RECEIVED = 648;
    private final long MESSAGE_RECEIVED = 638;
    private final long NUMERATION_ERROR = 639;
    private final long HASH_ERROR = 640;
    private final long TEXT = 635;
    private final long BASE64 = 636;

    private final Map<Long, String> classifiers = new ConcurrentHashMap<>();
    public ClassifierStore() {
        classifiers.put(SEND_WAITING, "В очереди");
        classifiers.put(SENT, "Отправлено");
        classifiers.put(CONFIRMED, "Подтверждено");
        classifiers.put(ERROR_MESSAGE, "Сообщение об ошибке");
        /**
         * Статус самого сообщения или квитанции
         */
        classifiers.put(RECEIVED, "Принятно");
        /**
         * Содержание квитанции относительно сообщения информацию по доставке которого она несёт
         */
        classifiers.put(MESSAGE_RECEIVED, "Сообщение принято");
        classifiers.put(NUMERATION_ERROR, "Нарушена сквозная нумерация");
        classifiers.put(HASH_ERROR, "Нарушена целостность(не совпадает hash)");
        classifiers.put(TEXT, "Текст");
        classifiers.put(BASE64, "Base64");
    }

    public String getSendWaitingValue() {
        return classifiers.get(SEND_WAITING);
    }

    public String getSentValue() {
        return classifiers.get(SENT);
    }

    public String getConfirmedValue() {
        return classifiers.get(CONFIRMED);
    }

    public String getErrorMsgValue() {
        return classifiers.get(ERROR_MESSAGE);
    }

    public String getReceivedMsgValue() {
        return classifiers.get(RECEIVED);
    }

    public String getMsgReceivedForReceiptContent() {
        return classifiers.get(MESSAGE_RECEIVED);
    }

    public String getNumErrorValue() {
        return classifiers.get(NUMERATION_ERROR);
    }

    public String getHashErrorValue() {
        return classifiers.get(HASH_ERROR);
    }

    public String getTextValue() {
        return  classifiers.get(TEXT);
    }

    public String getBase64Value() {
        return classifiers.get(BASE64);
    }


}
