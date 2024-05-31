package logic;

import client.UCB1Client;
import lenz.htw.hamidagaa.Move;
import lenz.htw.hamidagaa.Server;
import wrapper.serializable.MovePersistent;
import wrapper.serializable.MoveStatistics;
import wrapper.serializable.PlayerMove;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Trainer {
    static final String DATABASE_FILENAME = "resource/against-self/ucb1_memory_simulation8k.ser";
    static final float ACCEPTANCE_THRESHOLD = 0.7f;

    public static void main(String[] args) {
        //Games are simulated here
        //Set i number of simulations
        for (int i = 0; i < 1000; i++) {
            final Map<PlayerMove, MoveStatistics> db;
            Map<PlayerMove, MoveStatistics> dbTransient = new ConcurrentHashMap<>();

            // <editor-fold defaultstate="collapsed" desc="DB-LOAD">
            try {
                dbTransient = Persister.loadDatabase(DATABASE_FILENAME);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading database: " + e.getMessage());
                dbTransient = new ConcurrentHashMap<>();
            } finally {
                db = dbTransient;
            }
            // </editor-fold>

            final int C = 1;
            AtomicInteger winnerId = new AtomicInteger();

            ExecutorService executorService = Executors.newFixedThreadPool(5);
            List<CompletableFuture<?>> allFutures = new ArrayList<>();

            final Map<Integer, Integer> clientNameToId = new HashMap<>();
            final Map<Integer, List<Move>> playerMoves = new HashMap<>();

            // <editor-fold defaultstate="collapsed" desc="Server-Thread">
            CompletableFuture<Integer> futureResult = CompletableFuture.supplyAsync(
                    () -> Server.runOnceAndReturnTheWinner(1, 22135),
                    executorService
            );

            allFutures.add(futureResult.thenAccept(result -> winnerId.set(result - 1)));
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Client-Threads">
            for (int j = 0; j < 4; j++) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException ignored) {
                }

                int clientName = j;

                CompletableFuture<List<Move>> futurePlayedMoves = CompletableFuture.supplyAsync(() -> {
                    UCB1Client client = new UCB1Client("Client" + clientName, db, C, ACCEPTANCE_THRESHOLD);

                    clientNameToId.put(clientName, client.id);

                    return client.play();
                }, executorService);

                allFutures.add(futurePlayedMoves.thenAccept(moves -> playerMoves.put(clientNameToId.get(clientName), moves)));
            }
            // </editor-fold>

            CompletableFuture<?> updateAndSaveFuture = CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        // <editor-fold defaultstate="collapsed" desc="DB-ACCESS">
                        for (int j = 0; j < 4; j++) {
                            for (Move move : playerMoves.get(j)) {
                                int playerId = j;
                                db.compute(
                                        new PlayerMove(playerId, new MovePersistent(move.from, move.to)),
                                        (key, existingStats) -> {
                                            if (existingStats == null) {
                                                existingStats = new MoveStatistics(0, 1); // If it doesn't exist, create it with occurrence = 1
                                            } else {
                                                existingStats.occurrenceAmount++;
                                            }
                                            if (playerId == winnerId.get()) {
                                                existingStats.winAmount++;
                                            }
                                            return existingStats;
                                        }
                                );
                            }
                        }
                        // </editor-fold>

                        // <editor-fold defaultstate="collapsed" desc="DB-SAVE">
                        try {
                            Persister.saveDatabase(db, DATABASE_FILENAME);
                        } catch (IOException e) {
                            System.err.println("Error while saving database: " + e.getMessage());
                            e.printStackTrace();
                        }
                        // </editor-fold>

                        executorService.shutdown();
                    })
                    .exceptionally(ex -> {
                        System.err.println("Error in game execution or DB operations: " + ex.getMessage());
                        ex.printStackTrace();
                        executorService.shutdown();
                        return null;
                    });

            // Wait for the game to stop before iterating again
            try {
                updateAndSaveFuture.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Visualizer.visualize(DATABASE_FILENAME);
    }
}