package ClientServer.game;

import ClientServer.IO.Input;
import ClientServer.game.level.Level;
import ClientServer.graphics.TextureAtlas;

import java.awt.*;

public abstract class Entity {

    public final EntityType type;

    //местонахождение объектов
    protected float x;
    protected float y;
    protected float height;
    protected float width;
    protected TextureAtlas atlas;
    protected static Level lvl;

    protected Entity(EntityType type, float x, float y, TextureAtlas atlas) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.atlas = atlas;
    }

    public abstract void update(Input input);

    public abstract void render(Graphics2D g);

//    public boolean isInArea(int x, int y) {

//    }
}