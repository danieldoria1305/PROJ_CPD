package org.project.server;

public class AuthResult {
    private boolean isAuthenticated;
    private boolean isThroughToken;

    public AuthResult() {
        this.isAuthenticated = false;
        this.isThroughToken = false;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public boolean isThroughToken() {
        return isThroughToken;
    }

    public void setParameters(boolean isAuthenticated, boolean isThroughToken) {
        this.isAuthenticated = isAuthenticated;
        this.isThroughToken = isThroughToken;
    }
}