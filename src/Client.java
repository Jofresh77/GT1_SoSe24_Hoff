import lenz.htw.hamidagaa.net.NetworkClient;
import lenz.htw.hamidagaa.Move;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private final byte id;
    private final byte[] board;
    private final NetworkClient net;

    public Client(String name) {
        board = new byte[49];

        for (int i = 0; i < 49; i++) {
            switch (i) {
                case 2, 3, 4, 10 -> board[i] = 0;
                case 14, 21, 22, 28 -> board[i] = 1;
                case 38, 44, 45, 46 -> board[i] = 2;
                case 20, 26, 27, 34 -> board[i] = 3;
                default -> board[i] = -1;
            }
        }

        net = new NetworkClient("192.168.0.2", 22135, name, new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB));
        id = (byte) net.getMyPlayerNumber();
    }

    public static void main(String[] args) {
        Client self = new Client(args[0]);
        self.run();
    }

    private void run() {
        while (true) {
            // FIND ALL POSSIBLE MOVES FOR ID PLAYER
            // REMOVE ALL-NON-DIAG IF ONE OR MORE DIAG POSSIBLE

            net.sendMove(getLegalMoves()[0]);

            Move move = net.receiveMove();

            if (move != null) {
                System.out.println(move);
            }
        }
    }

    private Move[] getLegalMoves() {
        List<Move> result = new ArrayList<>();

        //TODO run only if no diag moves are found before that
        for (int i = 0; i < 49 ; i++) {
            if (board[i] == id) {
                int nextPos = -1;

                switch (id) {
                    case 0 -> {
                        nextPos = i + 7;
                        if (nextPos <= 48 && board[nextPos] == -1) continue;
                    }
                    case 1 -> {
                        nextPos = i + 1;
                        if ((i % 7) != 0 && board[nextPos] == -1) continue;
                    }
                    case 2 -> {
                        nextPos = i - 7;
                        if (nextPos >= 0 && board[nextPos] == -1) continue;
                    }
                    case 3 -> {
                        nextPos = i - 1;
                        if (((i - 6) % 7) != 0 && board[nextPos] == -1) continue;
                    }
                }

                board[nextPos] = board[i];
                board[i] = -1;
                result.add(new Move(i, nextPos));
            }
        }

        return result.toArray(new Move[0]);
    }
}
