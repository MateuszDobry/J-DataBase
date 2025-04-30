package org.example.client;

import org.example.Message;
import org.example.RunConfig;

import java.io.IOException;

public class ClientTask implements Runnable {

    private final int id;
    private final RunConfig config;
    private final int bigArraySize;

    private final int serverPortNumber;

    @Override
    public void run() {
        // create a client instance, send data, receive data
        Client client = null;
        try {
            client = new Client("localhost", config, bigArraySize, serverPortNumber);

            client.work();

//            while (true) {
//                Message message = client.getMessage();
//                if (message == null) {
//                    continue;
//                }
//                if (message.msg.equals(""))
//                client.sendArray();
//                client.receiveArray();
//
//                client.saveBigArrayToTheFile(id);
//            }

        } catch (IOException e) {
            System.out.println("Error in thread " + Thread.currentThread().getName() + " " + e.getMessage());
        } finally {
            try {
                if (client != null)
                    client.close();

            } catch (IOException e) {
                throw new RuntimeException("Error closing connection in thread " + Thread.currentThread().getName(), e);
            }
        }
    }


    ClientTask(int id, RunConfig config, int bigArraySize, int serverPortNumber) {
        this.id = id;
        this.config = config;

        this.bigArraySize = bigArraySize;
        this.serverPortNumber = serverPortNumber;
    }
}
