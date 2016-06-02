package com.alpari.avia_kos.alpidea.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Avia-Kos on 18.05.16.
 */
public class Message {
    private int id;
    private String content;
    private String from;
    private String to;
    private String date;
    private boolean read;

    public Message(int id, String content, String from, String to, String date, boolean read) {
        this.id = id;
        this.content = content;
        this.from = from;
        this.to = to;
        this.date = dateConvert(date);
        this.read = read;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDate() {
        return date;
    }

    public boolean isRead() {
        return read;
    }

    public String dateConvert(String D){
        D = D.substring(0, D.length() - 2);
        SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat formattedDate = new SimpleDateFormat("dd.MM", Locale.ENGLISH);
        Date dateTime = null;
        Date date = null;
        Date currentDate = null;
        String time = "";
        String formattedOutput;
        try {
            //currentDate = format3.parse(format3.format(Calendar.getInstance().getTime()));
            currentDate = formatDate.parse(formatDate.format(Calendar.getInstance().getTime()));
            dateTime = formatDateTime.parse(D);
            date = formatDate.parse(formatDateTime.format(dateTime));
            time = formatTime.format(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date.compareTo(currentDate) == 0){
            formattedOutput = "сегодня ";
        }
        else {
            long diff = currentDate.getTime() - date.getTime();
            diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            if(diff == 1)
                formattedOutput = "вчера ";
            else if(diff < 7)
                formattedOutput = diff + " д. назад ";
            else
                formattedOutput = formattedDate.format(date);
        }

        String dateString = formattedOutput + " в " + time;
        return dateString;
    }
}
