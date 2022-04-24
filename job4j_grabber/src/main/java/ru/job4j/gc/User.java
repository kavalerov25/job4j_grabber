package ru.job4j.gc;

public class User {
    private String name;
    private int age;

    public User(String name, int id) {
        this.name = name;
        this.age = id;
    }

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return age;
    }

    public void setId(int id) {
        this.age = id;
    }

    @Override
    protected void finalize() {
        System.out.printf("Removed %d %s%n", age, name);
    }
}
