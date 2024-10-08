package org.project.server.matchmaking;

import org.project.server.User;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class SimpleMatchmaking implements MatchmakingStrategy {
    private static final List<User> simplePlayers = new ArrayList<>();
    private static final Object lock = new Object();

    @Override
    public void run() {
        while (true) {
            checkAndRemoveOfflineClients();
            synchronized (lock) {
                handleMatches();
            }
        }
    }

    @Override
    public void addClient(User user) {
        synchronized (lock) {
            simplePlayers.add(user);
        }
    }

    public static User findUserByUsername(String username) {
        synchronized (lock) {
            for (User user : simplePlayers) {
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
        }
        return null;
    }

    @Override
    public void handleMatches() {
        synchronized (lock) {
            if (simplePlayers.size() >= 2) {
                User user1 = null;
                User user2 = null;

                for (User user : simplePlayers) {
                    if (user.isOnline()) {
                        if (user1 == null) {
                            user1 = user;
                        } else if (user2 == null) {
                            user2 = user;
                        }

                        if (user1 != null && user2 != null) {
                            break;
                        }
                    }
                }

                if (user1 != null && user2 != null) {
                    makeMatch(user1, user2);
                }
            }
        }
    }

    private void checkAndRemoveOfflineClients() {
        synchronized (lock) {
            Iterator<User> iterator = simplePlayers.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                if (!user.isOnline() &&
                        Duration.between(user.getLastOnline(), LocalDateTime.now()).toSeconds() >= 60
                ) {
                    iterator.remove();
                    System.out.println("Client " + user.getUsername() + " removed from matchmaking pool due to inactivity.");
                }
            }
        }
    }

    @Override
    public void removeClient(User user) {
        synchronized (lock) {
            simplePlayers.remove(user);
        }
    }
}