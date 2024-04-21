import lenz.htw.hamidagaa.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Helper {

    public static List<LegalMove<Move, Boolean>> getLegalMoves(Client client) {
        List<LegalMove<Move, Boolean>> legalMoves = new ArrayList<>();

        client.pawns.stream()
                .filter(p -> p.playerId == client.id)
                .forEach(p -> {
                    List<Integer> nextPositions = new ArrayList<>();

                    int mod = client.id <= 1 ? 1 : -1;
                    switch (client.id) {
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
                            .filter(pos -> client.pawns.stream().noneMatch(pawn -> Objects.equals(pawn.position, pos)))
                            .filter(pos -> {
                                if (client.id == 1 || client.id == 3) {
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
                                        if (client.pawns.stream()
                                                .filter(pawn -> pawn.playerId != client.id)
                                                .anyMatch(pawn -> Objects.equals(pawn.position, nextPositions.get(3)))) {
                                            legalMoves.add(new LegalMove<>(new Move(p.position, pos), true));
                                        }
                                        break;
                                    case 2:
                                        if (client.pawns.stream()
                                                .filter(pawn -> pawn.playerId != client.id)
                                                .anyMatch(pawn -> Objects.equals(pawn.position, nextPositions.get(4)))) {
                                            legalMoves.add(new LegalMove<>(new Move(p.position, pos), true));
                                        }
                                        break;
                                }
                            });
                });

        return legalMoves;
    }

    public static LegalMove<Move, Boolean> findBest(List<LegalMove<Move, Boolean>> moves) {
        if (moves.stream().findAny().isPresent()) {
            return moves.stream()
                    .filter(m -> m.isDiagonal)
                    .findFirst()
                    .orElse(moves.stream()
                            .findFirst().get());
        }
        return null;
    }
}
