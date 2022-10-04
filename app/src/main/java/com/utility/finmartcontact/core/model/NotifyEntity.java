package com.utility.finmartcontact.core.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by IN-RB on 22-02-2018.
 */

public class NotifyEntity implements Parcelable {


    /**
     * notifyFlag : L
     * img_url : http://i.stack.imgur.com/CE5lz.png
     * body : this is Different Data Daniyal this is Different Data Rahul this is Different Data Umesh this is Different Data Vinit this is Different Data Nilesh this is Different Data DaRajeevniyal
     * title : PushNotification from Data120 Daniyal...
     * web_url : http://i.stack.imgur.com/CE5lz.png
     * web_title : Demo
     * message_id : 1
     */
    private String title;
    private String body;
    private String notifyFlag;
    private String web_url;
    private String web_title;
    private String message_id;



    public NotifyEntity() {

    }
    protected NotifyEntity(Parcel in) {
        title = in.readString();
        body = in.readString();
        notifyFlag = in.readString();
        web_url = in.readString();
        web_title = in.readString();
        message_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(notifyFlag);
        dest.writeString(web_url);
        dest.writeString(web_title);
        dest.writeString(message_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NotifyEntity> CREATOR = new Creator<NotifyEntity>() {
        @Override
        public NotifyEntity createFromParcel(Parcel in) {
            return new NotifyEntity(in);
        }

        @Override
        public NotifyEntity[] newArray(int size) {
            return new NotifyEntity[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getNotifyFlag() {
        return notifyFlag;
    }

    public void setNotifyFlag(String notifyFlag) {
        this.notifyFlag = notifyFlag;
    }

    public String getWeb_url() {
        return web_url;
    }

    public void setWeb_url(String web_url) {
        this.web_url = web_url;
    }

    public String getWeb_title() {
        return web_title;
    }

    public void setWeb_title(String web_title) {
        this.web_title = web_title;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }



}
