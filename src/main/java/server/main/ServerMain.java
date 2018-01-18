package server.main;

import server.game.ServerGame;

public class ServerMain {

    public static void main(String[] args) {
        ServerGame tanks = new ServerGame();
        tanks.start();


    }
}
