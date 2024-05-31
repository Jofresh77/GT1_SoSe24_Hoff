package logic;

import wrapper.serializable.MoveStatistics;
import wrapper.serializable.PlayerMove;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Visualizer {
    final static String FILENAME = "resource/ucb1_memory01.ser";

    public static void main(String[] args) {
        visualize(FILENAME);
    }

    @SuppressWarnings("unchecked")
    public static void visualize(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Map<PlayerMove, MoveStatistics> db = (Map<PlayerMove, MoveStatistics>) ois.readObject();

            for (Map.Entry<PlayerMove, MoveStatistics> entry : db.entrySet()) {
                PlayerMove move = entry.getKey();
                MoveStatistics stats = entry.getValue();
                System.out.println("Player: " + move.playerId() +
                        ", Move: " + move.move().from + " -> " + move.move().to +
                        ", Wins: " + stats.winAmount +
                        ", Occurrences: " + stats.occurrenceAmount);
            }

            MoveStatistics maxOccurrencesStats = db.entrySet().stream()
                    .max(Comparator.comparing(e -> e.getValue().occurrenceAmount))
                            .map(Map.Entry::getValue)
                                    .orElse(null);
            MoveStatistics maxWinsStats = db.entrySet().stream()
                    .max(Comparator.comparing(e -> e.getValue().winAmount))
                    .map(Map.Entry::getValue)
                    .orElse(null);

            System.out.println("Max occurrences: " + maxOccurrencesStats);
            System.out.println("Max wins: " + maxWinsStats);
            printTop4Players(db);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void printTop4Players(Map<PlayerMove, MoveStatistics> db) {
        Map<Integer, Integer> playerWins = db.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().playerId(),
                        Collectors.summingInt(entry -> entry.getValue().winAmount)
                ));

        LinkedHashMap<Integer, Integer> sortedPlayerWins = playerWins.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        if (!sortedPlayerWins.isEmpty()) {
            System.out.println("Top 4 Players (based on wins):");
            int rank = 1;
            for (Map.Entry<Integer, Integer> entry : sortedPlayerWins.entrySet()) {
                System.out.println(rank + ". Player " + entry.getKey() + ": " + entry.getValue() + " wins");
                rank++;
            }
        } else {
            System.out.println("No player statistics found in the database.");
        }
    }
}
