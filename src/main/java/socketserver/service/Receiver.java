package socketserver.service;

import lombok.extern.slf4j.Slf4j;
import org.socketserver.model.Message;
import org.socketserver.model.Receipt;
import org.socketserver.repository.ClassifierStore;
import org.socketserver.repository.MessageRepository;
import org.socketserver.repository.ReceiptRepository;
import org.socketserver.util.JsonMapper;
import org.socketserver.util.MessageCheck;

import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
public class Receiver {

    private final MessageRepository messageRepository;
    private final ReceiptRepository receiptRepository;
    private BufferedReader bufferedReader;
    private final ClassifierStore classifierStore = new ClassifierStore();
    private static final String MESSAGE = "MESSAGE";
    private static final String RECEIPT = "RECEIPT";

    /**
     * Содержание квитанции доставленного сообщения
     */
    private static final String MESSAGE_DELIVERED = "Сообщение доставлено";
    /**
     * Содержание квитанции с сообщением об ошибке при приёме сообщения
     */
    private static final String MESSAGE_NUM_ERROR = "Нарушена сквозная нумерация";
    private static final String MESSAGE_HASH_ERROR = "Нарушена целостность (не совпадает hash)";


    public Receiver(MessageRepository messageRepository, ReceiptRepository receiptRepository, BufferedReader bufferedReader) {
        this.messageRepository = messageRepository;
        this.receiptRepository = receiptRepository;
        this.bufferedReader = bufferedReader;
    }

    public void listen() {
        String jsonObj;
        try {
            jsonObj = bufferedReader.readLine();
            execute(jsonObj);
        } catch (IOException e) {
            log.error("Ошибка в сервисе-получателе при чтении из сокета");
        }
    }

    public void execute(String jsonObj) {
        String objType = JsonMapper.getJsonObjectType(jsonObj);
        if (objType.equals(MESSAGE)) {
            Message message = JsonMapper.jsonToMessage(jsonObj);
            messageProcess(message);
        } else if (objType.equals(RECEIPT)) {
            Receipt receipt = JsonMapper.jsonToReceipt(jsonObj);
            receiptProcess(receipt);
        }
    }

    private void messageProcess(Message message) {
        String senderServerId = message.getSenderServerId();
        String checkStatus = MessageCheck.checkMsg(messageRepository.lastNumber(senderServerId), message);
        if (checkStatus.equals(MESSAGE_DELIVERED)) {
            successLogic(message);
        } else {
            errorLogic(message, checkStatus);
        }
    }

    private void successLogic(Message message) {
        message = messageRepository.addMessageIn(message).orElseThrow(
                () -> (new RuntimeException("Ошибка при добавлении в messageIn нового сообщения"))
        );
        Receipt receipt = receiptBuild(message);
        addReceiptOut(receipt, MESSAGE_DELIVERED);
    }


    private void errorLogic(Message message, String checkStatus) {
        if (checkStatus.equals(MESSAGE_HASH_ERROR)) {
            Receipt receipt = receiptBuild(message);
            addReceiptOut(receipt, MESSAGE_HASH_ERROR);
        } else if (checkStatus.equals(MESSAGE_NUM_ERROR)) {
            Receipt receipt = receiptBuild(message);
            addReceiptOut(receipt, MESSAGE_NUM_ERROR);
        }
    }

    private Receipt receiptBuild(Message message) {
        return new Receipt(
                message.getSourceMessageId(),
                message.getSenderServerId(),
                message.getReceiverServerId()
        );
    }

    /**
     * при добавлении квитанции в receipt_out её статус сразу устанавливается на "ожидание отправки"
     */
    private void addReceiptOut(Receipt receipt, String receiptStatus) {
        receiptRepository.addReceiptOut(receipt, receiptStatus).orElseThrow(
                () -> (new RuntimeException("Ошибка при добавлении квитанции"))
        );
    }
    //todo дописать разбивку на ошибки
    private void receiptProcess(Receipt receipt) {
        long sourceMessageId = receipt.getSourceMessageId();
        long receiptContentId = receipt.getReceiptContentId();
        receiptRepository.addReceiptIn(receipt);
        if (receiptContentId == classifierStore.getMESSAGE_RECEIVED()) {
            messageRepository.statusOutMessageChangeToConfirmed(sourceMessageId);
            messageRepository.setConfirmedTime(sourceMessageId);
        } else if (receiptContentId == classifierStore.getNUMERATION_ERROR()
            || receiptContentId == classifierStore.getHASH_ERROR()) {
            messageRepository.statusOutMessageChangeToError(sourceMessageId);
            log.info("Ошибка в сообщении  c id {}", sourceMessageId);
        }
    }
}
