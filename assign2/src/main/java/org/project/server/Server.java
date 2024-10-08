package org.project.server;

import org.project.database.DatabaseManager;
import org.project.server.matchmaking.MatchmakingStrategy;
import org.project.server.matchmaking.RankedMatchmaking;
import org.project.server.matchmaking.SimpleMatchmaking;
import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;

public class Server {
    private SSLServerSocket serverSocket;
    private DatabaseManager databaseManager;

    public Server(int portNumber) {
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

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            this.serverSocket = (SSLServerSocket) ssf.createServerSocket(portNumber);
            this.databaseManager = new DatabaseManager();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException |
                 KeyManagementException | UnrecoverableKeyException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    public void start() throws IOException {
        MatchmakingStrategy simpleMatchmakingPool = new SimpleMatchmaking();
        MatchmakingStrategy rankedMatchmakingPool = new RankedMatchmaking();

        Thread simpleThread = Thread.ofVirtual().start(simpleMatchmakingPool);
        Thread rankedThread = Thread.ofVirtual().start(rankedMatchmakingPool);

        while (!this.serverSocket.isClosed()) {
            SSLSocket clientSocket = (SSLSocket) this.serverSocket.accept();
            ClientSession clientSession = new ClientSession(clientSocket, simpleMatchmakingPool, rankedMatchmakingPool, this);
            Thread clientThread = new Thread(clientSession);
            Thread.ofVirtual().start(clientSession);
        }
    }

    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Exception caught when trying to close the server socket");
            System.out.println(e.getMessage());
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}