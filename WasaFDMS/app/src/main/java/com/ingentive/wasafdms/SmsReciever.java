package com.ingentive.wasafdms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.ingentive.wasafdms.activeandroid.SmsTable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SNIHostName;

/**
 * Created by PC on 30-07-2016.
 */
public class SmsReciever extends BroadcastReceiver {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;


    private String urlSendMessage = "http://yourbrand.pk/wasafdms/services/save_message_api";
    private int conn = 0;
    private String phoneNumber = "";
    private String message = "";
    private String dateTime = "";
    SmsTable smsTable;

    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    phoneNumber = currentMessage.getDisplayOriginatingAddress();
                   //phoneNumber = "03466969193"; this no registered
                    message = currentMessage.getDisplayMessageBody();
                    Log.d("SmsReceiver", "senderNum: " + phoneNumber + "; message: " + message);
                }
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTime = df.format(c.getTime());
                Log.d("", "" + dateTime);

                smsTable = new SmsTable();
                smsTable.message = message;
                smsTable.phoneNumber = phoneNumber;
                smsTable.messageDateTime = dateTime;

                smsTable.setMessage(message);
                smsTable.setPhoneNumber(phoneNumber);
                smsTable.setMessageDateTime(dateTime);
                smsTable.save();

                conn = getConnectivityStatus(context);
                if (isOnline(context) == true && conn == TYPE_MOBILE || conn == TYPE_WIFI) {
                    List<SmsTable> smsTableList = new ArrayList<SmsTable>();
                    smsTableList = new Select().from(SmsTable.class).execute();
                    if (smsTableList.size() > 0) {
                        for (int i = 0; i < smsTableList.size(); i++) {
                            new sendMessage(smsTableList.get(i)).execute();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    private class sendMessage extends AsyncTask<Void, Void, Void> {

        private SmsTable smsTable;

        private sendMessage(SmsTable model) {
            smsTable = new SmsTable();
            this.smsTable = model;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair("phone_number", smsTable.getPhoneNumber() + "")); //
            params.add(new BasicNameValuePair("message", smsTable.getMessage() + ""));
            params.add(new BasicNameValuePair("message_date", smsTable.getMessageDateTime() + ""));

            ServiceHandler sh = new ServiceHandler();
            String jsonStr = sh.makeServiceCall(urlSendMessage, ServiceHandler.POST, params);
            android.util.Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null && jsonStr.equals("Success")) {
                SmsTable model = new SmsTable();
                model = new Select().from(SmsTable.class).where("message_date_time=?", smsTable.getMessageDateTime()).executeSingle();
                if (model != null) {
                    new Delete().from(SmsTable.class).where("message_date_time=?", smsTable.getMessageDateTime()).execute();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    public static boolean isOnline(Context context) {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

    }
}