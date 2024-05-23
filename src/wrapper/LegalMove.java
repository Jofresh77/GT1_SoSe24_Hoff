package wrapper;

public class LegalMove<K, V> {
    public K move;
    public V isDiagonal;

    public LegalMove(K move, V isDiagonal) {
        this.move = move;
        this.isDiagonal = isDiagonal;
    }

    public String toString() {
        return move + " " + isDiagonal;
    }
}