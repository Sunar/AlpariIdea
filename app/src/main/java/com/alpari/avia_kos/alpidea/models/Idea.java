package com.alpari.avia_kos.alpidea.models;

import java.io.Serializable;

/**
 * Created by Avia-Kos on 10.05.16.
 */
public class Idea implements Serializable{
    private String content;
    private IdeaStatus status;
    private String resources;
    private String goal;
    private String realizTime;
    private String rukDecision = "неизвестно";

    public Idea(IdeaStatus status, String content, String resources, String goal, String realizTime, String rukDecision) {
        this.status = status;
        this.content = content;
        this.resources = resources;
        this.goal = goal;
        this.realizTime = realizTime;
        this.rukDecision = (rukDecision == null)? this.rukDecision : rukDecision;
    }

    public void setStatus(IdeaStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public IdeaStatus getStatus() {
        return status;
    }

    public String getResources() {
        return resources;
    }

    public String getGoal() {
        return goal;
    }

    public String getRealizTime() {
        return realizTime;
    }

    public String getRukDecision() {
        return rukDecision;
    }
}
