package com.github.neiplz.pedometer.models;

public class Pref {

    private int id;
    private String email;
    private int stride;
    private int goal;
    private float sensitivity;
    private int sync;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getSync() {
        return sync;
    }

    public void setSync(int sync) {
        this.sync = sync;
    }

    @Override
    public String toString() {
        return "Pref{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", stride=" + stride +
                ", goal=" + goal +
                ", sensitivity=" + sensitivity +
                ", sync=" + sync +
                '}';
    }
}
