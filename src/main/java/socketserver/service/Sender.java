package socketserver.service;

import lombok.extern.slf4j.Slf4j;
import org.socketserver.model.Message;
import org.socketserver.model.Receipt;
import org.socketserver.repository.MessageRepository;
import org.socketserver.repository.ReceiptRepository;
import org.socketserver.util.JsonMapper;
import org.socketserver.util.MessageCheck;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class Sender {
    // todo переделать репозитории на singleton
    private final MessageRepository messageRepository;
    private final ReceiptRepository receiptRepository;
    private BufferedWriter bufferedWriter;
    private String typeFlag;
    private static final String RECEIPT = "receipt";
    private static final String MESSAGE = "message";
    private long currentSendingObjectId;
    private long currentSendingMessageNum;

    public Sender(MessageRepository messageRepository, ReceiptRepository receiptRepository, BufferedWriter bufferedWriter) {
        this.messageRepository = messageRepository;
        this.receiptRepository = receiptRepository;
        this.bufferedWriter = bufferedWriter;
    }

    //todo оптимизировать порядок проверки при необходимости ускорения получения сообщений из message_out
    public void send (String receiverServerId) {
        Optional<String> jsonToSend = getJsonToSend(receiverServerId);
        if (jsonToSend.isPresent()) {
            if (typeFlag.equalsIgnoreCase(MESSAGE)) {
                sendMessage(jsonToSend.get(), receiverServerId);
            } else if (typeFlag.equalsIgnoreCase(RECEIPT)) {
                sendReceipt(jsonToSend.get());
            }
        }

    }

    public void sendMessage(String message, String receiverServerId) {
        if (MessageCheck.checkMsgBeforeSend(currentSendingMessageNum, messageRepository.getLastConfirmedMsgNum(receiverServerId))) {
            try {
                writeToBuffer(message);
                messageRepository.setSentTime(currentSendingObjectId);
                messageRepository.statusOutMessageChangeToSent(currentSendingObjectId);
            } catch (IOException e) {
                log.error("Ошибка при отправке сообщения в сервисе-отправителе", e);
            }
        }
    }

    public void sendReceipt(String receipt) {
        try {
            writeToBuffer(receipt);
            receiptRepository.setSentTime(currentSendingObjectId);
            receiptRepository.changeReceiptOutStatusToSent(currentSendingObjectId);
        } catch (IOException e) {
            log.error("Ошибка при отправке квитанции в сервисе-отправителе", e);
        }

    }

    public boolean handShake(String senderServerId) {
        boolean rsl = false;
        try {
            writeToBuffer(senderServerId);
            rsl = true;
        } catch (IOException e) {
            log.error("Ошибка при установке соединения с сервером", e);
        }
        return rsl;
    }

    public Optional<String> getJsonToSend(String receiverServerId) {
        Optional<Receipt> receiptOptional = receiptRepository.getReceiptByEarliestDate(receiverServerId);
        if (receiptOptional.isPresent()) {
            typeFlag = RECEIPT;
            currentSendingObjectId = receiptOptional.get().getId();
            return Optional.of(JsonMapper.receiptToJson(receiptOptional.get()));
        } else {
            Optional<Message> optionalMessage = messageRepository.getMessageForSend(receiverServerId);
            if (optionalMessage.isPresent()) {
                typeFlag = MESSAGE;
                currentSendingObjectId = optionalMessage.get().getSourceMessageId();
                currentSendingMessageNum = optionalMessage.get().getMessageNum();
                return Optional.of(JsonMapper.messageToJson(optionalMessage.get()));
            }
        }
        return Optional.empty();
    }

    private void writeToBuffer(String strObjToWrite) throws IOException {
        bufferedWriter.write(strObjToWrite);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
}
