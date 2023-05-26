package socketserver.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserSender {

    /**
     * Имя пользователя, от имени которого поступило сообщение
     * (от имени которого работает программа, сделавшая текущую запись)
     */
    @EqualsAndHashCode.Include
    private String login;

    /**
     * Имя программного модуля, сделавшего текущую запись
     */
    private String program;

    /**
     * IP адрес, с которого работает программа, сделавшая текущую запись.
     */
    @EqualsAndHashCode.Include
    private String address;
}
