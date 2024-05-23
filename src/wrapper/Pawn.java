package wrapper;

public class Pawn<K, V> {
    public K playerId;
    public V position;

    public Pawn(K playerId, V position) {
        this.playerId = playerId;
        this.position = position;
    }

    public String toString() {
        return playerId + ": " + position;
    }
}
