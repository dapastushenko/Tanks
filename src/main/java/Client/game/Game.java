package Client.game;

import ClientServer.IO.Input;
import ClientServer.display.Display;
import ClientServer.game.level.Level;
import ClientServer.graphics.TextureAtlas;
import ClientServer.utils.Time;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Game implements Runnable {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Tanks";
    public static final int CLEAR_COLOR = 0xff000000;
    public static final int NUM_BUFFERS = 3;


    public static final float UPDATE_RATE = 60.0f; //сколько раз в секунду идет просчет физики, ура танчики едут
    public static final float UPDATE_INTERVAL = Time.SECOND / UPDATE_RATE; //храним время между апдейтами
    public static final long IDLE_TIME = 1; //ожидание для threada время(млсек)
    private static Map<EntityType, List<Bullet>> bullets;

    private List<Bullet> bullets1;
    private List<Bullet> bullets2;

    private boolean running; //флаг запущена ли игра
    private Thread gameThread;
    private Graphics2D graphics;
    private Input input;
    private TextureAtlas atlas;
    private ClientServer.game.Player player;
    private ClientServer.game.Player player2;
    private Level lvl;

    private Client client;
    public ObjectOutputStream oout;

    public static final String ATLAS_FILE_NAME = "Battle City JPN.png";

    //не нужно пока что
    public static final int BUF_SIZE = 1024;
//    private ServerSocketChannel serverSockCh;
//    private Selector sel;
//    private ByteBuffer buffer;

    public Game() {
        running = false;
        Display.created(WIDTH, HEIGHT, TITLE, CLEAR_COLOR, NUM_BUFFERS);
        graphics = Display.getGraphics();
        input = new Input();
        Display.addInputListener(input);
        atlas = new TextureAtlas(ATLAS_FILE_NAME);
        //сопстно передаем координаты чтобы порезать картинку
        bullets = new HashMap<>();
        bullets.put(EntityType.Player, new LinkedList<Bullet>());
        player = new ClientServer.game.Player(300, 300, 2, 3, atlas, lvl);
        player2 = new ClientServer.game.Player(300, 20, 2, 3, atlas, lvl);
        lvl = new Level(atlas);
    }

    public synchronized void start() {
        //старт игры вызыватся только одним потоком
        if (running)
            return;
        running = true;

        gameThread = new Thread(this);

        gameThread.start();

        client = new Client();
        client.start();
    }

    protected class Client extends Thread {
        ServerSocket serverSocket;

        Socket sock;
        //        SocketAddress socketAddress;
        ObjectInputStream oin;
//        ObjectOutputStream oout;

        @Override
        public void run() {


            try {
//                serverSocket = new ServerSocket(12345);
//                sock = serverSocket.accept();
                sock = new Socket();

                sock.connect(new InetSocketAddress("localhost", 12345));
                System.out.printf("ggg");
                oout = new ObjectOutputStream(sock.getOutputStream());
                oin = new ObjectInputStream(sock.getInputStream());



            } catch (IOException e) {
                e.printStackTrace();
            }


            //Cmd cmd = (Cmd) oin.readObject(); // control command

//                Cmd cmd = (Cmd) oout.writeObject();

//                player2.update(cmd.direction, cmd.isSpace);

        }
    }

    private static class Cmd implements Serializable {
        private Player.Heading direction;
        private boolean isSpace;

        protected Cmd(Player.Heading direction, boolean isSpace) {
            this.direction = direction;
            this.isSpace = isSpace;
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

//        player.update(input);
        try {
            if (input.getKey(KeyEvent.VK_UP)) {
                Cmd cmd = new Cmd(Player.Heading.NORTH, false);
//                oout.writeObject(new Cmd(Player.Heading.NORTH, false));
                oout.writeObject(cmd);
            } else if (input.getKey(KeyEvent.VK_RIGHT)) {
                oout.writeObject(new Cmd(Player.Heading.EAST, false));
//                update(Player.Heading.EAST, isSpace);
            } else if (input.getKey(KeyEvent.VK_DOWN)) {
                oout.writeObject(new Cmd(Player.Heading.SOUTH, false));
//                update(Player.Heading.SOUTH, isSpace);
            } else if (input.getKey(KeyEvent.VK_LEFT)) {
                oout.writeObject(new Cmd(Player.Heading.WEST, false));
//                update(Player.Heading.WEST, isSpace);
//            } else if (isSpace)
//                oout.writeObject(null, false);

//                update(null, true)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        player2.update(direction, isSpace);
        lvl.update();

        for (int i = 0; i < bullets.get(EntityType.Player).size(); i++)
            bullets.get(EntityType.Player).get(i).update();
        // send to all clients: write to ObjectOutputStream
    }

    private void render() {
        //прорисовка сцен
        Display.clear();
        lvl.render(graphics);
        player.render(graphics);
        player2.render(graphics);
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

if (client.oin !=null) {
    try {
        player = (ClientServer.game.Player) client.oin.readObject();
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
    }
}

//                if (srv.oout != null) {
////                    srv.oout.writeObject(); // lvl, players
//                try {
//                    srv.oout.writeObject(player);
//                    srv.oout.writeObject(player2);
////пока без уровня                    srv.oout.writeObject(lvl);
//
//                    srv.oout.flush();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
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

    public static void unregisterBullet(EntityType type, Bullet bullet) {
        if (bullets.get(type).size() > 0) {
            bullets.get(type).remove(bullet);
        }
    }

    public static List<Bullet> getBullets(EntityType type) {
        return bullets.get(type);
    }
}
