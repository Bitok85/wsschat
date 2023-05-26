package socketserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Класс описывающий модель сообщения-квитанции. Оно формируется сервером-получателем и отправляется обратно отправителю
 * после получения и обработки основного сообщения.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Receipt {

    @EqualsAndHashCode.Include
    private long id;

    /**
     * Id сообщения, при получении и обработке которого, его получатель формирует по нему сообщение-квитанцию.
     */
    @EqualsAndHashCode.Include
    private long sourceMessageId;

    /**
     * Поле для десерелиазации объекта в сообщение из JSON
     */
    private final String objectType = "RECEIPT";

    /**
     * Время создания квитанции.
     */
    private LocalDateTime created;

    /**
     * Время получения квитанции сервером-отправителем исходного сообщения
     */
    private LocalDateTime received;

    /**
     * Id сервера ожидающего квитанции-подтверждения
     */
    private String senderServerId;

    /**
     * Id сервера отправившего квитанцию-подтверждение
     */
    private String receiverServerId;

    /**
     * Состояние квитанции для таблицы receipts_out ("ожидает отправки", "отправлена")
     */
    private long receiptStatusId;

    /**
     * Тип  для исходного сообщения:
     *     "0" - сообщение принято
     *     "1" - нарушена сквозная нумерация
     *     "2" - нарушена целостность (не совпадает hash)
     */
    private long receiptContentId;

    public Receipt(long sourceMessageId, String senderServerId, String receiverServerId) {
        this.sourceMessageId = sourceMessageId;
        this.senderServerId = senderServerId;
        this.receiverServerId= receiverServerId;
    }

}
