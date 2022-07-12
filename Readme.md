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
          implementation 'com.github.verloop:android-sdk:1.1.4'
  }
```

### If you are using proguard in android add the following

```
-keepattributes *Annotation*
```


## Usage

In your `MainActivity` (or any of the activity), you need to initialize an object of `Verloop` class. It accepts a `VerloopConfig` object as an argument for initializing.


```kotlin
// Exhaustive list of values which you can put while creating config
var config = VerloopConfig.Builder()
    .clientId("CLIENT_ID")              // Required: this would be your account name associated with verloop. eg: <client_id>.verloop.io
    .userId("USER_ID")                  // If user is logged in, and if you want to associate older chats, else, skip this for anonymous user 
    .fcmToken("FCMTOKEN_FOR_DEVICE")    // If you wish to get notifications, else, skip this
    .recipeId("RECIPE_ID")              // In case you want to use default recipe, skip this
    .userName("USER_NAME")              // If guest name variable is a part of the recipe, or the value is not required, skip this
    .userEmail("USER_EMAIL")            // If email variable is a part of the recipe, or the value is not required, skip this
    .userPhone("USER_PHONE")            // If phone variable is a part of the recipe, or the value is not required, skip this
    .isStaging(false)                   // Keep this as true if you want to access <client_id>.stage.verloop.io account. If the account doesn't exist, keep it as false or skip it
    .fields(customFields)               // These are predefined variables added on room level or user level
    .build()                            // this would build the final config object which is later used by Verloop object to star the chat

val verloop = Verloop(this, config); // `this` here refers to an activity context.

```

If you don't have a user ID, or have an anonymous user, simply leave the field and our SDK will take care of it.

```kotlin
val config = VerloopConfig.Builder()("YOUR-CLIENT-ID").build();
```


And when the user clicks on "Support" button, you simply need to call
```kotlin
verloop.showChat();
```
and the chat activity will start.


### User Properties

To set a user properties `name`, `email`, and `phone`, you can directly call the method in `VerloopConfig` object.

```kotlin
config.userName("Name").build()
config.userEmail("Email").build()
config.userPhone("Phone").build()
```

### Manual recipe ID override

Default recipe will run during the conversation unless we do a manual recipe override by calling the following method:
```kotlin
config.recipeId("<Recipe ID>").build()
```

### Custom variables

You can send custom details of a customer with conversation scope or at global scope.

Custom fields set on the conversation scope will have context associated with only that particular conversation. Once the conversation is over, you can set different values for a new conversation for the same customer. Use the following code to set the value:
```kotlin
config.putCustomField("Test Field", "Test Value");
```
OR
```kotlin
config.putCustomField("Test Field", "Test Value", Scope.ROOM);
```

Global scope variables are associated with customer and not on a conversation. Something like name, email etc. do not change on different conversation of the same user. To set the value for global scope variables:

```kotlin
config.putCustomField("Test Field", "Test Value", Scope.USER);
```


### Notification
This will work only after you have added FCM token at the time of creation of the verloop config object
```kotlin
config.fcmToken("FCMTOKEN-FOR-DEVICE").build()
```

Now, to let Verloop handle Notifications, simply add this line in your `FirebaseMessagingService` class

```kotlin
@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
    int icon = R.drawable.notification_image; // set a drawable to use as icon for notifications
    VerloopNotification.showNotification(this, icon, remoteMessage.getData()); // This will be auto-ignored if notification is not from Verloop.

    // Do anything else with your message.
}
```

### Handle Notification click

In your Launcher activity override onNewIntent as given below.

```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    if (intent != null && intent.extras != null) {
        val verloopData = intent.extras?.get("verloop")
        if (verloopData != null) {
            val obj = JSONObject(verloopData.toString())
            if (obj.has("client_id")) clientId = obj.getString("client_id")
            if (obj.has("userId")) userId = obj.getString("userId")
        }
        if (clientId === null) clientId = intent.extras?.get("clientId") as String?
        if (userId === null) userId = intent.extras?.get("userId") as String?
        if (clientId != null) {
            verloop?.showChat()	
        }
    }
}
```


### Button click listener
At the time of having the conversation, bot shows button for faster replies. Button click listeners can be added using:
```kotlin
config?.setButtonClickListener(object : LiveChatButtonClickListener {
    override fun buttonClicked(title: String?, type: String?, payload: String?) {
        // Add the app logic for button click
    }
})
```

### URL click listener
If the user clicks on any URL provided by the bot or by the agent, then you can listen to the URL and take the action in the app. Actions can be like re-routing based on the product link etc. URL click listeners can be added using:
`overrideUrlClick`: keep it as true if you want to handle the url internally in your app. Else keep it false if you want to open a browser when url is clicked.
```kotlin
config?.setUrlClickListener(object : LiveChatUrlClickListener{
    override fun buttonClicked(url: String?) {
        // Add the app logic for url click
    }
}, overrideUrlClick)
```

# User session management

When the user logs out of your app, make sure to call

```kotlin
verloop.logout();
```
