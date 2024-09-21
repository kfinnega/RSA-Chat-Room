import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private RSA rsa;

    public Client() {
        rsa = new RSA();
    }

    @Override
    public void run() {
        try {
            client = new Socket("localhost", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                if (inMessage.startsWith("PUBLIC_KEY:")) {
                    String[] parts = inMessage.split(" ", 2);
                    String publicKeyString = parts[1];
                    rsa.setPublicKeyString(publicKeyString);
                    System.out.println("public key " + publicKeyString);
                    rsa.initFromStrings();
                    System.out.println(parts[1]);
                } else {
                    System.out.println(inMessage);
                }
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown(){
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        out.println(message);
                        inReader.close();
                        shutdown();
                    } else {
                        try {
                            String encryptedMessage = rsa.encrypt(message);
                            out.println(encryptedMessage);
                        } catch (Exception e) {
                            System.out.println("Error encrypting message: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}