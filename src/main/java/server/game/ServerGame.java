package server.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.IO.Input;
import server.IO.RenderObject;
import server.display.Display;
import server.game.level.Level;
import server.graphics.TextureAtlas;
import server.utils.Time;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class ServerGame implements Runnable {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Tanks";
    public static final int CLEAR_COLOR = 0xff000000;
    public static final int NUM_BUFFERS = 3;


    public static final float UPDATE_RATE = 60.0f; //сколько раз в секунду идет просчет физики, ура танчики едут
    public static final float UPDATE_INTERVAL = Time.SECOND / UPDATE_RATE; //храним время между апдейтами
    public static final long IDLE_TIME = 1; //ожидание для threada время(млсек)

    private static final Logger LOG = LoggerFactory.getLogger(ServerGame.class);

    private static Map<EntityType, List<Bullet>> bullets;

    private static List<Bullet> serverPlayerBullets = null;
    private static List<Bullet> clientPlayerBullets = null;

    private boolean running; //флаг запущена ли игра
    private Thread gameThread;
    private final Graphics2D graphics;
    private final Input input;
    private final Player serverPlayer;
    private final Player clientPlayer;
    private final Level lvl;

    private volatile Server srv;


    public static final String ATLAS_FILE_NAME = "Battle City JPN.png";
    private static boolean gameOver;
    //не нужно пока что
    public static final int BUF_SIZE = 1024;
//    private ServerSocketChannel serverSockCh;
//    private Selector sel;
//    private ByteBuffer buffer;

    public ServerGame() {
        running = false;
        Display.created(WIDTH, HEIGHT, TITLE, CLEAR_COLOR, NUM_BUFFERS);
        graphics = Display.getGraphics();
        input = new Input();
        Display.addInputListener(input);
        //сопстно передаем координаты чтобы порезать картинку
        bullets = new HashMap<>();
        serverPlayerBullets = new LinkedList<Bullet>();
        clientPlayerBullets = new LinkedList<Bullet>();
        bullets.put(EntityType.Player, new LinkedList<>());
        lvl = new Level();
        clientPlayer = new Player("clientPlayer", 20, 20, 2, 3, Level.atlas, lvl);
        serverPlayer = new Player("serverPlayer", 300, 300, 2, 3, Level.atlas, lvl);

    }

    public synchronized void start() {
        //старт игры вызыватся только одним потоком
        if (running)
            return;
        running = true;

        gameThread = new Thread(this);

        gameThread.start();

        srv = new Server();
        srv.init();

        srv.start();

    }

    public static void setGameOver() {
        gameOver = true;
    }

    private class Server extends Thread {
        ServerSocket serverSocket;
        Socket sock;
        ObjectInputStream oin;
        ObjectOutputStream oout;

        private void init() {
            try {
                serverSocket = new ServerSocket(12345);

                sock = serverSocket.accept();

                oout = new ObjectOutputStream(sock.getOutputStream());
                oin = new ObjectInputStream(sock.getInputStream());
            } catch (IOException e) {
                LOG.error("Error establishing connection");

                throw new IllegalStateException("Error establishing connection", e);
            }
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Command cmd = (Command) oin.readObject(); // control command

                    clientPlayer.update(cmd.direction, cmd.isSpace);
                }
            } catch (Exception e) {
                LOG.error("Error reading command from client", e);
            }
        }

        private void sendState() {
            try {
                oout.writeObject(new RenderObject(serverPlayer, clientPlayer, lvl, Collections.emptyList(), Collections.emptyList()));
            } catch (Exception e) {
                LOG.error("Error sending state to client", e);
            }
        }
    }

    public synchronized void stop() {
        //конец игры
        if (!running)
            return;
        running = false;

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            //добавить логирование нафига только?

            e.printStackTrace();
        }
        cleanup();
        srv.interrupt();
    }

    private void update() {
        //физика игры
        serverPlayer.update(input);
//        clientPlayer.update(direction, isSpace);
//        lvl.update();

        for (int i = 0; i < bullets.get(EntityType.Player).size(); i++)
            bullets.get(EntityType.Player).get(i).update();
        // send to all clients: write to ObjectOutputStream
    }

    private void render() {
        assert srv != null;

        srv.sendState();
        //прорисовка сцен
        Display.clear();
        lvl.render(graphics);

        if (serverPlayer != null) {
            if (!serverPlayer.isAlive()) {
                serverPlayer.drawExplosion(graphics);
            } else
                serverPlayer.render(graphics);
        }
//        serverPlayer.render(graphics);

        clientPlayer.render(graphics);
        for (Bullet bullet : getBullets(EntityType.Player))
            bullet.render(graphics);
        lvl.renderGrass(graphics);

        Display.swapBuffers();
    }

    @Override
    public void run() {
        //синхронизированный код
        //ядро, луп
        int fps = 0;
        int upd = 0;
        int updl = 0;

        long count = 0;


        float delta = 0;

        long lasttime = Time.get(); //прошлое время
        while (running) {
            long now = Time.get(); //тек время
            //считаем сколько времени прошло с последнего запуска кода
            long elapsedTime = now - lasttime;
            lasttime = now;
            count += elapsedTime;

            boolean render = false;
            //кол-во раз сколько должна бежать функция
            delta += (elapsedTime / UPDATE_INTERVAL); //каждая 1 означает что нужно сделать update
            while (delta > 1) {
                update();
                upd++;
                delta--;
                if (render) {
                    updl++;
                } else {
                    render = true;
                }
            }
            if (render) {

//                if (srv.oout != null) {
////пример                    srv.oout.writeObject(); // lvl, players
//                    try {
//                        System.out.println("111");
//                        srv.oout.writeObject(serverPlayer);
//                        srv.oout.writeObject(clientPlayer);
////пока без уровня       srv.oout.writeObject(lvl);
//
//                        srv.oout.flush();
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
                //если что-то изменили перерисовываем сцену
                render();
                fps++;
            } else {
                //стопим тред
                try {
                    Thread.sleep(IDLE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (count >= Time.SECOND) {
                Display.setTitle(TITLE + " || fps:" + fps + " | Upd:" + upd + " | Updl:" + updl);
                upd = 0;
                updl = 0;
                fps = 0;
                count = 0;
            }
        }

    }

    private void cleanup() {
        //удаляем окно
        Display.destroy();
    }

    public static void registerBullet(EntityType type, Bullet bullet) {
        bullets.get(type).add(bullet);
    }

    public static void registerBulletinList(String playerName, Bullet bullet) {
        if (playerName == "serverPlayer") {
            serverPlayerBullets.add(bullet);
        } else if (playerName == "clientPlayer") {
            clientPlayerBullets.add(bullet);
        }

    }

    public static void unregisterBullet(EntityType type, Bullet bullet) {
        if (bullets.get(type).size() > 0) {
            bullets.get(type).remove(bullet);
        }
    }

    public static void unregisterBulletList(String playerName, Bullet bullet) {
        if (playerName == "serverPlayer") {
            if (serverPlayerBullets.size() > 0) {
                serverPlayerBullets.remove(bullet);
            }
        } else if (playerName == "clientPlayer") {
            if (clientPlayerBullets.size() > 0) {
                clientPlayerBullets.remove(bullet);
            }
        }
    }

    public static List<Bullet> getBullets(EntityType type) {
        return bullets.get(type);
    }

    public static List<Bullet> getBullets(String playerName) {
        if (playerName == "serverPlayer") {
            return serverPlayerBullets;
        } else if (playerName == "clientPlayer") {
            return clientPlayerBullets;
        }
        return null;
    }
}