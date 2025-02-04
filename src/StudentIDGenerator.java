import java.io.*;
import java.util.*;

public class StudentIDGenerator {
    private static final int ID_LENGTH = 4;
    private static final int MAX_TRIES = 100;
    private static final Random RANDOM = new Random();
    public static final Set<Integer> usedIDs = new HashSet<>();
    private static final String FILENAME = "data\\usedIDs";

    static {
        loadUsedIDs();
    }

    public static int generateID() {
        int maxValue = (int) Math.pow(10, ID_LENGTH); // e.g., 10^4 = 10000
        for (int i = 0; i < MAX_TRIES; i++) {
            int id = RANDOM.nextInt(maxValue); // generates a number between 0 and 9999
            if (usedIDs.add(id)) { // returns true if the id was not already present
                saveUsedIDs();
                return id;
            }
        }
        throw new IllegalStateException("Could not generate unique ID after " + MAX_TRIES + " tries");
    }

    private static void loadUsedIDs() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                usedIDs.add(Integer.parseInt(line.trim()));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading used IDs from file: " + e.getMessage());
        }
    }

    public static void saveUsedIDs() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            for (int id : usedIDs) {
                writer.write(Integer.toString(id));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing used IDs to file: " + e.getMessage());
        }
    }
}