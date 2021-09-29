# Verloop Android SDK

---

## How to install

Add JitPack in your project's gradle file :

```
  allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
  }
```

Then add the Verloop's dependency in your app's gradle file

```
  dependencies {
          implementation 'com.github.verloop:android-sdk:1.0.8'
  }
```

### If you are using proguard in android add the following

```
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
```


## Usage

In your `MainActivity` (or any of the activity), you need to initialize an object of `Verloop` class. It accepts a `VerloopConfig` object as an argument for initializing.


```java
VerloopConfig config = new VerloopConfig("YOUR-CLIENT-ID", "USER-ID");


// Needed if you want to receive Notifications.
config.setFcmToken("FCMTOKEN-FOR-DEVICE");

// Optional; for testing.
config.setStaging(true);

Verloop verloop = new Verloop(this, config); // `this` here refers to an activity context.

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


### User Properties

To set a user properties `name`, `email`, and `phone`, you can directly call the method in `VerloopConfig` object.

```java
config.setUserName("Name");
config.setUserEmail("Email");
config.setUserPhone("Phone");
```

### Manual recipe ID override

Default recipe will run during the conversation unless we do a manual recipe override by calling the following method:
```
config.setRecipeId("<Recipe ID>");
```

### Custom variables

You can send custom details of a customer with conversation scope or at global scope.

Custom fields set on the conversation scope will have context associated with only that particular conversation. Once the conversation is over, you can set different values for a new conversation for the same customer. Use the following code to set the value:
```
config.putCustomField("Test Field", "Test Value");
```
OR
```
config.putCustomField("Test Field", "Test Value", Scope.ROOM);
```

Global scope variables are associated with customer and not on a conversation. Something like name, email etc. do not change on different conversation of the same user. To set the value for global scope variables:

```
config.putCustomField("Test Field", "Test Value", Scope.USER);
```


### Notification
This will work only after you have added FCM token at the time of creation of the verloop config object
```
config.setFcmToken("FCMTOKEN-FOR-DEVICE");
```

Now, to let Verloop handle Notifications, simply add this line in your `FirebaseMessagingService` class

```java
@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
    int icon = R.drawable.notification_image; // set a drawable to use as icon for notifications
    VerloopNotification.showNotification(this, icon, remoteMessage.getData()); // This will be auto-ignored if notification is not from Verloop.

    // Do anything else with your message.
}
```

### Button click listener
At the time of having the conversation, bot shows button for faster replies. Button click listeners can be added using:
```
config.setButtonOnClickListener((title, type, payload) -> {
  // Add the app logic for button click
});
```

### URL click listener
If the user clicks on any URL provided by the bot or by the agent, then you can listen to the URL and take the action in the app. Actions can be like re-routing based on the product link etc. URL click listeners can be added using:
```
config.setUrlClickListener((url) -> {
  // Add the app logic for URL click
});
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
