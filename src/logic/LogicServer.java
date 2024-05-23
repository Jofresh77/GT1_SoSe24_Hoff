package logic;

import client.MinMaxClient;
import lenz.htw.hamidagaa.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LogicServer {
    public static void main(String[] args) {
        final Map<Integer, String> idToName = new HashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        Future<Integer> result = executorService.submit(() -> Server.runOnceAndReturnTheWinner(8, 22135));

        for (int i = 0; i < 4; i++) {
            final String clientName = "Client" + i;
            executorService.submit(() -> {
                MinMaxClient client = new MinMaxClient(clientName);

                idToName.put(client.id, clientName);
                client.play();
            });
        }

        try {
            int winnerId = result.get() - 1;
            System.out.println("Winner: " + idToName.get(winnerId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}