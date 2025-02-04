public class Events {
    private final int id;
    private String name;
    private int points;
    private String duration;

    public Events(int id, String name, int points, String duration){
        this.id = id;
        this.name = name;
        this.points = points;
        this.duration = duration;
    }
    //toString
    public void setName(String name){
        this.name = name;
    }

    public void setDuration(String duration){
        this.duration = duration;
    }

    public void setPoints(int points){
        this.points = points;
    }
    public int getPoints(){
        return points;
    }
    public int getId() { return id; }

    public String getName() { return name; }
    public String getDuration() { return duration; }

    @Override
    public String toString(){
        return String.format("Name(%s)", name);
    }
}
