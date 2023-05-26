package socketserver.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Модель содержащая время и текст ошибки при возврате сообщения.
 * При повторных ошибках текст ошибки добавляется в начало строки со сдвигом вниз предыдущих сообщений.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MessageError {

    private static final DateTimeFormatter FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @EqualsAndHashCode.Include
    private int errorId;

    @EqualsAndHashCode.Include
    private String errorText = "";

    private LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

    /**
     * @param error
     * добавляет новую ошибку со сдвигом предыдущей вниз
     */
    public void addErrorText(String error) {
        StringBuilder sb = new StringBuilder(errorText);
        sb.insert(0, errorFormatter(error));
        errorText = sb.toString();
    }

    private String errorFormatter(String error) {
        return created.format(FORMATTER)
                + " "
                + error
                + System.lineSeparator();
    }


}
