package com.alpari.avia_kos.alpidea.models;

import java.util.ArrayList;

/**
 * Created by Avia-Kos on 05.05.16.
 */
public class User {

    private int id;
    private String login;
    private String name;
    private String telephone;
    private String email;
    private ArrayList<Idea> ideas = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private boolean isExpert = false;
    private boolean isRuk = false;
    private int currentPoints;
    private static User ourInstance = new User();
    public static User getInstance() {
        return ourInstance;
    }

    private User() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<Idea> getIdeas() {
        return ideas;
    }

    public void setIdeas(ArrayList<Idea> ideas) {
        this.ideas = ideas;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public int newMessagesCount(){
        int count = 0;
        for (Message message: messages) {
            if(!message.isRead() && message.getTo().equals(this.getLogin())) count++;
        }
        return count;
    }

    public int newIdeasCount(){
        int count = 0;
        for (Idea idea: ideas) {
            if(idea.getStatus().isActualForExpert())
                count++;
        }
        return count;
    }

    public boolean isExpert() {
        return isExpert;
    }

    public void setExpert(boolean expert) {
        isExpert = expert;
    }

    public boolean isRuk() {
        return isRuk;
    }

    public void setRuk(boolean ruk) {
        isRuk = ruk;
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
    }

    public void exit(){
        ourInstance = new User();
    }
}
