package wrapper.serializable;

import java.io.Serial;
import java.io.Serializable;

public record PlayerMove(int playerId, MovePersistent move) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayerMove other = (PlayerMove) obj;
        return playerId == other.playerId && move.equals(other.move);
    }
}
