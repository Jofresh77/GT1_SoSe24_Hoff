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
                            nextPositions.add((p.position + 7 * mod - 1) + (p.position + 7 * mod - 1));
                            nextPositions.add((p.position + 7 * mod + 1) + (p.position + 7 * mod + 1));
                            nextPositions.add(p.position + 7 * mod - 1);
                            nextPositions.add(p.position + 7 * mod + 1);
                            break;
                        case 1, 3:
                            nextPositions.add(p.position + mod);
                            nextPositions.add((p.position + mod + 7) + (p.position + mod + 7));
                            nextPositions.add((p.position + mod - 7) + (p.position + mod - 7));
                            nextPositions.add(p.position + mod + 7);
                            nextPositions.add(p.position + mod - 7);
                            break;
                    }

                    nextPositions.stream()
                            .filter(pos -> nextPositions.indexOf(pos) <= 2)
                            .forEach(pos -> {
                        if (!((pos < 0
                                || pos >= client.pawns.size()
                                || p.position % 7 == 0
                                || (p.position - 6) % 7 == 0))
                                && client.pawns.stream().noneMatch(pawn -> Objects.equals(pawn.position, pos))) {
                            switch (nextPositions.indexOf(pos)) {
                                case 0:
                                    legalMoves.add(new LegalMove<>(new Move(p.position, pos), false));
                                    break;
                                    case 1:
                                        if (client.pawns.stream()
                                                .filter(pawn -> pawn.playerId != client.id)
                                                .anyMatch(pawn -> Objects.equals(pawn.position, pos))) {
                                            //TODO finish the if statement: replace pos with the 4th index of nextPositions and 5th in the other case 2
                                    }
                            }
                            if (nextPositions.indexOf(pos) == 0) {
                            }
                        }
                    });
                });

        return legalMoves;
    }

    public static LegalMove<Move, Boolean> findBest(List<LegalMove<Move, Boolean>> moves) {
        return moves.stream().findFirst().orElse(null);
    }
}
