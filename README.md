# Deep Linking for Android

Android app created to sending/receiving data using the Deep Linking.

## Requirements

- [Android Studio](https://developer.android.com/studio)
- [Java 8](https://www.oracle.com/technetwork/pt/java/javase/downloads/index.html)

## Steps to Run

1. Install JDK 8.

2. Install Android Studio and setup an emulador.

3. Open the Android Studio and build.

## Clock-In Integration

### Sending data to login with email/password:

```java
public final class MyActivity extends AppCompatActivity {

   private void sendDataToClockIn() {
      Uri.Builder builder = new Uri.Builder()
         .scheme("clockin")
         .authority("login")
         .appendPath("oauth2")
         .appendQueryParameter("tenant", tenant)
         .appendQueryParameter("email", email)
         .appendQueryParameter("password", password)
         .appendQueryParameter("appScheme", appScheme)
         .appendQueryParameter("appName", appName)
         .appendQueryParameter("appIdentifier", appIdentifier);

      final Uri uri = builder.build();

      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      intent = intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                               | Intent.FLAG_ACTIVITY_CLEAR_TASK
                               | Intent.FLAG_ACTIVITY_NO_HISTORY);

      final PackageManager packageManager = mContext.getPackageManager();
      List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

      if (activities.size() > 0) {
         startActivity(intent);
      }
   }
   
}
```

### Receiving data from the latest clock-in(s):

```xml
<manifest>
   <application>
      <!-- ... -->

      <activity android:name=".ReceiveDataActivity"
         android:launchMode="singleTask">
         <intent-filter>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <data android:scheme="myscheme" android:host="clockin" />
         </intent-filter>
      </activity>

      <!-- ... -->
   </application>
</manifest>
```

```java
public final class ReceiveDataActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      receiveDataFromClockIn();
   }

   private void receiveDataFromClockIn(final Intent intent) {
      final Intent intent = getIntent();
      if (intent == null) {
         return null;
      }

      final Uri intentData = intent.getData();
      if (intentData == null) {
         return null;
      }

      intent.setData(null);

      final String host = intentData.getHost();
      if (host == null || !host.equals("clockin")) {
         return null;
      }

      final String clockInsStr = intentData.getQueryParameter("data");
      if (clockInsStr == null) {
         return null;
      }

      TypeToken type = new TypeToken<List<ClockInObject>>(){}.getType();
      List<ClockInObject> clockIns = new Gson().toJson(clockInsStr, type);

      // ...
    }

}

public final class ClockInObject {

   @SerializedName("name")
   private String name;

   @SerializedName("data")
   private ClockInDataObject data;
	
}

public final class ClockInDataObject {

   @SerializedName("clockinCoordinates")
   private ClockInCoordinatesObject clockinCoordinates;

   @SerializedName("clockinDatetime")
   private String clockinDatetime;

   @SerializedName("employeePersonId")
   private String employeePersonId;

   //...
}

public final class ClockInCoordinatesObject {

   @SerializedName("lat")
   private double latitude;

   @SerializedName("long")
   private double longitude;

   @SerializedName("formatted")
   private String formatted;

}
```
 
*For more implementation details just build the example.*

*Information about deep linking protocol can be found on [Wiki](https://github.com/totvslabs/clockin-deep-linking-android/wiki).*

## Change-log

A brief summary of each release can be found on the [releases](https://github.com/totvslabs/clockin-deep-linking-android/releases) do projeto.

## License

```
© TOTVS Labs
```

- - -

<p align="center">
<a href="https://github.com/totvslabs/clockin-deep-linking-android/blob/master/README_pt.md">Português</a>
</p>
