import lenz.htw.hamidagaa.Move;

public class LegalMove<K, V> {
    protected K move;
    protected V isDiagonal;

    public LegalMove(K move, V isDiagonal) {
        this.move = move;
        this.isDiagonal = isDiagonal;
    }
}