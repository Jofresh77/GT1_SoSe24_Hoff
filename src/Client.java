import lenz.htw.hamidagaa.net.NetworkClient;
import lenz.htw.hamidagaa.Move;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public static Client self;

    final byte id;
    final NetworkClient net;

    final List<Pawn<Integer, Integer>> pawns = new ArrayList<>();

    public Client(String name) {
        pawns.add(new Pawn<>(0, 2));
        pawns.add(new Pawn<>(0, 3));
        pawns.add(new Pawn<>(0, 4));
        pawns.add(new Pawn<>(0, 10));

        pawns.add(new Pawn<>(1, 14));
        pawns.add(new Pawn<>(1, 21));
        pawns.add(new Pawn<>(1, 22));
        pawns.add(new Pawn<>(1, 28));

        pawns.add(new Pawn<>(2, 38));
        pawns.add(new Pawn<>(2, 44));
        pawns.add(new Pawn<>(2, 45));
        pawns.add(new Pawn<>(2, 46));

        pawns.add(new Pawn<>(3, 20));
        pawns.add(new Pawn<>(3, 26));
        pawns.add(new Pawn<>(3, 27));
        pawns.add(new Pawn<>(3, 34));

        net = new NetworkClient(null, 22135, name, new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB));
        id = (byte) net.getMyPlayerNumber();
    }

    public static void main(String[] args) {
        self = new Client(args[0]);
        self.run();
    }

    private void run() {
        System.out.println(id);

        for (int i = 0; i < 100; i++){
            Move receive;
            while ((receive = net.receiveMove()) != null) {
                Move move = receive;

                if (move.to != move.from + 7
                && move.to != move.from - 7
                && move.to != move.from + 1
                && move.to != move.from - 1) {
                    pawns.stream()
                            .filter(p -> {
                                if (move.from < move.to) {
                                    return p.position == move.from + (move.to - move.from) / 2;
                                } else {
                                    return p.position == move.to + Math.abs(move.to - move.from) / 2;
                                }
                            })
                            .findFirst()
                            .ifPresent(pawns::remove);
                }

                pawns.stream()
                        .filter(p -> p.position == move.from)
                        .findFirst()
                        .ifPresent(p -> p.position = move.to);
            }

            List<LegalMove<Move, Boolean>> moves = Helper.getLegalMoves(self);
            LegalMove<Move, Boolean> bestMove = Helper.findBest(moves);

            //System.out.println(bestMove.move);
            net.sendMove(bestMove.move);
        }
    }

    static class Pawn<K, V> {
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
}
