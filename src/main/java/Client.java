import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    private static ObjectMapper objectMapper = new ObjectMapper();

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter =  new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) ;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        TestMessage testMessage = new TestMessage("Center", "Edge", "Text");
        //ObjectMapper objectMapper = new ObjectMapper();
        String jsonTestMessage = objectMapper.writeValueAsString(testMessage);
        client.listenForMessage();
        client.sendMessage(jsonTestMessage);
    }

    public void sendMessage(String jsonMessage) {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            while (socket.isConnected()) {
                bufferedWriter.write(jsonMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //todo logs
        }

    }

    public void listenForMessage() {
        Thread listenMsg = new Thread(
                () -> {
                    String jsonMessage;
                    while (socket.isConnected()) {
                        try {
                            jsonMessage = bufferedReader.readLine();
                            System.out.println(objectMapper.readValue(jsonMessage, TestMessage.class));
                            //todo логика обработки
                        } catch (IOException e) {
                            e.printStackTrace();
                            //todo log
                        }
                    }
                }
        );
        listenMsg.start();
    }

//    public void listenForMessage() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String messageFromChat;
//                while (socket.isConnected()) {
//                    try {
//                        messageFromChat = bufferedReader.readLine();
//                        System.out.println(messageFromChat);
//                    } catch ( IOException e) {
//                        closeEverything(socket, bufferedReader, bufferedWriter);
//                    }
//                }
//            }
//        }).start();
//    }

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
