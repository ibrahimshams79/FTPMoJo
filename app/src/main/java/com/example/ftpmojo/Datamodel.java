package com.example.ftpmojo;

public class Datamodel {
//    int storyId, noOfFiles;
    String storyTitle, storyDesc, noOfFiles, storyTime;

    public Datamodel(String noOfFiles, String storyTitle, String storyDesc, String storyTime) {

        this.noOfFiles = noOfFiles;
        this.storyTitle = storyTitle;
        this.storyDesc = storyDesc;
        this.storyTime = storyTime;
    }

    public String getNoOfFiles() {
        return noOfFiles;
    }

    public String getStoryTime() {
        return storyTime;
    }

    public void setStoryTime(String storyTime) {
        this.storyTime = storyTime;
    }

    public void setNoOfFiles(String noOfFiles) {
        this.noOfFiles = noOfFiles;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public String getStoryDesc() {
        return storyDesc;
    }

    public void setStoryDesc(String storyDesc) {
        this.storyDesc = storyDesc;
    }
}
