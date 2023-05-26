package socketserver.repository;

import lombok.extern.slf4j.Slf4j;
import org.socketserver.model.Message;
import org.socketserver.util.ConnectionInit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Класс c API message(i/o)
 */
@Slf4j
public class MessageRepository {

    private final Connection connection;

    private final ClassifierStore classifierStore = new ClassifierStore();

    private static final String ADD_MESSAGE_OUT = "SELECT FROM * delivery.func_add_new_message_out(?, ?, ?, ?, ?, ?, ?)";

    private static final String GET_MESSAGE_TO_SEND_WITH_MIN_MESSAGE_NUM_AND_WAITING_OR_ERROR_STATUS_BY_SERVER_RECIPIENT_ID
            = "SELECT * FROM delivery.func_get_min_message2send(?)";

    private static final String TIME_SENT_UPDATE = "SELECT * FROM delivery.func_upd_time_sent_out(?)";
    private static final String TIME_CONFIRMED_UPDATE = "SELECT * FROM delivery.func_upd_time_confirmed_out(?)";
    private static final String FIND_LAST_MESSAGE_NUM_BY_SENDER = "SELECT * FROM delivery.func_get_max_order_received(?)";
    private static final String ADD_MESSAGE_IN = "SELECT * FROM delivery.func_add_new_message_in(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String STATUS_MESSAGE_OUT_CHANGE = "SELECT * FROM delivery.func_set_message_out_status(?, ?)";
    private static final String LAST_CONFIRMED_MESSAGE_NUM = "SELECT * FROM delivery.func_get_max_order_confirmed(?)";
    private static final long ZERO_LAST_NUMBER = 0;

    public MessageRepository() {
        this.connection = ConnectionInit.getConnection();
    }


    /**
     * Получить последний номер сквозной нумерации по отправителю из message_in
     */
    public long lastNumber(String senderServerId) {
        long result = ZERO_LAST_NUMBER;
        try (PreparedStatement ps = connection.prepareStatement(FIND_LAST_MESSAGE_NUM_BY_SENDER)) {
            ps.setString(1, senderServerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = rs.getLong(1);
                    System.out.println(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при получении номера сквозной нумерации от последнего проверенного сообщения в таблице message_in");
        }
        return result;
    }

    /**
     * Добавление в сообщения в таблицу message_out
     */

    public Optional<Message> addMessageOut(Message message) {
        Optional<Message> result = Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(ADD_MESSAGE_OUT)) {
//            ps.setLong(1, message.getSourceMessageId());
//            ps.setString(2, message.getMessage());
//            ps.setString(3, String.valueOf(message.getOutMessageHash()));
//            ps.setLong(4, message.getMessageNum());
//            ps.setString(5, message.getReceiverServerId());
//            ps.setString(6, message.getUserRecipient());
//            ps.setString(7, message.getSenderServerId());
//            ps.setString(8, message.getUserSender());
            ps.setLong(9, message.getMessageType());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    message.setId(rs.getLong(1));
                    result = Optional.of(message);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при добавлении сообщения в таблицу message_out");
        }
        return result;
    }


    /**
     Запись полученного сообщения в таблицу message_in
     */
    public Optional<Message> addMessageIn(Message message) {
        Optional<Message> result = Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(ADD_MESSAGE_IN)) {
            ps.setLong(1, message.getSourceMessageId());
            ps.setString(2, message.getMessage());
            ps.setString(3, String.valueOf(message.getOutMessageHash()));
            ps.setLong(4, message.getMessageNum());
            ps.setString(5, message.getReceiverServerId());
            ps.setString(6, message.getUserRecipient());
            ps.setString(7, message.getSenderServerId());
            ps.setString(8, message.getUserSender());
            ps.setLong(9, message.getMessageType());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    message.setId(rs.getLong(1));
                    result = Optional.of(message);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при добавлении сообщения в таблицу message_in");
        }
        return result;
    }

