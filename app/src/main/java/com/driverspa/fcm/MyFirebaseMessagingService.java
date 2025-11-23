package com.driverspa.fcm;

/**
 * Created by Yerzhan Tanatov on 08.01.2025.
 */


//public class MyFirebaseMessagingService extends FirebaseMessagingService {
public class MyFirebaseMessagingService {
//
//    private static final String TAG = "YERZHAN";
//
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        String message = remoteMessage.getData().get("message");
//        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        Log.d(TAG, "Message: " + message);
//
//        // Process the message here
//        if (message != null) {
//            sendNotification(message);
//        }
//    }
//
//    private void sendNotification(String message) {
//        // Same code as before for notifications, just ensuring FCM-specific handling
//        if(UserPreferences.isUserLoggedIn(this)) {
//            // Handle user data and process notification
//            PushData data = PushData.deserialize(message);
//
//            // Notification building logic stays the same
//            int notifyId = NotificationId.getID();
//            Intent intent = new Intent(getApplicationContext(), ClientHomeActivity.class);
//            intent.putExtra(PUSH_TYPE, data.getType());
//            intent.putExtra(PUSH_DATA, data.getText());
//            intent.putExtra(EXTRA_BOOKING_ID, data.getObjectId());
//            intent.putExtra(EXTRA_WASHER_ID, data.getObjectId());
//            intent.putExtra(Constants.EXTRA_CAMPAIGN_ID, data.getObjectId());
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            // Similar logic for different notification types
//            if (data.getType().contains("book") || data.getType().contains("serving") || data.getType().contains("express")) {
//                BA.getEventBus().post(new NewBookingPushRequestEvent(data));
//            } else if (data.getType().contains("bid")) {
//                // Handle bid-related logic
//                //handleBidNotifications(data);
//            } else if (data.getType().contains("session_end")) {
//                // Handle session end logic
//                //handleSessionEnd(data);
//            }
//
//            // Notification creation (same as before)
//            //TODO: come here and uncomment
////            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
////            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
////
////            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
////                    .setSmallIcon(R.drawable.ic_launch)
////                    .setContentTitle("DriverSpa")
////                    .setContentText(data.getText())
////                    .setAutoCancel(true)
////                    .setSound(defaultSoundUri)
////                    .setContentIntent(pendingIntent)
////                    .setStyle(new NotificationCompat.BigTextStyle().bigText(data.getText()));
////
////            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            //TODO: come here and uncomment
////            notificationManager.notify(notifyId, notificationBuilder.build());
//        }
//    }
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Default Channel";
//            String description = "Channel for DriverSpa notifications";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            //TODO: come here and uncomment
////            NotificationChannel channel = new NotificationChannel("default", name, importance);
////            channel.setDescription(description);
//
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
////            notificationManager.createNotificationChannel(channel);
//        }
//    }
//
//    @Override
//    public void onNewToken(String token) {
//        Log.d(TAG, "Refreshed token: " + token);
//        // Send the token to your server if needed
//    }
}
