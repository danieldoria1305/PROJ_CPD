package org.project.database;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.*;
import org.mindrot.jbcrypt.BCrypt;
import org.project.server.AuthenticationHandler;
import org.project.server.ClientSession;
import org.project.server.User;
import org.project.server.matchmaking.RankedMatchmaking;
import org.project.server.matchmaking.SimpleMatchmaking;
import java.time.LocalDateTime;

public class DatabaseManager {
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock writeLock = lock.writeLock();
    private static final String DATABASE_FILE = "src/main/java/org/project/database/database.csv";
    public boolean register(String username, String password, User user, String newToken) throws IOException {
        if (verifyUsername(username)) {
            return false;
        }

        String salt = BCrypt.gensalt();
        String encryptedPassword = BCrypt.hashpw(password, salt);
        LocalDateTime localDateTime = LocalDateTime.now();

        writeLock.lock();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(DATABASE_FILE), StandardOpenOption.APPEND)) {
            writer.write(username + "," + 0 + "," + encryptedPassword + "," + salt+ "," + newToken + "," + localDateTime + "\n");
        } finally {
            writeLock.unlock();
        }
        user.populate(username, 0, newToken,  localDateTime);
        return true;
    }

    public boolean verifyUsername(String username) {
        readLock.lock();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(DATABASE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            System.out.println("The database file was not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("There was an issue reading the database file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            readLock.unlock();
        }
        return false;
    }

    public boolean verifyPassword(String username, String password, User user, String newToken) {
        readLock.lock();
        List<String> fileContent = new ArrayList<>();
        Boolean verified = false;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(DATABASE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    if(BCrypt.checkpw(password, parts[2])){
                        parts[4] = newToken;
                        parts[5] = LocalDateTime.now().toString();
                        user.populate(parts[0], Integer.parseInt(parts[1]), parts[4], LocalDateTime.now());
                        line = String.join(",", parts);
                        verified = true;
                    }
                }
                fileContent.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("The database file was not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("There was an issue reading the database file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }

        if (verified) {
            writeLock.lock();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(DATABASE_FILE))) {
                for (String fileLine : fileContent) {
                    writer.write(fileLine);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("There was an issue writing to the database file: " + e.getMessage());
                e.printStackTrace();
            } finally {
                writeLock.unlock();
            }
        }

        return verified;
    }

    public void updateClient(String username, int score, LocalDateTime lastOnline) {
        writeLock.lock();
        try {
            List<String> fileContent = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(DATABASE_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(username)) {
                        parts[1] = Integer.toString(score);
                        parts[5] = lastOnline.toString();
                        line = String.join(",", parts);
                    }
                    fileContent.add(line);
                }
            }
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(DATABASE_FILE))) {
                for (String fileLine : fileContent) {
                    writer.write(fileLine);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("There was an issue updating the client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    public boolean verifyToken(String token, ClientSession clientSession) {
        readLock.lock();
        try {
            if(token.equals("null")){
                return false;
            }

            try (BufferedReader reader = Files.newBufferedReader(Paths.get(DATABASE_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[4].equals(token)) {
                        if(AuthenticationHandler.userIsAuthenticated(parts[0])){
                            clientSession.write("ALREADY_AUTHENTICATED\n");
                            return false;
                        }

                        if (Duration.between(LocalDateTime.parse(parts[5]), LocalDateTime.now()).toSeconds() >= 60) {
                            return false;
                        }

                        User userRanked = RankedMatchmaking.findUserByUsername(parts[0]);
                        User userSimple = SimpleMatchmaking.findUserByUsername(parts[0]);
                        if(userRanked != null){
                            clientSession.setUser(userRanked);
                            clientSession.getUser().goOnline();
                            clientSession.getUser().setClientSession(clientSession);
                            return true;
                        }
                        else if(userSimple != null){
                            clientSession.setUser(userSimple);
                            clientSession.getUser().goOnline();
                            clientSession.getUser().setClientSession(clientSession);
                            return true;
                        }

                        return false;
                    }
                }
                return false;
            } catch (IOException e) {
                System.out.println("There was an issue reading the database file: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            readLock.unlock();
        }

        return false;
    }
}