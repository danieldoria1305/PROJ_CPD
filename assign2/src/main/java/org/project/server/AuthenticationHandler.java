package org.project.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.project.database.DatabaseManager;

public class AuthenticationHandler {
    private enum AuthState {
        INITIAL_STATE,
        AWAITING_TOKEN,
        AWAITING_REGISTER_USERNAME,
        AWAITING_REGISTER_PASSWORD,
        AWAITING_LOGIN_USERNAME,
        AWAITING_LOGIN_PASSWORD
    }

    private AuthState state;
    private String username;
    private String token;
    private DatabaseManager databaseManager;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock writeLock = lock.writeLock();
    private static Set<String> authenticatedUsers = new HashSet<>();
    private AuthResult authResult;

    public AuthenticationHandler(DatabaseManager databaseManager) {
        this.state = AuthState.INITIAL_STATE;
        this.databaseManager = databaseManager;
        this.authResult = new AuthResult();
    }

    public AuthResult handleInput(String input, ClientSession clientSession) throws IOException {
        String[] inputParts = input.split(",");
        System.out.println("inputParts: " + Arrays.toString(inputParts));

        switch (this.state) {
            case INITIAL_STATE:
                if (inputParts[0].equals("TOKEN")){
                    this.state = AuthState.AWAITING_TOKEN;
                }
                else if(inputParts[0].equals("AUTH_REGISTER")){
                    this.state = AuthState.AWAITING_REGISTER_USERNAME;
                    clientSession.write("REQUEST_USERNAME\n");

                    this.authResult.setParameters(false, false);
                    return this.authResult;
                }

            case AWAITING_TOKEN:
                if (inputParts[0].equals("TOKEN") && inputParts.length == 2) {
                    this.token = inputParts[1];
                    if (databaseManager.verifyToken(token, clientSession)) {
                        clientSession.write("AUTHENTICATED," + token + "\n");
                        successfulAuthentication(clientSession);

                        this.authResult.setParameters(true, true);
                        return this.authResult;
                    } else {
                        clientSession.write("REQUEST_USERNAME\n");
                        this.state = AuthState.AWAITING_LOGIN_USERNAME;

                        this.authResult.setParameters(false, false);
                        return this.authResult;
                    }
                }

                this.authResult.setParameters(false, false);
                return this.authResult;

            case AWAITING_LOGIN_USERNAME:
                if (inputParts[0].equals("USERNAME") && inputParts.length == 2) {
                    this.username = inputParts[1];
                    if(userIsAuthenticated(this.username)){
                        clientSession.write("ALREADY_AUTHENTICATED\n");

                        this.authResult.setParameters(false, false);
                        return this.authResult;
                    }
                    if (databaseManager.verifyUsername(username)) {
                        this.state = AuthState.AWAITING_LOGIN_PASSWORD;

                        clientSession.write("REQUEST_PASSWORD\n");
                    } else {
                        clientSession.write("REQUEST_USERNAME\n");
                    }
                }

                this.authResult.setParameters(false, false);
                return this.authResult;

            case AWAITING_LOGIN_PASSWORD:
                if (inputParts[0].equals("PASSWORD") && inputParts.length == 2) {
                    String newToken = UUID.randomUUID().toString();
                    if (databaseManager.verifyPassword(this.username, inputParts[1], clientSession.getUser(), newToken)) {
                        this.token = newToken;
                        clientSession.write("AUTHENTICATED," + token + "\n");
                        successfulAuthentication(clientSession);

                        this.authResult.setParameters(true, false);
                        return this.authResult;
                    } else {
                        clientSession.write("REQUEST_PASSWORD\n");
                    }
                }

                this.authResult.setParameters(false, false);
                return this.authResult;

            case AWAITING_REGISTER_USERNAME:
                if (inputParts[0].equals("USERNAME") && inputParts.length == 2) {
                    this.username = inputParts[1];
                    if (!databaseManager.verifyUsername(username)) {
                        this.state = AuthState.AWAITING_REGISTER_PASSWORD;
                        clientSession.write("REQUEST_PASSWORD\n");
                    } else {
                        clientSession.write("REQUEST_USERNAME\n");
                    }
                }

                this.authResult.setParameters(false, false);
                return this.authResult;

            case AWAITING_REGISTER_PASSWORD:
                if (inputParts[0].equals("PASSWORD") && inputParts.length == 2) {
                    String newToken = UUID.randomUUID().toString();
                    if (databaseManager.register(this.username, inputParts[1], clientSession.getUser(), newToken)) {
                        this.token = newToken;
                        clientSession.write("AUTHENTICATED," + token + "\n");
                        successfulAuthentication(clientSession);

                        this.authResult.setParameters(true, false);
                        return this.authResult;
                    } else {
                        clientSession.write("REQUEST_USERNAME\n");
                    }
                }

                this.authResult.setParameters(false, false);
                return this.authResult;

            default:
                this.authResult.setParameters(false, false);
                return this.authResult;
        }
    }

    public void successfulAuthentication(ClientSession clientSession) {
        String username = clientSession.getUser().getUsername();
        writeLock.lock();
        try {
            authenticatedUsers.add(username);
        } finally {
            writeLock.unlock();
        }
    }

    public static boolean userIsAuthenticated(String username){
        readLock.lock();
        try {
            return authenticatedUsers.contains(username);
        } finally {
            readLock.unlock();
        }
    }

    public static void removeAuthenticatedUser(String username){
        writeLock.lock();
        try {
            authenticatedUsers.remove(username);
        } finally {
            writeLock.unlock();
        }
    }


}