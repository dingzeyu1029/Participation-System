import java.io.*;
import java.util.*;

public class StudentTracker {
    private static final String STUDENT_FILE = "data\\student";
    private static final String EVENT_FILE = "data\\event";
    private static final String PARTICIPATION_FILE = "data\\participation";

    public Map<Integer, Student> students;
    public Map<Integer, Events> events;
    public Map<Integer, List<Integer>> registration;

    public StudentTracker() throws IOException {
        students = new HashMap<>();
        events = new HashMap<>();
        registration = new HashMap<>();

        loadStudents(STUDENT_FILE);
        loadEvents(EVENT_FILE);
        loadParticipations(PARTICIPATION_FILE);
    }

    public void addStudent(String name, int classYear) throws IOException {
        if (getIdBasedOnName(name) == -1) {
            int id = StudentIDGenerator.generateID();
            students.put(id, new Student(id, name, classYear));
            saveStudents(STUDENT_FILE);
        } else {
            System.out.println("Student name already exists");
        }
    }

    public void addEvent(String name, int points, String duration) throws IOException {
        if (getEventIdBasedOnName(name) == -1) {
            int id = StudentIDGenerator.generateID();
            events.put(id, new Events(id, name, points, duration));
            saveEvents(EVENT_FILE);
        } else {
            System.out.println("Event name already exists");
        }
    }

    public void removeStudent(int id) throws IOException {
        if (students.remove(id) != null) {
            saveStudents(STUDENT_FILE);
            StudentIDGenerator.usedIDs.remove(id);
            StudentIDGenerator.saveUsedIDs();
            registration.remove(id);
            saveParticipations(PARTICIPATION_FILE);
        } else {
            System.out.println("Student does not exist");
        }
    }

    public void removeEvent(int id) throws IOException {
        if (events.remove(id) != null) {
            saveEvents(EVENT_FILE);
            StudentIDGenerator.usedIDs.remove(id);
            StudentIDGenerator.saveUsedIDs();
            // Remove the event from every student's registration
            registration.values().forEach(eventList -> eventList.remove((Integer) id));
            saveParticipations(PARTICIPATION_FILE);
        } else {
            System.out.println("Event does not exist");
        }
    }

    public void setStudentName(String name, String newName) throws IOException {
        int id = getIdBasedOnName(name);
        if (id != -1) {
            students.get(id).setName(newName);
            saveStudents(STUDENT_FILE);
        } else {
            System.out.println("Student does not exist");
        }
    }

    public void setStudentYear(int id, int year) throws IOException {
        if (students.containsKey(id)) {
            students.get(id).setClassYear(year);
            saveStudents(STUDENT_FILE);
        } else {
            System.out.println("Student does not exist");
        }
    }

    public void setEventName(String name, String newName) throws IOException {
        int id = getEventIdBasedOnName(name);
        if (id != -1) {
            events.get(id).setName(newName);
            saveEvents(EVENT_FILE);
        } else {
            System.out.println("Event does not exist");
        }
    }

    public void setEventDuration(int id, String duration) throws IOException {
        if (events.containsKey(id)) {
            events.get(id).setDuration(duration);
            saveEvents(EVENT_FILE);
        } else {
            System.out.println("Event does not exist");
        }
    }

    public void setEventPoints(int id, int points) throws IOException {
        if (events.containsKey(id)) {
            events.get(id).setPoints(points);
            saveEvents(EVENT_FILE);
        } else {
            System.out.println("Event does not exist");
        }
    }

    public void register(int studentId, int eventId) throws IOException {
        if (addStudentToParticipation(studentId, eventId)) {
            saveParticipations(PARTICIPATION_FILE);
        }
    }

    public int getIdBasedOnName(String name) {
        for (Student student : students.values()) {
            if (Objects.equals(student.getName(), name)) {
                return student.getId();
            }
        }
        return -1;
    }

    public int getEventIdBasedOnName(String name) {
        for (Events event : events.values()) {
            if (Objects.equals(event.getName(), name)) {
                return event.getId();
            }
        }
        return -1;
    }

    public void dropoff(int studentId, int eventId) throws IOException {
        List<Integer> eventList = registration.get(studentId);
        if (eventList != null && eventList.remove((Integer) eventId)) {
            if (eventList.isEmpty()) {
                registration.remove(studentId);
            }
            saveParticipations(PARTICIPATION_FILE);
        }
    }

    public int totalPoints(int studentId) {
        List<Integer> eventList = registration.get(studentId);
        if (eventList != null) {
            return eventList.stream()
                    .mapToInt(eventId -> events.get(eventId).getPoints())
                    .sum();
        }
        return 0;
    }

    public Student getTopStudentOfQuarter(String target) {
        return students.values().stream()
                .max(Comparator.comparingInt(student -> getQuarterlyPersonReport(target, student.getId())))
                .orElse(null);
    }

    public Student getRandomWinner() {
        List<Student> studentList = new ArrayList<>(students.values());
        if (studentList.isEmpty()) {
            return null;
        }
        return studentList.get(new Random().nextInt(studentList.size()));
    }

    public int getQuarterlyPersonReport(String target, int studentId) {
        int sum = 0;
        List<Integer> eventList = registration.get(studentId);
        if (eventList != null) {
            for (Integer eventId : eventList) {
                Events event = events.get(eventId);
                if (Objects.equals(target, event.getDuration())) {
                    sum += event.getPoints();
                }
            }
        }
        return sum;
    }

    private boolean addStudentToParticipation(int studentId, int eventId) {
        if (!students.containsKey(studentId) || !events.containsKey(eventId)) {
            return false;
        }
        registration.computeIfAbsent(studentId, k -> new ArrayList<>());
        if (!registration.get(studentId).contains(eventId)) {
            registration.get(studentId).add(eventId);
            return true;
        }
        return false;
    }

    // ------------------ File I/O Operations ------------------

    private void loadStudents(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.out.println("Bad student line: " + line);
                    continue;
                }
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                int classYear = Integer.parseInt(parts[2].trim());
                students.put(id, new Student(id, name, classYear));
            }
        }
    }

    private void loadEvents(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    System.out.println("Bad event line: " + line);
                    continue;
                }
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                int points = Integer.parseInt(parts[2].trim());
                String duration = parts[3].trim();
                events.put(id, new Events(id, name, points, duration));
            }
        }
    }

    private void loadParticipations(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    System.out.println("Bad participation line: " + line);
                    continue;
                }
                int studentId = Integer.parseInt(parts[0].trim());
                int eventId = Integer.parseInt(parts[1].trim());
                addStudentToParticipation(studentId, eventId);
            }
        }
    }

    private void saveStudents(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Student student : students.values()) {
                writer.write(String.format("%d,%s,%d", student.getId(), student.getName(), student.getClassYear()));
                writer.newLine();
            }
        }
    }

    private void saveEvents(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Events event : events.values()) {
                writer.write(String.format("%d,%s,%d,%s", event.getId(), event.getName(), event.getPoints(), event.getDuration()));
                writer.newLine();
            }
        }
    }

    private void saveParticipations(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Map.Entry<Integer, List<Integer>> entry : registration.entrySet()) {
                int studentId = entry.getKey();
                for (Integer eventId : entry.getValue()) {
                    writer.write(String.format("%d,%d", studentId, eventId));
                    writer.newLine();
                }
            }
        }
    }
}
