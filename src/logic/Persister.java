package logic;

import wrapper.serializable.MoveStatistics;
import wrapper.serializable.PlayerMove;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Persister {
    public static void saveDatabase(Map<PlayerMove, MoveStatistics> db, String filename) throws IOException {
        File file = new File(filename);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Couldn't create parent directory for file: " + filename);
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))){
            oos.writeObject(db);
        }
    }

    @SuppressWarnings("unchecked") //needed because of the cast
    public static Map<PlayerMove, MoveStatistics> loadDatabase(String filename) throws IOException, ClassNotFoundException {
        File file = new File(filename);

        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))){
            return (Map<PlayerMove, MoveStatistics>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid file format or class not found: " + e.getMessage(), e);
        }
    }
}
