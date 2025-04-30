package org.example.server;

import org.example.Message;
import org.example.rabbits.Zootopia;
import org.example.thread.SortableData;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class TCPClientHandler implements Runnable {

    private final String name;

    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private ArrayList<SortableData> bigArray;


    @Override
    public void run() {
        try {
            while (!Message.waitForStartMsg(this.in)) {
                // do nothing
            }
            Message.sendOk(this.out);

            Message.sendArray(this.out, this.bigArray);
            this.bigArray = Message.receiveArray(this.in);

            Message.sendEnd(this.out);

            System.out.println("recieved data");
            for (SortableData elem : this.bigArray)
                ServerLogger.log(elem.toString());

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Thread: " + this.getName() + ": Error handling client connection" + e.getMessage());
            ServerLogger.log("Thread: " + this.getName() + ": Error handling client connection" + e.getMessage());
        } finally {
            try {
                this.close();
            } catch (IOException e) {
                System.err.println("Thread: " + this.getName() + ": Error closing connection resources" + e.getMessage());
                ServerLogger.log("Thread: " + this.getName() + ": Error closing connection resources" + e.getMessage());
            }
        }
    }


    public TCPClientHandler(Socket socket, long seed, int bigArraySize, String name) {
        this.name = name;

        try {
            this.clientSocket = socket;
            this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.out.flush(); // without this, this might crash?

            this.in = new ObjectInputStream(this.clientSocket.getInputStream());

            Random rand = new Random(seed);
            this.bigArray = new ArrayList<>();
            for (int i = 0; i < bigArraySize; i++) {
                bigArray.add(new SortableData(rand));
            }

        } catch (IOException e) {
            ServerLogger.log("Thread: " + this.getName() + ": Could not create a client thread" + e.getMessage());
            throw new RuntimeException("Could not create a client thread" + e.getMessage());
        }
    }


    public String getName() {
        return this.name;
    }


    public void close() throws IOException {
        // not to generate an exception on the other side -
        // - if u try and read from inputStream without corresponding outputStream u get an Exception
        this.out.flush();

        this.in.close();
        this.out.close();
        this.clientSocket.close();
    }


    public void sendArray(ArrayList<SortableData> arrayToSend) throws IOException {
        // send the data in chunks to avoid network timeouts and it's just better
        int chunkSize = 10000;
        for (int i = 0; i < arrayToSend.size(); i += chunkSize) {
            ArrayList<SortableData> chunk = new ArrayList<>(
                arrayToSend.subList(i, Math.min(arrayToSend.size(), i + chunkSize))
            );
            this.out.writeObject(chunk);
        }

        // End marker
        this.out.writeObject(null);
        this.out.flush();
    }


    public ArrayList<SortableData> receiveArray() throws IOException, ClassNotFoundException {
        ArrayList<SortableData> receivedArray = new ArrayList<>();

        while (true) {
            Object recievedObject = this.in.readObject();
            // check if received an end marker
            if (recievedObject == null)
                break;

            if (recievedObject instanceof ArrayList<?>) {
                for (Object o : (ArrayList<?>) recievedObject) {
                    if (o instanceof SortableData) {
                        receivedArray.add((SortableData) o);
                    }
                }
            }
        }

        return receivedArray;
    }
}
