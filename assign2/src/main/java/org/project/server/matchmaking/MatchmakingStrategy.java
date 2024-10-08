package org.project.server.matchmaking;

import org.project.server.ClientSession;
import org.project.server.Game;
import org.project.server.User;
import org.project.server.UserStateEnum;
import java.io.IOException;
import java.util.UUID;

public interface MatchmakingStrategy extends Runnable {
    void addClient(User user);
    void handleMatches();
    void removeClient(User user);

    default void makeMatch(User user1, User user2) {
        removeClient(user1);
        removeClient(user2);

        user1.setState(UserStateEnum.IN_GAME);
        user2.setState(UserStateEnum.IN_GAME);

        UUID gameId = UUID.randomUUID();

        Game game = null;
        try {
            game = new Game(gameId, user1.getClientSession(), user2.getClientSession());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        synchronized (ClientSession.games) {
            ClientSession.games.put(gameId, game);
        }

        user1.getClientSession().setGameId(gameId);
        user2.getClientSession().setGameId(gameId);
    }
}