    /**
     * Смена статуса у сообщения на "Отправлено" в таблице message_out
     */
    public void statusOutMessageChangeToSent(long id) {
        long statusConfirmedId = classifierStore.getSENT();
        try {
            statusOutMessageChange(id, statusConfirmedId);
        } catch (SQLException e) {
            log.error("Ошибка при изменении статуса сообщения в message_out на {}", classifierStore.getSentValue());
        }
    }


    /**
     * Смена статуса у сообщения на "Доставлено" в таблице message_out
     */
    public void statusOutMessageChangeToConfirmed(long id) {
        long statusConfirmedId = classifierStore.getCONFIRMED();
        try {
            statusOutMessageChange(id, statusConfirmedId);
        } catch (SQLException e) {
            log.error("Ошибка при изменении статуса сообщения в message_out на {}", classifierStore.getConfirmedValue());
        }
    }

    /**
     * Смена статуса у сообщения на "Ошибка" в таблице message_out
     */
    public void statusOutMessageChangeToError(long id) {
        long statusErrorId = classifierStore.getERROR_MESSAGE();
        try {
            statusOutMessageChange(id, statusErrorId);
        } catch (SQLException e) {
            log.error("Ошибка при изменении статуса сообщения в message_out на {}", classifierStore.getErrorMsgValue());
        }

    }

    private void statusOutMessageChange(long id, long status) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(STATUS_MESSAGE_OUT_CHANGE);
        ps.setLong(1, id);
        ps.setLong(2, status);
        ps.execute();
        ps.close();
    }

    /**
     * Получить объект из таблицы message_out по минимальному message_num и статусу "в очереди/ошибка"
     */

    // todo доработать по дате добавления. Jackson не мапит localDateTime
    // todo разобраться почему не возвращается Resultset полноценный для 654, 1115 и 790
    public Optional<Message> getMessageForSend(String receiverServerId) {
        Optional<Message> result = Optional.empty();
        try (PreparedStatement ps
                     = connection.prepareStatement(
                             GET_MESSAGE_TO_SEND_WITH_MIN_MESSAGE_NUM_AND_WAITING_OR_ERROR_STATUS_BY_SERVER_RECIPIENT_ID)
        ) {
            ps.setString(1, receiverServerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Message message = new Message();
                    message.setSourceMessageId(rs.getLong("id"));
                    //message.setTimeAdded(rs.getTimestamp("time_added").toLocalDateTime());
                    message.setMessageType(rs.getLong("message_type"));
                    message.setMessage(rs.getString("message"));
                    message.setOutMessageHash(Integer.parseInt(rs.getString("message_hash")));
                    message.setMessageNum(rs.getLong("message_num"));
                    message.setUserSender(rs.getString("sender"));
                    message.setUserRecipient(rs.getString("recipient"));
                    message.setSenderServerId(rs.getString("sender_server_id"));
                    message.setReceiverServerId(rs.getString("recipient_server_id"));
                    System.out.println(rs.getString("recipient_server_id"));
                    result = Optional.of(message);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при получении сообщения из message_out для отправки", e);
        }
        return result;
    }

    /**
     * Установить время отправки квитанции в message_out
     */
    public Optional<LocalDateTime> setSentTime(long messageId) {
        Optional<LocalDateTime> result = Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(TIME_SENT_UPDATE)) {
            ps.setLong(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime timeSent = rs.getTimestamp(1).toLocalDateTime();
                    result = Optional.of(timeSent);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при установке времени отправки в message_out", e);
        }
        return result;
    }

    public Optional<LocalDateTime> setConfirmedTime(long messageId) {
        Optional<LocalDateTime> result = Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(TIME_CONFIRMED_UPDATE)) {
            ps.setLong(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime timeConfirmed = rs.getTimestamp(1).toLocalDateTime();
                    result = Optional.of(timeConfirmed);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при установке времени подтверждения в message_out", e);
        }
        return result;
    }

    public long getLastConfirmedMsgNum(String receiverServerId) {
        long result = ZERO_LAST_NUMBER;
        try (PreparedStatement ps = connection.prepareStatement(LAST_CONFIRMED_MESSAGE_NUM)) {
            ps.setString(1, receiverServerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при получении номера последнего подтверждённого сообщения в message_out", e);
        }
        return result;
    }

}
