/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.hybridApp.push;

import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import kr.co.hybridApp.R;
import kr.co.hybridApp.util.NotificationUtil;


public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // 푸시를 받을 경우 해당 클래스를 통해 Notification 을 호출 한다. receive_message
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "getBody: " + remoteMessage.getNotification().getBody());

        //Log.d(TAG, "Notification Message TITLE: " + remoteMessage.getNotification().getBody());
        Log.d(TAG, "Notification Message TITLE: " + remoteMessage.getData().get("title"));
        Log.d(TAG, "Notification Message BODY: " + remoteMessage.getData().get("body"));
        Log.d(TAG, "Notification Message URL: " + remoteMessage.getData().get("url"));
        Log.d(TAG, "Notification Message DATA: " + remoteMessage.getData().toString());
        String url = null;
        String title = null;
        String body = null;
        Map<String, String> data = remoteMessage.getData();
        title = data.get("title");
        body = data.get("body");
        url = data.get("url");

        Log.d(TAG, "title: " + title);
        Log.d(TAG, "body: " + body);
        Log.d(TAG, "url: " + url);
        //if(remoteMessage.getData().containsKey("url")){
        //    url = remoteMessage.getData().get("url");
        //}
        

        // 푸시 메세지에 값이 있을 경우.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // 무조건 notification 을 던진다.
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if(data != null){
        //if (remoteMessage.getNotification() != null) {
            //Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //Map<String, String> data = remoteMessage.getData();

            //sendNotification(data.get("message"),data.get("title"));
            //url = remoteMessage.getData().get("url");

            //sendNotification(remoteMessage.getNotification().getBody(), url);
            sendNotification(body, url);
        }

        //sendNotification(remoteMessage.getNotification().getTitle(),
        //        remoteMessage.getNotification().getBody(), remoteMessage.getData());

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     *
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
       // sendNotification("1234");
    }

    /**
     * FCM 메세지 전달
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, String url) {
        NotificationUtil.show(this, getResources().getString(R.string.app_name), messageBody, "새 메시지가 도착했습니다", "PUSH", url);
    }


    //public static void  show(Context context, String title, String text, String ticker, String msg_type, String param) {

    private void sendNotification(String messageTitle, String messageBody, Map<String, String> row) {
        String url = null;
        if(row.containsKey("url")) {
            url = row.get("url");
        }
        NotificationUtil.show(this, getResources().getString(R.string.app_name), messageTitle, messageBody, "PUSH", url);
    }

}