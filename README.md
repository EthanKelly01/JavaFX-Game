# PongWin
 A basic JavaFX game about playing Pong with windows.
 
 By:
-  E. Kelly (100789657)
-  J. Shi (100618496)
  
---
Using JavaFX, we've created an application that allows you to connect to an online opponent as either a client or host, using multithreaded networking.

<img align="right" src="https://github.com/EthanKelly01/JavaFX-Game/blob/main/assets/MainMenu.png">

As a host, you create your own room at your IP address and a port of your choice where players can come challenge you. Multithreading allows multiple clients to connect at a time and be placed in a waiting list to battle you.

As a client, you can connect to a host of your choice by inputting the correct IP address and port. Don't worry if there's already another client connected, you'll be put into a queue to wait your turn!

The game is quite simple, it's Pong! But instead of controlling a paddle in the window, you control a separate window on your side of the screen. You move your window around the screen to block balls coming your way and send them back at the opponent.

The twist here is that you can't see anything between the no-mans-land and your paddle, meaning you have to track the ball and guess where its going.

<img src="https://github.com/EthanKelly01/JavaFX-Game/blob/main/assets/unknown.png">

---
`main.java` controls the main menu of the game and the submenus to start either a host or client connection. It then calls the `Game` object from `Game.java` upon request, starting the game itself.

The `Game` class represents an instance of the game itself, and contains both a client and server object. It will instantiate whichever one is needed at the time. `Game` contains a `run()` method that then starts the game loop. Depending on whether you are the client or host, the game will display differently.

`Client` represents the client socket connection and allows messages to be sent to the host. This is used to pass back and forth the positions of objects in the game, such as the player's windows and the ball.

Lastly, the `Controller` represents a server and is started through a new thread. This thread then listens for incoming connection requests and spawns a new thread for each one using the nested `Player` class. Players are put into a queue so only one at a time is capable of updating the main game loop.

### How to Run

In order to run, first ensure you have Java and Gradle up to date. Then clone the repo (or download and unzip the included submission.zip), navigate to the project directory in your system, and enter `gradle run`
