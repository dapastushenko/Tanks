package server.IO;

import server.game.Bullet;
import server.game.Player;
import server.game.level.Level;

import java.io.Serializable;
import java.util.List;

public class RenderObject implements Serializable {
    public final Player serverPlayer;
    public final Player clientPlayer;
    public final Level level;
    public List<Bullet> serverPlayerBullets;
    public List<Bullet> clientPlayerBullets;

    public RenderObject(
            Player serverPlayer,
            Player clientPlayer,
            Level level,
            List<Bullet> serverPlayerBullets,
            List<Bullet> clientPlayerBullets) {
        this.serverPlayer = serverPlayer;
        this.clientPlayer = clientPlayer;
        this.level = level;
        this.serverPlayerBullets = serverPlayerBullets;
        this.clientPlayerBullets = clientPlayerBullets;
    }

    @Override
    public String toString() {
        return "RenderObject{" +
                "serverPlayer=" + serverPlayer +
                ", clientPlayer=" + clientPlayer +
                ", level=" + level +
                ", serverPlayerBullets=" + serverPlayerBullets +
                ", clientPlayerBullets=" + clientPlayerBullets +
                '}';
    }
}
