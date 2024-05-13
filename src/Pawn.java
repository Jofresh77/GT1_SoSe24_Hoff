public class Pawn<K, V> {
    protected K playerId;
    protected V position;

    public Pawn(K playerId, V position) {
        this.playerId = playerId;
        this.position = position;
    }

    public String toString() {
        return playerId + ": " + position;
    }
}
