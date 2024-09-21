import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done = false;
    private ExecutorService pool;
    private RSA rsa;
    private String publicKeyString;

    public Server() {
        connections = new ArrayList<>();
        rsa = new RSA();
        rsa.init();
        publicKeyString = Base64.getEncoder().encodeToString(rsa.getPublicKey().getEncoded());
    }

    @Override
    public void run(){
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler connectionHandler : connections) {
            if (connectionHandler != null) {
                connectionHandler.sendMesssage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }

            for(ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {

        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ConnectionHandler(Socket client){
            this.client = client;
        }


        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("PUBLIC_KEY: "+ getPublicKeyString() + "\nPlease enter a name: ");
                name = rsa.decrypt(in.readLine());
                out.println(name + " connected");
                broadcast(name + " joined the chat");
                String message;
                while((message = in.readLine()) != null) {
                    String decryptedMessage = getRsa().decrypt(message);
                    if (decryptedMessage.startsWith("/name ")) {
                        String[] messageSplit = decryptedMessage.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(name + " renamed themselves to " + messageSplit[1]);
                            System.out.println(name + " renamed themselves to " + messageSplit[1]);
                            name = messageSplit[1];
                            out.println("Successfully changed name to " + name);
                        } else {
                            out.println("No not provided");
                        }
                    } else if (decryptedMessage.startsWith("/quit")) {
                        broadcast(name + " left the chat");
                        shutdown();
                    } else {
                        broadcast(name + ": " + decryptedMessage);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }

        private RSA getRsa() {
            return rsa;
        }

        public void sendMesssage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();

                }
            } catch (IOException e) {
                //ignore
            }
        }
        public String getPublicKeyString() {
            return publicKeyString;
        }
    }


    public static void main(String[] args) {
        System.out.println("SERVER STARTED");
        Server server = new Server();
        server.run();
    }
}