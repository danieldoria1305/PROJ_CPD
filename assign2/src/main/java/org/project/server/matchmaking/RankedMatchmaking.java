package org.project.server.matchmaking;

import org.project.server.User;
import java.util.*;

public class RankedMatchmaking implements MatchmakingStrategy {
    private static final PriorityQueue<User> rankedPlayers = new PriorityQueue<>(Comparator.comparingInt(User::getScore));
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
            rankedPlayers.offer(user);
        }
    }

    public static User findUserByUsername(String username) {
        synchronized (lock) {
            for (User user : rankedPlayers) {
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
        }
        return null;
    }

@Override
public void handleMatches() {

    if (rankedPlayers.size() >= 2) {
        List<User> matchedPlayers = new ArrayList<>();
        int maxDifference = 5; // Initial score difference
        long waitTime = 0; // Time before increasing the threshold
        long lastThresholdIncreaseTime = System.currentTimeMillis(); // Time when the threshold was last increased
        long maxWaitTime = 120000; // Maximum wait time before giving up

        while (matchedPlayers.size() < 2 && !rankedPlayers.isEmpty() && System.currentTimeMillis() - lastThresholdIncreaseTime < maxWaitTime) {
            User player = rankedPlayers.poll();

            if (matchedPlayers.isEmpty() || Math.abs(player.getScore() - matchedPlayers.get(0).getScore()) <= maxDifference) {
                matchedPlayers.add(player);
            } else {
                rankedPlayers.offer(player);

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastThresholdIncreaseTime >= waitTime) {
                    maxDifference += 5;
                    lastThresholdIncreaseTime = currentTime;
                    waitTime += 5000;
                }
            }
        }

        if (matchedPlayers.size() == 2) {
            makeMatch(matchedPlayers.get(0), matchedPlayers.get(1));
        } else {
            rankedPlayers.addAll(matchedPlayers);
        }
    }

}

    private void checkAndRemoveOfflineClients() {
        synchronized (lock) {
            rankedPlayers.removeIf(user -> !user.isOnline());
        }
    }

    @Override
    public void removeClient(User user) {
        synchronized (lock) {
            rankedPlayers.remove(user);
        }
    }
}