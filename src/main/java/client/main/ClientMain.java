package client.main;

import client.game.ClientGame;

public class ClientMain {

    public static void main(String[] args) {
        ClientGame tanks = new ClientGame();
        tanks.start();
    }
}
