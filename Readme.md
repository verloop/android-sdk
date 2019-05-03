# Verloop Android SDK

---

## How to install

Copy paste the AAR file in your project and import it like any other library. You can follow this tutorial to find out how to go about it - https://medium.com/@notestomyself/how-to-include-external-aar-file-using-gradle-6604b378e808

## Usage

In your `MainActivity` (or any of the activity), you need to initialize an object of `Verloop` class. It accepts a `VerloopConfig` object as an argument for initializing.


```java
VerloopConfig config = new VerloopConfig("YOUR-CLIENT-ID", "USER-ID");


// Needed if you want to receive Notifications.
config.setFcmToken("FCMTOKEN-FOR-DEVICE");

// Optional; for testing.
config.setStaging(true);

Verloop verloop = new Verloop(getApplicationContext(), config);

```

If you don't have a user ID, or have an anonymous user, simply leave the field and our SDK will take care of it.

```java
VerloopConfig config = new VerloopConfig("YOUR-CLIENT-ID");
```


And when the user clicks on "Support" button, you simply need to call


```java
verloop.showChat();
```

and the chat activity will start.

To let Verloop handle Notifications, simply add this line in your `FirebaseMessagingService` class

```java
@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
    int icon = R.drawable.notification_image; // set a drawable to use as icon for notifications
    VerloopNotification.showNotification(this, icon, remoteMessage.getData()); // This will be auto-ignored if notification is not from Verloop.

    // Do anything else with your message.
}
```

# User session management

When the user logs out of your app, make sure to call

```java
verloop.logout();
```

to remove their session. If they login again, you can use this to log them in

```java
verloop.login("USER-ID", "FCMTOKEN-FOR-DEVICE");

// or if you don't have the FCM token, you can simply do

verloop.login("USER-ID");
```