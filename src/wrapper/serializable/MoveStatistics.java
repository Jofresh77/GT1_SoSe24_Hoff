package wrapper.serializable;

import java.io.Serial;
import java.io.Serializable;

public class MoveStatistics implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    public int winAmount;
    public int occurrenceAmount;

    public MoveStatistics(int score, int occurrenceAmount) {
        this.winAmount = score;
        this.occurrenceAmount = occurrenceAmount;
    }

    @Override
    public String toString() {
        return "[winAmount=" + winAmount + ", occurrenceAmount= " + occurrenceAmount + "]";
    }
}
