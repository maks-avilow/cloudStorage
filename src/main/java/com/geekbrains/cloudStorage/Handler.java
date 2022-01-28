package com.geekbrains.cloudStorage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements Runnable {
    private static final int SIZE = 256;
    private byte [] buf;
    private Path clientDir;
    private DataInputStream is;
    private DataOutputStream os;

    public Handler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        clientDir = Paths.get("data");
        buf = new byte[SIZE];
        sendServerFiles();
    }

    public DataInputStream getIs() { return is;
    }
    public void setIs(DataInputStream is) { this.is = is;
    }
    public DataOutputStream getOs() { return os;
    }
    public void setOs(DataOutputStream os) { this.os = os;
    }

    public void sendServerFiles() throws IOException {
        List<String> files = Files.list(clientDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        os.writeUTF("#list#");
        os.writeInt(files.size());
        for (String file : files) {
            os.writeUTF(file);
        }
        os.flush();
    }
    @Override
    public void run() {
        try{
            while (true){
                String command = is.readUTF();
                System.out.println("received:" + command);
                if (command.equals("#file#")){
                    String fileName = is.readUTF();
                    long size = is.readLong();
                    for (int i = 0; i < (size + SIZE-1)/SIZE ; i++) {
                        try(OutputStream fos = new FileOutputStream(clientDir.resolve(fileName).toFile())) {
                            int readBytes = is.read(buf);
                            fos.write(buf, 0, readBytes);
                        }
                    }
                    sendServerFiles();
                } else if (command.equals("#get_file#")){
                    String fileName = is.readUTF();
                    os.writeUTF("#file#");
                    os.writeUTF(fileName);
                    Path file = clientDir.resolve(fileName);
                    long size = Files.size(file);
                    byte [] bytes = Files.readAllBytes(file);
                    os.writeLong(size);
                    os.write(bytes);
                    os.flush();
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }}



