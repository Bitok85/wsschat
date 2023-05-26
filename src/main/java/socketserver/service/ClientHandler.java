package socketserver.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.socketserver.util.PropertiesReader;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ClientHandler implements Runnable {

    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserName;
    private static final String MESSAGE_TYPE = "MESSAGE";
    private static final String RECEIPT_TYPE = "RECEIPT";

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) ;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            clientHandlers.add(this);
//            if (verifyClientName(clientUserName)) {
//                clientHandlers.add(this);
//            } else {
//                log.info("Ошибка аутентификации: имя {} отсутствует в списке клиентов", this.clientUserName);
//                closeEverything(socket, bufferedReader, bufferedWriter);
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    // todo сделать проверку на handshake или json сообщение
                    sendMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    // todo разобраться с размером сета с clientHandlers
    public void sendMessage(String jsonMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode messageTree = mapper.readTree(jsonMessage);
            String recipient = getRecipient(messageTree);
            for (ClientHandler clientHandler : clientHandlers) {
                //System.out.println(clientHandlers.size());
                if (clientHandler.clientUserName.equalsIgnoreCase(recipient)) {
                    clientHandler.bufferedWriter.write(jsonMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }

    public String getClientUserName() {
        return clientUserName;
    }

    private boolean verifyClientName(String name) {
        Set<String> clientsNames = new HashSet<>();
        try {
            clientsNames = PropertiesReader.getClientsSet();
        } catch (IOException e) {
            log.error("Ошибка при чтении файла с именами клиентов", e);
        }
        return clientsNames.contains(name);
    }


    //todo отрефакториить повторяющийся код
    private String getRecipient(JsonNode jsonNode) {
        String recipient = null;
        String objType = getSendingType(jsonNode);
        if (objType.equalsIgnoreCase(MESSAGE_TYPE)) {
            recipient = jsonNode.get("serverRecipientId")
                    .toString()
                    .replace("\"", "");
        } else if (objType.equalsIgnoreCase(RECEIPT_TYPE)) {
            recipient = jsonNode.get("senderServerId")
                    .toString()
                    .replace("\"", "");
        }
        if (recipient == null) {
            log.error("Не удалось определить получателя!");
            throw new IllegalArgumentException();
        }
        return recipient;
    }

    private String getSendingType(JsonNode jsonNode) {
        return jsonNode.get("objectType")
                .toString()
                .replace("\"", "");
    }

    // todo отрефакторить метод
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket!= null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
