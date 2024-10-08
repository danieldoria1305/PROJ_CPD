package org.project.client;

import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;
public class Client {
    private SSLSocket echoSocket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String token;
    private String username = null;

    public Client(String hostName, int portNumber) throws IOException {
        char[] passphrase = "changeit".toCharArray();//keystore password
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("src/main/java/org/project/certificate/keystore.jks"), passphrase);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, passphrase);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            echoSocket = (SSLSocket) sslSocketFactory.createSocket(hostName, portNumber);

            this.writer = new BufferedWriter(new OutputStreamWriter(echoSocket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch(NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | UnrecoverableKeyException e){
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        authenticate();

        // Create a virtual thread for output
        Thread outputThread = Thread.ofVirtual().start(() -> outputLoop());

        // Use the main thread for input
        inputLoop();
    }

    private void inputLoop() throws IOException {
        String serverInput;
        while (this.echoSocket.isConnected()) {
            try{
                serverInput = reader.readLine();
                if (serverInput != null) {
                    System.out.println(serverInput);
                }
            } catch (IOException e) {
                System.err.println("Error reading server output");
            }
        }
    }

    private void outputLoop() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (this.echoSocket.isConnected()) {
                String message = scanner.nextLine();
                writer.write(message + "\n");
                writer.flush();
            }

        } catch (IOException e) {
            System.err.println("Error writing to server");
        }
    }

    private void authenticate() throws IOException {
        String authType = promptUserForAuthType();
        if(this.username == null){
            this.username = promptUserForUsername();
        }

        if(authType.equals("REGISTER")){
            writer.write("AUTH_REGISTER\n");
            writer.flush();
        }
        else if(authType.equals("LOGIN")){
            String token = readTokenFromFile(username);
            writer.write("TOKEN," + (token != null ? token : "<null>") + "\n");
            writer.flush();
        }
        else {
            System.out.println("Invalid choice");
            return;
        }

        String serverResponse = reader.readLine();
        while (serverResponse != null) {
            String[] responseParts = serverResponse.split(",");
            switch (responseParts[0]) {
                case "AUTHENTICATED":
                    saveTokenToFile(responseParts[1]);
                    this.token = responseParts[1];
                    return;
                case "REQUEST_USERNAME":
                    writer.write("USERNAME," + username + "\n");
                    writer.flush();
                    break;
                case "REQUEST_PASSWORD":
                    String password = promptUserForPassword();
                    writer.write("PASSWORD," + password + "\n");
                    writer.flush();
                    break;
                case "ALREADY_AUTHENTICATED":
                    System.out.println("-----------------------------------------------\n" +
                                       "|     You already have an active session.     |\n" +
                                       "|  Please log out from your current session.  |\n" +
                                       "-----------------------------------------------");
                    username = promptUserForUsername();
                    writer.write("USERNAME," + username + "\n");
                default:
            }
            serverResponse = reader.readLine();
        }
    }

    private String readTokenFromFile(String username) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/org/project/client/tokens/" + username + ".token"));
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void saveTokenToFile(String token) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/project/client/tokens/" + username + ".token"));
            writer.write(token);
            writer.close();
        } catch (IOException e) {
            System.err.println("Error saving token to file");
        }
    }

    private String promptUserForAuthType() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(
                "\n-----------------------------------------------\n" +
                "|              Select an option:              |\n" +
                "|---------------------------------------------|\n" +
                "|   Register                             [0]  |\n" +
                "|   Login                                [1]  |\n" +
                "-----------------------------------------------\n"
        );
        if(scanner.hasNextInt()){
            int choice = scanner.nextInt();
            scanner.nextLine();
            if(choice == 0){
                return "REGISTER";
            } else if(choice == 1){
                return "LOGIN";
            }
        }
        return "INVALID";
    }

    private String promptUserForUsername() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n-----------------------------------------------\n" +
                           "|   Please enter your username. (q to quit)   |\n" +
                           "-----------------------------------------------\n");
        return scanner.nextLine();
    }

    private String promptUserForPassword() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n-----------------------------------------------\n" +
                           "|   Please enter your password.               |\n" +
                           "-----------------------------------------------\n");
        return scanner.nextLine();
    }
}
