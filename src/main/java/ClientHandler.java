import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//todo добавить логи
//todo добавить jar plugin
public class ClientHandler implements Runnable {

    public static List<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserName;

    private JsonNode messageTree;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) ;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String jsonMessage = bufferedReader.readLine();
            ObjectMapper objectMapper = new ObjectMapper();
            messageTree = objectMapper.readTree(jsonMessage);
            this.clientUserName = messageTree.get("senderServerId").toString();
            clientHandlers.add(this);
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
                sendMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void sendMessage(String JSONMessage) {
        String receiverServerId = messageTree.get("receiverServerId").toString();
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.clientUserName.equalsIgnoreCase(receiverServerId)) {
                    clientHandler.bufferedWriter.write(JSONMessage);
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

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
