package com.geekbrains.cloudStorage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Client implements Initializable {
    private static final int SIZE = 256;
    private Path clientDir;
    public ListView<String> clientView;
    public ListView<String> serverView;
    private DataInputStream is;
    private DataOutputStream os;
    private byte [] buf;


    private void readLoop () { //то что прилетает от сервера читаем тут
        try {
            while(true){
                String command = is.readUTF();// ждем сообщения
                System.out.println("received: " + command);
                if (command.equals("#list#")){
                    Platform.runLater(() ->
                            serverView.getItems().clear());
                    int filesCount = is.readInt();
                    for (int i = 0; i < filesCount; i++) {
                        String fileName = is.readUTF();

                        Platform.runLater(() ->
                                serverView.getItems().add(fileName));
                    }
                } else if (command.equals("#file#")){
                    String fileName = is.readUTF();
                    long size = is.readLong();
                    for (int i = 0; i < (size + SIZE-1)/SIZE ; i++) {
                        try(OutputStream fos = new FileOutputStream(clientDir.resolve(fileName).toFile())) {
                            int readBytes = is.read(buf);
                            fos.write(buf, 0, readBytes);
                        }
                    }
                    Platform.runLater(this :: updateClientView);

                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateClientView() {

        try {
            clientView.getItems().clear();
            Files.list(clientDir) // проходим по всем файлам и добавл. в clientView
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buf = new byte[256];
            clientDir = Paths.get(System.getProperty("user.home"));
            updateClientView();
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created");
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String filename = clientView.getSelectionModel().getSelectedItem();
        os.writeUTF("#file#");
        os.writeUTF(filename);
        Path file = clientDir.resolve(filename);
        long size = Files.size(file);
        byte [] bytes = Files.readAllBytes(file);
        os.writeLong(size);
        os.write(bytes);
        os.flush();

    }

    public void download(ActionEvent actionEvent) throws IOException {
        String filename = serverView.getSelectionModel().getSelectedItem();
        os.writeUTF("#get_file#");
        os.writeUTF(filename);
        os.flush();
    }
}

