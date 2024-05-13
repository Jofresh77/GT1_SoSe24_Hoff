import lenz.htw.hamidagaa.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Helper {

    public static void updateBoard(List<Pawn<Integer, Integer>> pawns, Move move) {
        if (move.to != move.from + 7
                && move.to != move.from - 7
                && move.to != move.from + 1
                && move.to != move.from - 1) {
            pawns.stream()
                    .filter(p -> p.position == move.to + Math.abs(move.to - move.from) / 2)
                    .findFirst()
                    .ifPresent(pawns::remove);
        }

        pawns.stream()
                .filter(p -> p.position == move.from)
                .findFirst()
                .ifPresent(p -> p.position = move.to);
    }

    //First round move retrieval require around 15 milliseconds to finish execution
    public static List<LegalMove<Move, Boolean>> getLegalMoves(int clientId, List<Pawn<Integer, Integer>> pawns) {
        List<LegalMove<Move, Boolean>> legalMoves = new ArrayList<>();

        pawns.stream()
                .filter(p -> p.playerId == clientId)
                .forEach(p -> {
                    List<Integer> nextPositions = new ArrayList<>();

                    int mod = clientId <= 1 ? 1 : -1;
                    switch (clientId) {
                        case 0, 2:
                            nextPositions.add(p.position + 7 * mod);
                            nextPositions.add((p.position + 7 * mod - 1) + 7 * mod - 1);
                            nextPositions.add((p.position + 7 * mod + 1) + 7 * mod + 1);
                            nextPositions.add(p.position + 7 * mod - 1);
                            nextPositions.add(p.position + 7 * mod + 1);
                            break;
                        case 1, 3:
                            nextPositions.add(p.position + mod);
                            nextPositions.add((p.position + mod + 7) + mod + 7);
                            nextPositions.add((p.position + mod - 7) + mod - 7);
                            nextPositions.add(p.position + mod + 7);
                            nextPositions.add(p.position + mod - 7);
                            break;
                    }

                    nextPositions.stream()
                            .filter(pos -> nextPositions.indexOf(pos) <= 2)
                            .filter(pos -> !(pos < 0 || pos >= 48))
                            .filter(pos -> pawns.stream().noneMatch(pawn -> Objects.equals(pawn.position, pos)))
                            .filter(pos -> {
                                if (clientId == 1 || clientId == 3) {
                                    if (mod > 0) {
                                        if (nextPositions.indexOf(pos) == 0) {
                                            return (p.position - 6) % 7 != 0;
                                        } else {
                                            return (nextPositions.get(3) - 6) % 7 != 0;
                                        }
                                    } else {
                                        if (nextPositions.indexOf(pos) == 0) {
                                            return p.position % 7 != 0;
                                        } else {
                                            return nextPositions.get(3) % 7 != 0;
                                        }
                                    }
                                }
                                return true;
                            })
                            .forEach(pos -> {
                                switch (nextPositions.indexOf(pos)) {
                                    case 0:
                                        legalMoves.add(new LegalMove<>(new Move(p.position, pos), false));
                                        break;
                                    case 1:
                                        if (pawns.stream()
                                                .filter(pawn -> pawn.playerId != clientId)
                                                .anyMatch(pawn -> Objects.equals(pawn.position, nextPositions.get(3)))) {
                                            legalMoves.add(new LegalMove<>(new Move(p.position, pos), true));
                                        }
                                        break;
                                    case 2:
                                        if (pawns.stream()
                                                .filter(pawn -> pawn.playerId != clientId)
                                                .anyMatch(pawn -> Objects.equals(pawn.position, nextPositions.get(4)))) {
                                            legalMoves.add(new LegalMove<>(new Move(p.position, pos), true));
                                        }
                                        break;
                                }
                            });
                });

        if (legalMoves.stream().anyMatch(m -> m.isDiagonal)) {
            return legalMoves.stream()
                    .filter(m -> m.isDiagonal)
                    .toList();
        }

        System.out.println(clientId);
        legalMoves.forEach(System.out::println);
        System.out.println("____________________");
        return legalMoves;
    }

    public static int minimax(List<Pawn<Integer, Integer>> pawns, LegalMove<Move, Boolean> legalMove,int depth, int maximizer, int clientId, int alpha, int beta) {
        if (depth == 3 || legalMove == null) {
            return evaluate(pawns);
        }

        int bestScore;

        List<Pawn<Integer, Integer>> pawnsClone = new ArrayList<>(List.copyOf(pawns));
        updateBoard(pawnsClone, legalMove.move);

        List<LegalMove<Move, Boolean>> moves = getLegalMoves(clientId, pawnsClone);

        if (maximizer == clientId) {
            bestScore = Integer.MIN_VALUE;
            for (LegalMove<Move, Boolean> move : moves) {

                int score = minimax(pawnsClone, move, depth++, maximizer, ++clientId, alpha, beta);

                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        else {
            bestScore = Integer.MAX_VALUE;
            for (LegalMove<Move, Boolean> move : moves) {
                int score = minimax(pawnsClone, move, depth++, maximizer, clientId == 3 ? 0 : ++clientId, alpha, beta);

                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestScore;
    }

    //Maybe add client ID later if necessary
    private static int evaluate(List<Pawn<Integer, Integer>> pawns) {
        return 1;
    }

    private static int[] getCoordinates(int index) {
        return new int[]{index % 7, index / 7};
    }

    private static int getIndex(int x, int y) {
        return x * 7 + y;
    }

    /*public static LegalMove<Move, Boolean> findBest(List<LegalMove<Move, Boolean>> moves) {
        return moves.stream()
                .findFirst().orElse(null);
    }*/
}
