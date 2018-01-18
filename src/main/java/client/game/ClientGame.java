package client.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.IO.Input;
import server.IO.RenderObject;
import server.display.Display;
import server.game.*;
import server.game.level.Level;
import server.graphics.TextureAtlas;
import server.utils.Time;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ClientGame implements Runnable {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Tanks";
    public static final int CLEAR_COLOR = 0xff000000;
    public static final int NUM_BUFFERS = 3;


    public static final float UPDATE_RATE = 60.0f; //сколько раз в секунду идет просчет физики, ура танчики едут
    public static final float UPDATE_INTERVAL = Time.SECOND / UPDATE_RATE; //храним время между апдейтами
    public static final long IDLE_TIME = 1; //ожидание для threada время(млсек)

    private static final Logger LOG = LoggerFactory.getLogger(ClientGame.class);

    private volatile List<Bullet> serverPlayerBullets;
    private volatile List<Bullet> clientPlayerBullets;

    private volatile boolean running; //флаг запущена ли игра
    private volatile Thread gameThread;
    private final Graphics2D graphics;
    private final Input input;
    private volatile Player serverPlayer;
    private volatile Player clientPlayer;
    private volatile Level lvl;

    private volatile Client client;

    public static final String ATLAS_FILE_NAME = "Battle City JPN.png";

    public ClientGame() {
        running = false;
        Display.created(WIDTH, HEIGHT, TITLE, CLEAR_COLOR, NUM_BUFFERS);
        graphics = Display.getGraphics();
        input = new Input();
        Display.addInputListener(input);
        //сопстно передаем координаты чтобы порезать картинку
//        serverPlayer = new server.game.Player(300, 300, 2, 3, atlas, lvl);
//        clientPlayer = new server.game.Player(300, 20, 2, 3, atlas, lvl);
        lvl = new Level();
    }

    public synchronized void start() {
        //старт игры вызыватся только одним потоком
        if (running)
            return;
        running = true;

        gameThread = new Thread(this);

        client = new Client();

        client.init();

        gameThread.start();
        client.start();
    }

    protected class Client extends Thread {
        Socket sock;
        ObjectInputStream oin;
        ObjectOutputStream oout;
        final AtomicReference<RenderObject> rndObject = new AtomicReference<>();

        private void init() {
            InetSocketAddress addr = new InetSocketAddress("localhost", 12345);

            try {
                sock = new Socket();

                sock.connect(addr);

                LOG.info("Connected to {}", addr);

                oout = new ObjectOutputStream(sock.getOutputStream());
                oin = new ObjectInputStream(sock.getInputStream());
            } catch (IOException e) {
                LOG.error("Error establishing connection", e);

                throw new IllegalStateException("Connection was not established addr=" + addr, e);
            }
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    RenderObject rnd = (RenderObject) oin.readObject();

                    if (LOG.isDebugEnabled())
                        LOG.debug("Got render object {}", rnd);

                    rndObject.set(rnd);
                }
            } catch (IOException | ClassNotFoundException e) {
                LOG.error("Error on communicating", e);
            }
        }

        private void sendUpdate(Command cmd) {
            try {
                oout.writeObject(cmd);
            } catch (IOException e) {
                LOG.error("Error sending command: " + cmd, e);
            }
        }

        private RenderObject getRenderObject() {
            return rndObject.getAndSet(null);
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
    }

    private void update() {
        //физика игры

        Command cmd = null;

        if (input.getKey(KeyEvent.VK_UP)) {
            cmd = new Command(Heading.NORTH, false);
        }
        else if (input.getKey(KeyEvent.VK_RIGHT)) {
            cmd = new Command(Heading.EAST, false);
        }
        else if (input.getKey(KeyEvent.VK_DOWN)) {
            cmd = new Command(Heading.SOUTH, false);
        }
        else if (input.getKey(KeyEvent.VK_LEFT)) {
            cmd = new Command(Heading.WEST, false);
        }

        if (cmd != null) {
            assert client != null;

            client.sendUpdate(cmd);
        }

        // send to all clients: write to ObjectOutputStream
    }

    private void render() {
        //прорисовка сцен
        Display.clear();
        lvl.render(graphics);
        serverPlayer.render(graphics);
        clientPlayer.render(graphics);
//        for (Bullet bullet : getBullets(EntityType.Player))
//            bullet.render(graphics);
        lvl.renderGrass(graphics);

        Display.swapBuffers();

        //todo bullets render
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

            RenderObject rnd = client.getRenderObject();

            if (rnd != null) {
                assert client != null;


                    serverPlayer = rnd.serverPlayer;
                    clientPlayer = rnd.clientPlayer;
                    lvl = rnd.level;
                    serverPlayerBullets = rnd.serverPlayerBullets;
                    clientPlayerBullets = rnd.clientPlayerBullets;

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

//    public void registerBullet(EntityType type, Bullet bullet) {
//        bullets.get(type).add(bullet);
//    }
//
//    public void unregisterBullet(EntityType type, Bullet bullet) {
//        if (bullets.get(type).size() > 0) {
//            bullets.get(type).remove(bullet);
//        }
//    }
//
//    public List<Bullet> getBullets(EntityType type) {
//        return bullets.get(type);
//    }
}
