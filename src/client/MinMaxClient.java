package client;

import lenz.htw.hamidagaa.Move;
import logic.Helper;
import wrapper.LegalMove;
import wrapper.Pawn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinMaxClient extends Client{
    public static MinMaxClient self;

    public final Map<Long, Integer> transpositionTable = new HashMap<>();

    public MinMaxClient(String name) {
        super(name);
    }

    // Useful for GUI-startup only
    public static void main(String[] args) {
        self = new MinMaxClient(args[0]);
        System.out.println("Client ID: " + self.id);

        self.play();
    }

    public void play() {
        //run minimax blank to precompute transposition table
        Helper.getLegalMoves(this.id, this.pawns)
                .forEach(move -> Helper.minimax(this.transpositionTable, this.pawns, move, 0, this.id, 0, Integer.MIN_VALUE, Integer.MAX_VALUE));

        for (int i = 0; i < 100; i++) {
            Move receive;
            while ((receive = net.receiveMove()) != null) {
                this.pawns = Helper.updateBoard(this.pawns, receive, true);
            }

            List<LegalMove<Move, Boolean>> moves = Helper.getLegalMoves(this.id, this.pawns);
            LegalMove<Move, Boolean> bestMove = findBestMove(this.pawns, moves);

            if (bestMove != null) {
                net.sendMove(bestMove.move);
            } else {
                System.err.println("No valid move found.");
            }
        }
    }

    private LegalMove<Move, Boolean> findBestMove(List<Pawn<Integer, Integer>> pawns, List<LegalMove<Move, Boolean>> moves) {
        LegalMove<Move, Boolean> bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (LegalMove<Move, Boolean> move : moves) {
            int score = Helper.minimax(this.transpositionTable, pawns, move, 0, this.id, this.id, Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }
}