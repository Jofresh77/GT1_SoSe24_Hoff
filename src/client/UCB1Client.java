package client;

import lenz.htw.hamidagaa.Move;
import logic.Helper;
import wrapper.LegalMove;
import wrapper.serializable.MovePersistent;
import wrapper.serializable.MoveStatistics;
import wrapper.serializable.PlayerMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UCB1Client extends Client {
    final Map<PlayerMove, MoveStatistics> db;
    final List<Move> playedMoves;
    final int C;
    final float ACCEPTANCE_THRESHOLD;

    public UCB1Client(String name, Map<PlayerMove, MoveStatistics> db, int C, float ACCEPTANCE_THRESHOLD) {
        super(name);

        this.db = db;
        this.playedMoves = new ArrayList<>();
        this.C = C;
        this.ACCEPTANCE_THRESHOLD = ACCEPTANCE_THRESHOLD;
    }

    public List<Move> play() {
        try {
            for (int i = 0; i < 100; i++) {
                Move receive;
                while ((receive = net.receiveMove()) != null) {
                    this.pawns = Helper.updateBoard(this.pawns, receive, true);
                }

                List<LegalMove<Move, Boolean>> moves = Helper.getLegalMoves(this.id, this.pawns);
                Move bestMove = findBestMove(i, moves);

                playedMoves.add(bestMove);
                net.sendMove(bestMove);
            }

            return playedMoves;
        } catch (Exception e) {
            return playedMoves;
        }
    }

    private synchronized Move findBestMove(int iteration, List<LegalMove<Move, Boolean>> moves) {
        double bestScore = Double.MIN_VALUE;
        double dynamicC = Math.max(0.1, 1.0 / Math.sqrt(iteration)); // the more the game evolves, the more we exploit instead of explore
        Move bestMove = null;

        for (LegalMove<Move, Boolean> legalMove : moves) {
            MoveStatistics moveStatistics = db.get(new PlayerMove(this.id, new MovePersistent(legalMove.move.from, legalMove.move.to)));

            if (moveStatistics != null) {
/*
                double score = ((double) moveStatistics.winAmount / moveStatistics.occurrenceAmount)
                        + (dynamicC * (Math.sqrt(Math.log(iteration) / moveStatistics.occurrenceAmount)));
*/
                double score = ((double) moveStatistics.winAmount / moveStatistics.occurrenceAmount)
                        + (this.C * (Math.sqrt(Math.log(iteration) / moveStatistics.occurrenceAmount)));

                if (bestScore < score) {
                    bestScore = score;
                    bestMove = legalMove.move;
                }
            }
        }

        //return random move if (nothing found in db) OR (acceptance too low) => [Exploration VS Exploitation]
        if (bestMove == null || bestScore < this.ACCEPTANCE_THRESHOLD) {
            return moves.get(new Random().nextInt(moves.size())).move;
        }

        return bestMove;
    }
}
