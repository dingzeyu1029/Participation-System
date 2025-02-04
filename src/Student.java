public class Student {
    private String name;
    private int classYear;//class of 202*
    private final int id;

    public Student(int id, String name, int classYear) {
        this.id = id;
        this.name = name;
        this.classYear = classYear;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setClassYear(int classYear){
        this.classYear = classYear;
    }

    public String getName() {
        return name;
    }

    public int getClassYear() {
        return classYear;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString(){
        return String.format("StudentName(%s) ClassYear(%d) id(%d)", name, classYear, id);
    }
}

