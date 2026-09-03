# Soccer Stars — Server

> **Course project** — Computer Networks, Ferdowsi University of Mashhad, Fall 2025 (Final)

The matchmaking/lobby server for **Soccer Stars**, a real-time online 2-player game. The
server handles accounts, presence, and matchmaking over TCP; actual gameplay happens
peer-to-peer between clients (see
[Soccer-Stars-Client-Side](https://github.com/Rfarasati/Soccer-Stars-Client-Side)) once a
match is set up.

## In action

![Gameplay](docs/gameplay.png)

A match served by this server: two clients (`negar`, `reza`) matched through the lobby,
handed each other's P2P address via `GAME_START`, and are playing live. Full UI walkthrough
(lobby, invitation) is in the
[client repo](https://github.com/Rfarasati/Soccer-Stars-Client-Side#screenshots).

## Responsibilities

- **Accounts** (`DatabaseService`, SQLite): registration and login, with password hashes
  rather than plaintext, plus per-user games-played/games-won stats.
- **Presence & lobby**: tracks online users and broadcasts status changes
  (`GET_ONLINE_USERS`, `USER_STATUS_UPDATE`).
- **Matchmaking**: game invitations between users (`GAME_INVITE` → notify → accept/reject),
  and on acceptance, starts a `GameSession` and sends both clients each other's P2P address
  (`GAME_START`) so they can connect directly for gameplay.
- **Result reporting**: after a match, clients report the outcome to the server
  (`GAME_RESULT` → `GAME_RESULT_ACK`) so stats and history stay authoritative even though
  gameplay itself is P2P and not server-observed.

One thread per client connection (`ClientHandler`, on a cached thread pool), coordinated
through a `SessionManager`. Messages are JSON, framed with a `type` field
(see `MessageType`).

## Tech stack

Java 21, plain TCP sockets, [Gson](https://github.com/google/gson) for JSON, SQLite
(`sqlite-jdbc`), Maven (multi-module: `server` + `shared`).

## Running it

```bash
cd server
mvn compile exec:java
```

Listens on the port configured in `Main`. `soccerstars.db` (SQLite) is created
automatically on first run in the working directory.

## Project structure

```
server/src/main/java/com/soccerstars/server/
    Main.java                  — entry point, starts GameServer
    network/GameServer.java      — accepts connections, thread pool, periodic cleanup/stats
    network/ClientHandler.java     — per-connection message loop
    service/DatabaseService.java     — SQLite accounts/stats
    service/SessionManager.java        — online users, invitations, active game sessions
    model/                                — User, UserSession, GameInvitation, GameSession
    protocol/                              — Message, Messages, MessageType (client<->server JSON protocol)
shared/src/main/java/com/soccerstars/shared/
    protocol/                              — a second copy of the client<->server protocol,
                                              plus P2PMessages/P2PMessageType for the P2P
                                              gameplay protocol
```
