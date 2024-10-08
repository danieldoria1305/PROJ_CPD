package org.project.server;

import org.project.database.DatabaseManager;
import java.time.LocalDateTime;


public class User {
    private UserStateEnum state;
    private String username;
    private Integer score;
    private String token;
    private LocalDateTime lastOnline;
    private ClientSession clientSession;

    public User(ClientSession clientSession) {
        this.username = null;
        this.score = null;
        this.token = null;
        this.state = UserStateEnum.AUTHENTICATING;
        this.lastOnline = null;
        this.clientSession = clientSession;
    }

    public void populate(String username, int score, String token, LocalDateTime lastOnline) {
        this.username = username;
        this.score = score;
        this.token = token;
        this.lastOnline = lastOnline;
    }

    public String getUsername() {
        return username;
    }
    public LocalDateTime getLastOnline() {return lastOnline;}
    public UserStateEnum getState() {return state;}
    public void setState(UserStateEnum state) {this.state = state;}

    public boolean isOnline() {
        return state != UserStateEnum.OFFLINE;
    }

    public void goOffline(DatabaseManager databaseManager) {
        this.state = UserStateEnum.OFFLINE;
        this.lastOnline = LocalDateTime.now();
        databaseManager.updateClient(username, score, lastOnline);
        AuthenticationHandler.removeAuthenticatedUser(username);
    }

    public void goOnline() {
        this.state = UserStateEnum.WAITING_ROOM;
        this.lastOnline = LocalDateTime.now();
    }
    public ClientSession getClientSession() {
        return clientSession;
    }

    public void setClientSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }
    public void incrementScore() {
        this.score++;
    }
    public void decrementScore() { this.score--;}
    public Integer getScore() {
        return score;
    }

}