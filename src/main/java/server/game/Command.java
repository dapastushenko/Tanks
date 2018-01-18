package server.game;

import java.io.Serializable;

public class Command implements Serializable {
    public final Heading direction;
    public final boolean isSpace;

    public Command(Heading direction, boolean isSpace) {
        this.direction = direction;
        this.isSpace = isSpace;
    }

    @Override
    public String toString() {
        return "Command{" +
                "direction=" + direction +
                ", isSpace=" + isSpace +
                '}';
    }
}
