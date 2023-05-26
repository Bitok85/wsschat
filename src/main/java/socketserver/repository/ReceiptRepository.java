package socketserver.repository;

import lombok.extern.slf4j.Slf4j;
import org.socketserver.model.Receipt;
import org.socketserver.util.ConnectionInit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Класс c API для receipt(i/o)
 */
@Slf4j
public class ReceiptRepository {

    private final Connection connection;

    private final ClassifierStore classifierStore = new ClassifierStore();
    private static final long CLASSIFIER_CONTENT_EMPTY_CODE = -1;

    private static final String MESSAGE_DELIVERED = "Сообщение доставлено";
    /**
     * Содержание квитанции с сообщением об ошибке при приёме сообщения
     */
    // todo сделать enum, т.к эти констатны используются в трёх местах - в проверке сообщения, в репозитории и сервисе
    private static final String MESSAGE_NUM_ERROR = "Нарушена сквозная нумерация";
    private static final String MESSAGE_HASH_ERROR = "Нарушена целостность (не совпадает hash)";
    private static final String ADD_RECEIPT_OUT = "SELECT * FROM delivery.func_add_new_receipt_out(?, ?, ?)";
    private static final String ADD_RECEIPT_IN = "SELECT * FROM delivery.func_add_new_receipt_in(?, ?, ?)";
    private static final String GET_RECEIPT_FROM_RECEIPT_OUT_BY_EARLIEST_DATE_AND_SEND_WAITING
            = "SELECT * FROM delivery.func_get_min_receipt_out(?)";
    private static final String CHANGE_RECEIPT_OUT_STATUS = "SELECT * FROM delivery.func_set_receipt_out_status(?, ?)";
    private static final String SET_RECEIPT_TIME_SENT = "SELECT * FROM delivery.func_upd_sent_receipt_out(?)";


    public ReceiptRepository() {
        this.connection = ConnectionInit.getConnection();
    }

    /**
     * Добавить квитанцию в таблицу receipt_in
     */

    public Optional<Receipt> addReceiptIn(Receipt receipt) {
        try (PreparedStatement ps = connection.prepareStatement(ADD_RECEIPT_IN)) {
            ps.setLong(1, receipt.getSourceMessageId());
            ps.setString(2, receipt.getReceiverServerId());
            ps.setLong(3, receipt.getReceiptContentId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    receipt.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при добавлении квитанции в receipt_in.", e);
        }
        return Optional.of(receipt);
    }

    /**
     * Добавить квитанцию в таблицу receipt_out. Content - содержание по классификатору 637:
     * Сообщение доставлено
     * Нарушена сквозная нумерация
     * Нарушена целостность (не совпадает hash)
     */
    // todo добавить сервер получатель
    public Optional<Receipt> addReceiptOut(Receipt receipt, String content) {
        long receiptContentClassifierId = contentCode(content);
        if (receiptContentClassifierId == CLASSIFIER_CONTENT_EMPTY_CODE) {
            log.error("Не удалось получить корректный код классификатора для содержания квитанции");
            throw new RuntimeException();
        }
        try (PreparedStatement ps = connection.prepareStatement(ADD_RECEIPT_OUT)) {
            ps.setLong(1, receipt.getSourceMessageId());
            ps.setString(2, receipt.getSenderServerId());
            ps.setLong(3, receiptContentClassifierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    receipt.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при добавлении квитанции в receipt_out.", e);
        }
        return Optional.of(receipt);
    }


    /**
     * Получить самую раннюю квитанцию с состоянием "ожидает отправки"
     */

    public Optional<Receipt> getReceiptByEarliestDate(String senderServerId) {
        Optional<Receipt> result = Optional.empty();
        try (PreparedStatement ps
                     = connection.prepareStatement(GET_RECEIPT_FROM_RECEIPT_OUT_BY_EARLIEST_DATE_AND_SEND_WAITING)) {
            ps.setString(1, senderServerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Receipt receipt = new Receipt();
                    receipt.setId(rs.getLong("id"));
                    receipt.setSourceMessageId(rs.getLong("source_message_id"));
                    receipt.setSenderServerId(rs.getString("sender_server_id"));
                    receipt.setReceiverServerId(rs.getString("receiver_server_id"));
                    receipt.setReceiptContentId(rs.getLong("receipt_content"));
                    result = Optional.of(receipt);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при получении квитанции по самой ранней дате из receipt_out.", e);
        }
        return  result;
    }

    /**
     * Изменить поле состояния квитанции в таблице receipt_out на "отправлена"
     */
    public void changeReceiptOutStatusToSent(long receiptId) {
        try (PreparedStatement ps = connection.prepareStatement(CHANGE_RECEIPT_OUT_STATUS)) {
            ps.setLong(1, receiptId);
            ps.setLong(2, classifierStore.getSENT());
            ps.execute();
        } catch (SQLException e) {
            log.error("Ошибка при изменении статуса квитанции на 'отправлено'" , e);
        }
    }

    /**
     * Установить время отправки квитанции в receipt_out
     */
    public Optional<LocalDateTime> setSentTime(long receiptId) {
        Optional<LocalDateTime> result = Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(SET_RECEIPT_TIME_SENT)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime sentTime = rs.getTimestamp(1).toLocalDateTime();
                    result = Optional.of(sentTime);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при установки даты отправки в receipt_out" , e);
        }
        return result;
    }


    private long contentCode(String content) {
        long receiptContentClassifierId = CLASSIFIER_CONTENT_EMPTY_CODE;
        if (content.equalsIgnoreCase(MESSAGE_DELIVERED)) {
            receiptContentClassifierId = classifierStore.getMESSAGE_RECEIVED();
        } else if (content.equalsIgnoreCase(MESSAGE_NUM_ERROR)) {
            receiptContentClassifierId = classifierStore.getNUMERATION_ERROR();
        } else if (content.equalsIgnoreCase(MESSAGE_HASH_ERROR)) {
            receiptContentClassifierId = classifierStore.getHASH_ERROR();
        }
        return receiptContentClassifierId;
    }


}
