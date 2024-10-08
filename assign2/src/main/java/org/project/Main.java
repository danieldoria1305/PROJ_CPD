package org.project;

import org.project.client.Client;
import org.project.server.Server;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n-----------------------------------------------\n" +
                             "|              Select an option:              |\n" +
                             "|---------------------------------------------|\n" +
                             "|   Run Server                           [1]  |\n" +
                             "|   Run Client                           [2]  |\n" +
                             "-----------------------------------------------\n\n");
        System.out.print("Enter your choice (1/2): ");


        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            System.out.println("\n-----------------------------------------------\n" +
                                 "|             Server Initialized!             |\n" +
                                 "-----------------------------------------------\n\n");
            Server server = new Server(8080);
            server.start();
        }
        else if (choice == 2) {
            Client client = new Client("localhost", 8080);
            client.start();
        } else {
            System.out.println("Invalid choice. Exiting.");
        }
    }
}