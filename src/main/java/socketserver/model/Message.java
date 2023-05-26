package socketserver.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Основная модель данных сообщения. Получается из БД "message_out".
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Message {

    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Id Сообщения в таблице message_out. Используется для работы с объектом на стороне получателя.
     * При получении сообщения основное поле id обнуляется до добавления объекта в в таблицу message_in.
     */
    private long sourceMessageId;

    /**
     * id ФИЗИЧЕСКОГО объекта-получателя. По этим id работают процессы отправки в своих потоках.
     */
    private String receiverServerId;

    /**
     * Поле для десерелиазации объекта в сообщение из JSON
     */
    private final String objectType = "MESSAGE";

    /**
     * id физического объекта отправителя.
     */
    private String senderServerId;

    /**
     * сквозной номер сообщения для сервера-получателя. В таблице производится сквозная нумерация заисей, начиная с 1,
     * с инкрементом 1, не допускаются разрывы.
     */
    private long messageNum;


    /**
     * HashCode сообщения или файла для проверки целостности передачи на стороне получателя
     */
    private int outMessageHash;

    private LocalDateTime timeAdded;

    private LocalDateTime timeSent;

    private LocalDateTime timeReceived;

    private LocalDateTime timeConfirmed;

    /**
     * Статус передачи сообщения в БД:
     *     В очереди
     *     Отправлено
     *     Принято/не проверено
     *     Принято/проверено
     *     Сообщение об ошибке
     *     Подтверждено
     */
    private String currentStatus;

    /**
     * Поле с сообщением об ошибке/ах, если отправка не удалась
     */
    private MessageError error;

    /**
     * Тип сообщения (коды в классификаторе 634)
     *     Текст - 635
     *     Base64 (для файлов) - 636
     */
    private long messageType;

    /**
     * Данные отправителя и получателя в БД хранятся в jsonb
     */
    private String userSender;
    private String userRecipient;

    /**
     * Текст сообщения или ссылка на файл.
     */
    @EqualsAndHashCode.Include
    private String message;

}
