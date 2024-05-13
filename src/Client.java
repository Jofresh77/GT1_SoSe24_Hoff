import lenz.htw.hamidagaa.net.NetworkClient;
import lenz.htw.hamidagaa.Move;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
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

        for (int i = 0; i < 10; i++){
            Move receive;
            while ((receive = net.receiveMove()) != null) {
                Helper.updateBoard(self.pawns, receive);
            }

            List<LegalMove<Move, Boolean>> moves = Helper.getLegalMoves(self.id, self.pawns);
            //moves.forEach(System.out::println);

            //TODO FIX GETLEGALMOVES

            LegalMove<Move, Boolean> bestMove = null;
            int bestScore = Integer.MIN_VALUE;

            for(LegalMove<Move, Boolean> move : moves){
                int clientId = self.id;
                int score = Helper.minimax(pawns, move, 0, self.id, clientId, -1000, 1000);
                //System.out.println("Score: " + score + " | move: " + move);

                if (bestMove == null || score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }

            if (bestMove == null) {
                System.err.println("Null best");
            }

            //System.out.println(bestMove);
            net.sendMove(bestMove.move);
        }
    }
}
