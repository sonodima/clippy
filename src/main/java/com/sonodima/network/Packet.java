package com.sonodima.network;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Packet implements Serializable {
    private final Type type;
    private final Object data;
    private List<byte[]> filesData;

    public enum Type {
        files,
        string
    }

    public Packet(Object data, Type type) {
        super();

        this.type = type;
        this.data = data;

        if (type == Type.files) {
            filesData = new ArrayList<>();

            for (File file : (List<File>) data) {
                byte[] fileData = new byte[(int) file.length()];

                FileInputStream fileInputStream;
                try {
                    fileInputStream = new FileInputStream(file);
                    int ignored = fileInputStream.read(fileData);
                    fileInputStream.close();

                    filesData.add(fileData);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public String getString() {
        return (String) data;
    }

    public List<File> getFileList() {
        return (List<File>) data;
    }

    public byte[] getFileData(int index) {
        return filesData.get(index);
    }

    public Type getType() {
        return type;
    }
}
