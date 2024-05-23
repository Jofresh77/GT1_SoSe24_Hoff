package logic;

import lenz.htw.hamidagaa.Move;
import wrapper.LegalMove;
import wrapper.Pawn;

import java.util.*;

public class Helper {
    private static final long[][] zobristTable = new long[48][5];

    private static Move previousMove = new Move(0, 0);

    // <editor-fold defaultstate="collapsed" desc="Zobrist Hashing">
    static {
        Random random = new Random();

        for (int i = 0; i < 48; i++) {
            for (int j = 0; j < 5; j++) {
                zobristTable[i][j] = random.nextLong();
            }
        }
    }

    private static long computeHash(List<Pawn<Integer, Integer>> pawns) {
        long hashValue = 0;

        for (int pos = 0; pos < 48; pos++) {
            boolean occupied = false;

            for (Pawn<Integer, Integer> pawn : pawns) {
                if (pawn.position == pos) {
                    hashValue ^= zobristTable[pos][pawn.playerId];
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                hashValue ^= zobristTable[pos][4];
            }
        }

        return hashValue;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Board Update & Move retrieval">
    public static List<Pawn<Integer, Integer>> updateBoard(List<Pawn<Integer, Integer>> pawns, Move move, boolean a) {
        if (previousMove.equals(move)) {
            return pawns;
        }

        List<Pawn<Integer, Integer>> pawnsClone = deepClonePawns(pawns);

        if (!(move.to == move.from + 7
                || move.to == move.from - 7
                || move.to == move.from + 1
                || move.to == move.from - 1)) {
            if (move.from < move.to) {
                pawnsClone.removeIf(p -> p.position == move.from + (Math.abs(move.to - move.from) / 2));
            } else {
                pawnsClone.removeIf(p -> p.position == move.to + (Math.abs(move.to - move.from) / 2));
            }
        }

        pawnsClone.stream()
                .filter(p -> p.position == move.from)
                .findFirst()
                .ifPresent(p -> p.position = move.to);

        previousMove = move;
        return pawnsClone;
    }

    // First round move retrieval require around 15 milliseconds to finish execution
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

        return legalMoves;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Minimax">
    public static int minimax(Map<Long, Integer> transpositionTable, List<Pawn<Integer, Integer>> pawns, LegalMove<Move, Boolean> legalMove,
                              int depth, int maximizer, int clientId, int alpha, int beta) {
        List<Pawn<Integer, Integer>> updatedPawns = updateBoard(pawns, legalMove.move, false);
        List<LegalMove<Move, Boolean>> moves = getLegalMoves(clientId, updatedPawns);

        int score = isGameOver(clientId, updatedPawns, moves);

        if (score != 0) {
            return score;
        }

        if (depth == 20) {
            score = evaluate(maximizer, updatedPawns, legalMove);
            return score;
        }

        long hashValue = computeHash(updatedPawns);
        if (transpositionTable.containsKey(hashValue)) {
            return transpositionTable.get(hashValue);
        }

        int bestScore;

        bestScore = maximizer == clientId ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (LegalMove<Move, Boolean> move : moves) {
            score = minimax(transpositionTable, updatedPawns, move, depth + 1, maximizer, (clientId + 1) % 4, alpha, beta);

            bestScore = maximizer == clientId ? Math.max(bestScore, score) : Math.min(bestScore, score);

            if (maximizer == clientId) {
                alpha = Math.max(alpha, bestScore);
            } else {
                beta = Math.min(beta, bestScore);
            }

            if (beta <= alpha) {
                break;
            }
        }

        transpositionTable.put(hashValue, bestScore);
        return bestScore;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Evaluation">
    private static int isGameOver(int clientId, List<Pawn<Integer, Integer>>
            pawns, List<LegalMove<Move, Boolean>> moves) {
        // player has no more pawns
        // OR no more moves but all his pawns are not on finish line => [LOOSE]
        if (pawns.stream().noneMatch(p -> p.playerId == clientId)
                || (moves.isEmpty() && !pawns.stream().allMatch(p -> isGoal(clientId, p.position)))) {
            return -10;
        }

        int playerBlocked = 0;

        for (int i = 0; i < 4; i++) {
            if (i == clientId) {
                if (moves.isEmpty()) {
                    playerBlocked++;
                }
                continue;
            }

            if (getLegalMoves(i, pawns).isEmpty()) {
                playerBlocked++;
            }

            //other player won => [LOOSE]
            int finalI = i;
            if (pawns.stream().allMatch(p -> isGoal(finalI, p.position))) {
                return -10;
            }
        }

        // [DRAW]
        if (playerBlocked == 4) {
            return -10;
        }

        // player has all his pawns on finish line => [WIN]
        if (moves.isEmpty() && pawns.stream().allMatch(p -> isGoal(clientId, p.position))) {
            return 10;
        }

        return 0;
    }

    private static int evaluate(int clientId, List<Pawn<Integer, Integer>>
            pawns, LegalMove<Move, Boolean> legalMove) {
        int evaluationScore = 0;

        //the pawn just moved on a goal position => approach victory
        if (isGoal(clientId, legalMove.move.to)) {
            evaluationScore += 5;
        }

        if (legalMove.isDiagonal) {
            evaluationScore += 1;
        }

        //TODO find if eaten player has more or less than 2 pawn (if > 2 = +1 else = +3)

        //pawn land diagonally next to already present neighbors, meaning that they might eat that pawn next turn
        if (getMovedPawn(pawns, legalMove.move.to).playerId == clientId) {
            // > 3 is different from > 2
            if (getPawnsLeft(clientId, pawns) > 3) {
                evaluationScore += getNeighborCount(clientId, pawns, legalMove.move.to);
            } else {
                evaluationScore -= getNeighborCount(clientId, pawns, legalMove.move.to);
            }
        }

        evaluationScore -= getBlocked(clientId, pawns);

        return evaluationScore;
    }

    public static boolean isGoal(int clientId, int position) {
        return switch (clientId) {
            case 0 -> Arrays.binarySearch(new int[]{42, 43, 44, 45, 46, 47, 48}, position) >= 0;
            case 1 -> Arrays.binarySearch(new int[]{6, 13, 20, 27, 34, 41, 48}, position) >= 0;
            case 2 -> Arrays.binarySearch(new int[]{0, 1, 2, 3, 4, 5, 6}, position) >= 0;
            case 3 -> Arrays.binarySearch(new int[]{0, 7, 14, 21, 28, 35, 42}, position) >= 0;
            default -> false;
        };
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utils">
    private static int getNeighborCount(int clientId, List<Pawn<Integer, Integer>> pawns, int position) {
        return switch (clientId) {
            case 0 -> (int) pawns.stream()
                    .filter(p -> (p.playerId == 2 && (p.position == position + 7 - 1 || p.position == position + 7 + 1))
                            || (p.playerId == 1 && (p.position == position - 1 - 7 || p.position == position - 1 + 7))
                            || (p.playerId == 3 && (p.position == position + 1 - 7 || p.position == position + 1 + 7)))
                    .count();
            case 1 -> (int) pawns.stream()
                    .filter(p -> (p.playerId == 0 && (p.position == position - 7 - 1 || p.position == position - 7 + 1))
                            || (p.playerId == 2 && (p.position == position + 7 - 1 || p.position == position + 7 + 1))
                            || (p.playerId == 3 && (p.position == position + 1 - 7 || p.position == position + 1 + 7)))
                    .count();
            case 2 -> (int) pawns.stream()
                    .filter(p -> (p.playerId == 0 && (p.position == position - 7 - 1 || p.position == position - 7 + 1))
                            || (p.playerId == 1 && (p.position == position - 1 - 7 || p.position == position - 1 + 7))
                            || (p.playerId == 3 && (p.position == position + 1 - 7 || p.position == position + 1 + 7)))
                    .count();
            case 3 -> (int) pawns.stream()
                    .filter(p -> (p.playerId == 0 && (p.position == position - 7 - 1 || p.position == position - 7 + 1))
                            || (p.playerId == 2 && (p.position == position + 7 - 1 || p.position == position + 7 + 1))
                            || (p.playerId == 1 && (p.position == position - 1 - 7 || p.position == position - 1 + 7)))
                    .count();
            default -> 0;
        };
    }

    private static Pawn<Integer, Integer> getMovedPawn(List<Pawn<Integer, Integer>> pawns, int position) {
        return pawns.stream()
                .filter(p -> p.position == position)
                .findFirst().orElse(null);
    }

    private static long getPawnsLeft(int clientId, List<Pawn<Integer, Integer>> pawns) {
        return pawns.stream()
                .filter(p -> p.playerId == clientId)
                .count();
    }

    private static int getBlocked(int clientId, List<Pawn<Integer, Integer>> pawns) {
        int amount = 0;

        for (Pawn<Integer, Integer> pawn : pawns) {
            if (pawn.playerId == clientId) {
                switch (clientId) {
                    case 0:
                        if (pawns.stream().anyMatch(p -> pawn.position + 1 == p.position)) {
                            amount++;
                        }
                        break;
                    case 1:
                        if (pawns.stream().anyMatch(p -> pawn.position + 7 == p.position)) {
                            amount++;
                        }
                        break;
                    case 2:
                        if (pawns.stream().anyMatch(p -> pawn.position - 1 == p.position)) {
                            amount++;
                        }
                        break;
                    case 3:
                        if (pawns.stream().anyMatch(p -> pawn.position - 7 == p.position)) {
                            amount++;
                        }
                        break;
                }
            }
        }

        return amount;
    }

    public static List<Pawn<Integer, Integer>> deepClonePawns(List<Pawn<Integer, Integer>> pawns) {
        List<Pawn<Integer, Integer>> clonedPawns = new ArrayList<>();
        for (Pawn<Integer, Integer> pawn : pawns) {
            clonedPawns.add(new Pawn<>(pawn.playerId, pawn.position));
        }
        return clonedPawns;
    }
    // </editor-fold>
}