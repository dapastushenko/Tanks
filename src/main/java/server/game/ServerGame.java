package server.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.IO.Input;
import server.IO.RenderObject;
import server.display.Display;
import server.game.level.Level;
import server.graphics.TextureAtlas;
import server.utils.Time;
import server.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

import static server.game.PlayerSide.CLIENT;
import static server.game.PlayerSide.SERVER;

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

    private static List<Bullet> serverPlayerBullets = Collections.emptyList();
    private static List<Bullet> clientPlayerBullets = Collections.emptyList();

    private boolean running; //флаг запущена ли игра
    private Thread gameThread;
    private final Graphics2D graphics;
    private final Input input;
    private final Player serverPlayer;
    private final Player clientPlayer;
    private final Level lvl;
    private BufferedImage gameOverImage;
    private static TextureAtlas atlas;
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
        atlas = new TextureAtlas(ATLAS_FILE_NAME);
        Display.addInputListener(input);
        bullets = new HashMap<>();
        serverPlayerBullets = new LinkedList<Bullet>();
        clientPlayerBullets = new LinkedList<Bullet>();
        bullets.put(EntityType.Player, new LinkedList<>());
        lvl = new Level();
        gameOver = false;
        clientPlayer = new Player(CLIENT, 300, 20, 2, 3, lvl);
        serverPlayer = new Player(SERVER, 300, 300, 2, 3, lvl);

        gameOverImage = Utils.resize(
                atlas.cut(36 * Level.TILE_SCALE, 23 * Level.TILE_SCALE, 4 * Level.TILE_SCALE, 2 * Level.TILE_SCALE),
                4 * Level.SCALED_TILES_SIZE, 2 * Level.SCALED_TILES_SIZE);
        for (int i = 0; i < gameOverImage.getHeight(); i++)
            for (int j = 0; j < gameOverImage.getWidth(); j++) {
                int pixel = gameOverImage.getRGB(j, i);
                if ((pixel & 0x00FFFFFF) < 10)
                    gameOverImage.setRGB(j, i, (pixel & 0x00FFFFFF));
            }
    }

    public synchronized void start() {
        //старт игры вызыватся только одним потоком
        if (running)
            return;
        running = true;

        gameThread = new Thread(this);

        srv = new Server();
        srv.start();

        gameThread.start();
    }

    public static void setGameOver() {
        gameOver = true;
    }

    private class Server extends Thread {
        final ServerSocket serverSocket;
        final Socket sock;
        final ObjectInputStream oin;
        final ObjectOutputStream oout;

        public Server() {
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
                synchronized (serverPlayer) {
                    synchronized (clientPlayer) {
//                        if (serverPlayer.updated() || clientPlayer.updated()) {
                        oout.reset();
if (serverPlayerBullets.size()==0)
    System.out.printf("i have server bullets");
                        oout.writeObject(new RenderObject(serverPlayer, clientPlayer, lvl, serverPlayerBullets, clientPlayerBullets));
//                        }
                    }
                }
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
        clientPlayer.update(null, false);
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
        if (clientPlayer != null) {
            if (!clientPlayer.isAlive()) {
                clientPlayer.drawExplosion(graphics);
            } else
                clientPlayer.render(graphics);
        }
//        serverPlayer.render(graphics);

        clientPlayer.render(graphics);
        for (Bullet bullet : getBullets(EntityType.Player))
            bullet.render(graphics,PlayerSide.SERVER);

        lvl.renderGrass(graphics);
        if (gameOver) {
            graphics.drawImage(gameOverImage, ServerGame.WIDTH / 2 - 2 * Level.SCALED_TILES_SIZE, ServerGame.HEIGHT / 2, null);

        }
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

    public static void registerBulletinList(PlayerSide side, Bullet bullet) {
        if (side == SERVER) {
            serverPlayerBullets.add(bullet);
        } else if (side == CLIENT) {
            clientPlayerBullets.add(bullet);
        }

    }

    public static void unregisterBullet(EntityType type, Bullet bullet, PlayerSide side) {
        if(side==SERVER) {
            if (serverPlayerBullets.size() > 0) {
                serverPlayerBullets.remove(bullet);
            }
            if (bullets.get(type).size() > 0) {
                bullets.get(type).remove(bullet);
            }
        } else if (side == CLIENT) {
            if (clientPlayerBullets.size() > 0) {
                clientPlayerBullets.remove(bullet);
            }
        }
    }

    public static void unregisterBulletList(PlayerSide side, Bullet bullet) {
        if (side == SERVER) {
            if (serverPlayerBullets.size() > 0) {
                serverPlayerBullets.remove(bullet);
            }
        } else if (side == CLIENT) {
            if (clientPlayerBullets.size() > 0) {
                clientPlayerBullets.remove(bullet);
            }
        }
    }

    public static List<Bullet> getBullets(EntityType type) {
        return bullets.get(type);
    }

    public static List<Bullet> getBullets(PlayerSide side) {
        if (side == SERVER) {
            return serverPlayerBullets;
        } else if (side == CLIENT) {
            return clientPlayerBullets;
        }
        return Collections.emptyList();
    }
}
