package com.example.ftpmojo;

public class filesNameList {
    String filename;
    int[] progressBarID;

    public filesNameList(String name, int[] progressBarID) {
        this.filename = name;
        this.progressBarID = progressBarID;
    }

    public filesNameList(String name) {
        this.filename = name;
    }
}
