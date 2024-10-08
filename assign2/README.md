# 2nd CPD Project
# Online Game Server

This is a client-server system for an online text-based Odds and Evens game, developed as part of a Distributed Systems assignment. The server supports user authentication, matchmaking, and game hosting, while the clients can register, authenticate, and participate in games.

## Prerequisites

- Java 21 or later
- Gradle

## Building the Project

1. Clone the repository:
   ```bash
   git clone git@git.fe.up.pt:cpd/2324/t10/g11.git
   cd g11/assign2
   ``` 

2. Build and run the project using Gradle. For that you can use the IntelliJ IDEA IDE or run the following command:
   ```bash
   ./gradlew --console plain run
   ```
3. To login in any of the available accounts you can use the password 1234 in any of the following accounts:
   - Username: joao
   - Username: luis
   - Username: ines
   - Username: joaquim

## Features

- User registration and authentication
- Simple matchmaking mode (first 2 users form a game)
- Ranked matchmaking mode (users are matched based on their pontuation levels)
- Fault tolerance for broken connections during matchmaking
- Concurrent processing using Java Virtual Threads
- Thread-safe data structures using `java.util.concurrent.locks`

## Contributors

- Adriano Machado: **up202105352@up.pt**    
- Daniel Dória: **up202108808@up.pt**
- André Rodrigues: **up202108721@up.pt**
