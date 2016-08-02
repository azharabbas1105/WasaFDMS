package com.ingentive.wasafdms.activeandroid;

import android.graphics.drawable.Drawable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by PC on 28-07-2016.
 */
@Table(name = "SmsTable")
public class SmsTable extends Model {

    @Column(name = "phone_number")
    public String phoneNumber;

    @Column(name = "message")
    public String message;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageDateTime() {
        return messageDateTime;
    }

    public void setMessageDateTime(String messageDateTime) {
        this.messageDateTime = messageDateTime;
    }

    @Column(name = "message_date_time")
    public String messageDateTime;


    public SmsTable(){
        super();
        this.phoneNumber="";
        this.message="";
        this.messageDateTime="";
    }
}
