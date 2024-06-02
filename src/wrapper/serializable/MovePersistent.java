package wrapper.serializable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

// copy class of legacy Move object
public class MovePersistent implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    public int from;
    public int to;

    public MovePersistent(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MovePersistent other = (MovePersistent) obj;
        return from == other.from && to == other.to;
    }

    public String toString() {
        return this.from + " -> " + this.to;
    }
}
