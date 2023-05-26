package socketserver.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserRecipient {

    @EqualsAndHashCode.Include
    private String login;

    /**
     * Идентификатор исполняющего процесса, осуществляющего обработку сообщения на принимающей стороне
     */
    private String process;
}
