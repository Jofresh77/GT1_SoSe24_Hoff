package client;

import lenz.htw.hamidagaa.net.NetworkClient;
import wrapper.Pawn;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public final int id;
    final NetworkClient net;

    public List<Pawn<Integer, Integer>> pawns = new ArrayList<>();

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
        id = net.getMyPlayerNumber();
    }
}