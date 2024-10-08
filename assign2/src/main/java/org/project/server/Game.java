package org.project.server;

import java.io.IOException;
import java.util.UUID;
import java.util.Random;
public class Game {
    private enum GameState {
        PARITY_SELECTION,
        GUESS_SELECTION
    }
    private UUID gameId;
    private ClientSession startingClient;
    private ClientSession nonStartingClient;
    private String parity;
    private Integer number1, number2;
    private GameState state;
    public Game(UUID gameId, ClientSession client1, ClientSession client2) throws IOException {
        this.gameId = gameId;
        this.state = GameState.PARITY_SELECTION;
        this.number1 = -1;
        this.number2 = -1;

        Random random = new Random();
        ClientSession startingClient = random.nextBoolean() ? client1 : client2;
        ClientSession nonStartingClient = startingClient == client1 ? client2 : client1;

        this.startingClient = startingClient;
        this.nonStartingClient = nonStartingClient;

        startingClient.write(
                "-----------------------------------------------\n" +
                "|                GAME STARTED                 |\n" +
                "|---------------------------------------------|\n" +
                "|  You'll be the player choosing the          |\n" +
                "|  parity for this game.                      |\n" +
                "|---------------------------------------------|\n" +
                "|  Please choose if you either want to        |\n" +
                "|  play as Even ['even'] or as Odd ['odd'].   |\n" +
                "-----------------------------------------------\n");

        nonStartingClient.write(
                    "-----------------------------------------------\n" +
                        "|                GAME STARTED                 |\n" +
                        "|---------------------------------------------|\n" +
                        "|  Please wait while the other player         |\n" +
                        "|  chooses its parity for this game.          |\n" +
                        "-----------------------------------------------\n"
        );
    }

    public void update(ClientSession client, String input) throws IOException {
        switch (state) {
            case PARITY_SELECTION:
                if(client == startingClient){
                    if (input.equals("odd") || input.equals("even")) {
                        parity = input;
                        nonStartingClient.write(
                                "\n-----------------------------------------------\n" +
                                "   Opponent has chosen " + input + ".          \n" +
                                "|---------------------------------------------|\n" +
                                "|  Please type which number you want to use.  |\n" +
                                "-----------------------------------------------\n");

                        state = GameState.GUESS_SELECTION;
                    } else {
                        client.write(
                                "\n-----------------------------------------------\n" +
                                    "| Invalid input. Please type 'even' or 'odd'. |\n" +
                                     "-----------------------------------------------\n");
                    }}
                break;
            case GUESS_SELECTION:
                try {
                    int guess = Integer.parseInt(input);
                    if (client == startingClient) {
                        number1 = guess;
                        nonStartingClient.write(
                                "\n-----------------------------------------------\n" +
                                "|   The other player made his guess.          |\n" +
                                "|   Please make yours.                        |\n" +
                                "-----------------------------------------------\n");
                    } else {
                        number2 = guess;
                        startingClient.write(
                                "\n-----------------------------------------------\n" +
                                "|   The other player made his guess.          |\n" +
                                "|   Please make yours.                        |\n" +
                                "-----------------------------------------------\n");
                    }
                    if (number1 != -1 && number2 != -1) {
                        int sum = number1 + number2;
                        String winner = sum % 2 == 0 ? "even" : "odd";
                        ClientSession winningClient, losingClient;

                        if ((winner.equals("even") && parity.equals("even")) || (winner.equals("odd") && parity.equals("odd"))) {
                            winningClient = startingClient;
                            losingClient = nonStartingClient;
                        } else {
                            winningClient = nonStartingClient;
                            losingClient = startingClient;
                        }

                        winningClient.write(
                                "\n-----------------------------------------------\n" +
                                "|        Congratulations, you've Won!         |\n" +
                                "|---------------------------------------------|\n" +
                                "   The final result is " + sum + " which is " + winner + ".\n" +
                                "-----------------------------------------------\n");


                        losingClient.write(
                                "\n-----------------------------------------------\n" +
                                "|          Bad Luck... you've Lost!           |\n" +
                                "|---------------------------------------------|\n" +
                                "   The final result is " + sum + " which is " + winner + ".\n" +
                                "-----------------------------------------------\n");

                        losingClient.changeState(UserStateEnum.GAME_OVER);
                        winningClient.changeState(UserStateEnum.GAME_OVER);

                       String message = (
                                        "-----------------------------------------------\n" +
                                        "|                  GAME OVER                  |\n" +
                                        "|---------------------------------------------|\n" +
                                        "|  Thanks for playing! Press to continue.     |\n" +
                                        "-----------------------------------------------\n");

                       winningClient.write(message);
                       losingClient.write(message);

                        ClientSession.games.remove(gameId);
                        winningClient.setGameId(null);
                        winningClient.setGameId(null);

                        winningClient.getUser().incrementScore();
                        losingClient.getUser().decrementScore();

                    }
                } catch (NumberFormatException e) {
                    client.write(
                            "\n-----------------------------------------------\n" +
                            "|  Invalid input. Please enter a number.      |\n" +
                            "-----------------------------------------------\n");
                }
                break;
        }
    }
}

