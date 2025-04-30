package org.example.client;

import org.example.Message;
import org.example.RunConfig;
import org.example.rabbits.Zootopia;
import org.example.thread.SortableData;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private ArrayList<SortableData> bigArray;
    private final int chunkSize;
    private RunConfig config;
    private Zootopia zootopia;

    /* EXAMPLORY CONFIG */
    /* -p 2000 */
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        RunConfig config = new RunConfig(args);
        System.out.println(config);

        AtomicInteger idCounter = new AtomicInteger(1);

        Thread cli1 = new Thread(new ClientTask(idCounter.getAndIncrement(), config,
                100_000, config.getPortNumber()), "client-1");
        Thread cli2 = new Thread(new ClientTask(idCounter.getAndIncrement(), config,
                10_000, config.getPortNumber()), "client-2");
        Thread cli3 = new Thread(new ClientTask(idCounter.getAndIncrement(), config,
                50_000, config.getPortNumber()), "client-3");

        cli1.start();
        cli2.start();
        cli3.start();

        try {
            cli1.join();
            cli2.join();
            cli3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public Client(String host, RunConfig config, int bigArraySize, int portNumber) throws IOException {
        this.socket = new Socket(host, portNumber);

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush(); // again without it, it can crash

        this.in = new ObjectInputStream(socket.getInputStream());

        Random rand = new Random(config.getSeed());
        this.bigArray = null;
        this.config = config;

        this.chunkSize = 10_000;
        this.zootopia =  new Zootopia(config);
        this.zootopia.initFluffleFromFile("input-data/balanced-input.txt");
    }


    public void close() throws IOException {
        // again not to generate an exception on the other side
        this.out.flush();

        this.in.close();
        this.out.close();
        this.socket.close();
    }


    public void sendArray() throws IOException {
        // ofc send in chunks as in TCPClientHandler
        for (int i = 0; i < bigArray.size(); i += chunkSize) {
            ArrayList<SortableData> chunk = new ArrayList<>(
                bigArray.subList(i, Math.min(bigArray.size(), i + chunkSize))
            );
            this.out.writeObject(chunk);
        }

        // End marker
        this.out.writeObject(null);
        this.out.flush();
    }


    public void work() {
        Message startMsg = new Message(null,"start");
        try {
            Message.sendMessage(out,startMsg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                Message okMsg = Message.getMessage(in);

                if (okMsg.msg.equals("OK")){
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        while (true) {
            try {
                Message msg = Message.getMessage(in);
                if (msg.msg.equals("array")){
                    this.bigArray = msg.content;
                    zootopia.modifyVeryImportantSort(this.bigArray,10);
                    zootopia.initSort();

                    Message sendToSend = new Message(this.bigArray, "sorted");
                    Message.sendMessage(out, sendToSend);
                } else if(msg.msg.equals("end")){
                    System.out.println("END MET");
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void receiveArray() throws IOException, ClassNotFoundException {
        this.bigArray.clear();

        while (true) {
            Object recievedObject = this.in.readObject();
            // check if received an end marker
            if (recievedObject == null)
                break;

            if (recievedObject instanceof ArrayList<?>) {
                for (Object o : (ArrayList<?>) recievedObject) {
                    if (o instanceof SortableData) {
                        this.bigArray.add((SortableData) o);
                    }
                }
            }
        }

        if (!arraySorted())
            System.err.println("Received array not sorted properly");
    }


    public boolean arraySorted() {
        for (int i = 1; i < bigArray.size(); i++) {
            if (!(bigArray.get(i).compareTo(bigArray.get(i - 1)) <= 0))
                return false;
        }

        return true;
    }
    public Message getMessage() {
        try {
            Message message = Message.getMessage(in);
            return message;
        } catch (IOException ClassNotFoundException){
            System.out.println("Could not get message");
            return  null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveBigArrayToTheFile(int id) {
        String dirPath = "ClientsSortedArrays";
        new File(dirPath).mkdirs(); // makes a new folder if it does not exist

        String fileName = dirPath + "/" + id + "-bigArray.txt";
        File file = new File(fileName);

        if (file.exists() && !file.delete()) // deletes old file
            System.err.println("Could not delete Clients file");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(bigArray.toString()); //saves new data

            System.out.println("Client's " + id + " data is written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
