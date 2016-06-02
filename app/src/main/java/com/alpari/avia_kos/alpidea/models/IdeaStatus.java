package com.alpari.avia_kos.alpidea.models;

import com.alpari.avia_kos.alpidea.DB;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Avia-Kos on 13.05.16.
 */
public class IdeaStatus implements Serializable{
    public static final int NOT_CONSIDERED = 1;
    public static final int UNDER_CONSIDERING = 2;
    public static final int SENT_TO_RUK = 3;
    public static final int REJECTED_BY_EXPERT = 4;
    public static final int RUK_DECIDED = 5;
    public static final int REJECTED_BY_RUK = 6;
    public static final int IDEA_ACCEPTED = 7;
    public static final int REJECTED_ON_DISCUSSION = 8;
    public static final int ACCEPTED_ON_DISCUSSION = 9;
    private static ArrayList<IdeaStatus> statuses = new ArrayList<>();
    private int id;
    private String status;
    private int point;
    private String messageTemplate;

    public IdeaStatus(int id, String status, int point, String messageTemplate) {
        this.id = id;
        this.status = status;
        this.point = point;
        this.messageTemplate = messageTemplate;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public int getPoint() {
        return point;
    }

    public boolean isActualForExpert(){

        if(User.getInstance().getId() == DB.ANALITIK_ID)
            return id == IDEA_ACCEPTED;
        else if(User.getInstance().isExpert())
            return id == UNDER_CONSIDERING || id == RUK_DECIDED;
        else if(User.getInstance().isRuk())
            return id == SENT_TO_RUK;
        return false;
    }

    public static void pushStatus(IdeaStatus is){
        statuses.add(is);
    }

    public static ArrayList<IdeaStatus> getStatuses() {
        return statuses;
    }
}
