package org.example;


import org.example.thread.SortableData;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class Message implements Serializable {

    public final String msg;
    public final ArrayList<SortableData> content;


    public Message(ArrayList<SortableData> content, String msg) {
        this.content = content;

        this.msg = msg;
    }


    public String getString() {
        return this.msg;
    }


    public ArrayList<SortableData> getArray() {
        return this.content;
    }


    public static void sendMessage(ObjectOutputStream out, Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }


    public static Message getMessage(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object recievedObject = in.readObject();

        if (recievedObject instanceof Message)
            return (Message) recievedObject;

        return null;
    }


    public static boolean waitForStartMsg(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Message message = Message.getMessage(in);

        return message != null && Objects.equals(message.getString(), "start");
    }


    public static void sendOk(ObjectOutputStream out) throws IOException {
        Message newMessage = new Message(null, "OK");

        Message.sendMessage(out, newMessage);
    }


    public static void sendEnd(ObjectOutputStream out) throws IOException {
        Message newMessage = new Message(null, "end");

        Message.sendMessage(out, newMessage);
    }


    public static void sendArray(ObjectOutputStream out, ArrayList<SortableData> bigArray) throws IOException {
        Message newMessage = new Message(bigArray, "array");

        Message.sendMessage(out, newMessage);
    }


    public static ArrayList<SortableData> receiveArray(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Message message = Message.getMessage(in);

        if (message != null && Objects.equals(message.getString(), "sorted"))
            return message.getArray();

        return null;
    }
}
