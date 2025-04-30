package org.example.server;

import org.example.InputListener;
import org.example.Message;
import org.example.RunConfig;
import org.example.rabbits.Zootopia;
import org.example.thread.Globals;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
    private final AtomicInteger idCounter;

    private boolean running;

    private ServerSocket serverSocket;
    // keep for managing client connections outside of threads
    private final ArrayList<Socket> clientSockets;
    private ExecutorService clientExecutor;

    /* EXAMPLORY CONFIG */
    /* -N -p 2000 */
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        RunConfig config = new RunConfig(args);
        System.out.println(config);

        Server server = null;
        try {
            server = new Server(config.getPortNumber());
            InputListener listener = new InputListener();
            listener.start();

            Zootopia.generateBalancedFluffle("input-data/balanced-input.txt", 8000, 5, 10);

            // we want to have a zootopia tree to manage sorting happening on the server
            Zootopia zootopia = new Zootopia(config.getDataStructure());
            zootopia.initFluffleFromFile("input-data/balanced-input.txt");

            server.acceptClients(zootopia,config.getSeed());
        } catch (IOException e) {
            System.err.println("Server could not be started on port: " + config.getPortNumber());
            ServerLogger.log("Server could not be started on port " + config.getPortNumber());

            // making sure that the server is always closed properly
        } finally {
            if (server != null && server.isRunning())
                server.stop();
        }
    }


    public Server(int portNumber) throws IOException {
        this.idCounter = new AtomicInteger(1);
        this.running = true;

        this.clientSockets = new ArrayList<>();
        this.serverSocket = new ServerSocket(portNumber);

        // we want dynamic thread pool not to waste resources on client connections
        this.clientExecutor = new ThreadPoolExecutor(
                4, // be careful -- this set to 1 will make multithreading useless?? nice Java
                16,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
    }


    public boolean isRunning() {
        return this.running;
    }


    public void stop() {
        this.running = false;
        try {
            broadcastEnd();
            clientExecutor.shutdown();
            // returns false if after waiting threads did not terminate
            // they will just send unsorted / partially sorted array back to the client
            if (!clientExecutor.awaitTermination(60, TimeUnit.SECONDS))
                clientExecutor.shutdownNow();

        } catch (IOException | InterruptedException e) {
            System.err.println("Could not close server resources");
            ServerLogger.log("Could not close server resources");
            clientExecutor.shutdownNow();
        } finally {
            try {
                closeClients();
                this.serverSocket.close();
            } catch (IOException e) {

            }
        }
    }


    public void closeClients() throws IOException {
        for (Socket clientSocket : clientSockets) {
            if (clientSocket.isClosed())
                continue;

            clientSocket.close();
        }
    }


    public void acceptClients(Zootopia zootopia,long seed) {
        while (this.running) {
            try {
                // after 1sec it throws so we can check
                serverSocket.setSoTimeout(1000);
                Socket clientSocket = serverSocket.accept();

                clientSockets.add(clientSocket);
                ServerLogger.log("New client connected: " + clientSocket.getInetAddress());
                Random rand = new Random();
                int randint = rand.nextInt(100001 - 10000) + 10000;


                // Start a thread to handle this client
                this.clientExecutor.submit(new TCPClientHandler(clientSocket, seed,randint,
                        "Thread" + idCounter.getAndIncrement() + " " + clientSocket.getInetAddress()));

                // throw happens after the timeout, and we want it to continue listening
            } catch (IOException e) {
                if (Globals.CLOSE_PROGRAM)
                    this.stop();
            }
        }
    }


    public void broadcastEnd() throws IOException {
        for (Socket clientSocket : this.clientSockets) {
            if (clientSocket.isClosed())
                continue;

            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

            Message.sendEnd(out);
        }
    }
}