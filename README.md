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

   public void sendDataToClockIn() {
      final Uri.Builder builder = new Uri.Builder()
         .scheme("clockin")
         .authority("login")
         .appendPath("oauth2")
         .appendQueryParameter("tenant", "mycompany")
         .appendQueryParameter("email", "user@mycompany.com")
         .appendQueryParameter("password", "MyP455w0rd")
         .appendQueryParameter("appScheme", "myappscheme")
         .appendQueryParameter("appName", "My App Name")
         .appendQueryParameter("appIdentifier", "com.mycompany.app");

      final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if (!isIntentSave(intent)) {
         return;
      }

      startActivity(intent);
   }

   private boolean isIntentSave(final Intent intent) {
      final PackageManager packageManager = mContext.getPackageManager();
      List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
      return activities.size() > 0;
   }
	
}
```

### Receiving data from the latest clock-in(s):

```xml
<manifest>
   <application>
      <activity android:name=".MyActivity">
         <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.LAUNCHER" />
         </intent-filter>
         <intent-filter>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <data android:scheme="myappscheme" android:host="clockin" />
         </intent-filter>
      </activity>
   </application>
</manifest>
```

```java
public final class MyActivity extends AppCompatActivity {

   @Override
   protected void onResume() {
      super.onResume();
      receiveDataFromClockIn();
   }

   private void receiveDataFromClockIn() {
      final Intent intent = getIntent();
      if (intent == null) {
         return;
      }

      final String data = mManager.receivedData(intent);
      intent.setData(null); // reset the data after handled

      if (data == null) {
         return;
      }

      final List<ClockInObject> clockIns = serializeData(data);
      
      // ...
      // handle the serialized list
      // ...
   }

   private List<ClockInObject> serializeData(@NonNull final String data) {
      final Type type = new TypeToken<List<ClockInObject>>(){}.getType();
      return new Gson().fromJson(data, type);
   }

}

public final class ClockInObject {

   @SerializedName("clockinCoordinates")
   private String clockinCoordinates;

   @SerializedName("deviceCode")
   private String deviceCode;

   @SerializedName("employeePersonId")
   private String employeePersonId;

   // ...
   // getters e setters
   // ...
	
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
