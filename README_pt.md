# Deep Linking para Android

Aplicativo Android criado para simular o envio/recebimento de dados fazendo uso do Deep Linking.

## Requisitos

- [Android Studio](https://developer.android.com/studio)
- [Java 8](https://www.oracle.com/technetwork/pt/java/javase/downloads/index.html)

## Configuração do ambiente

1. Instale o JDK 8.

2. Instale o Android Studio e configure um emulador.

3. No Android Studio, abra e rode o projeto.

## Integração com o Clock-In

### Enviando dados para realizar o login com e-mail/senha:

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
      final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

      if (activities.size() > 0) {
         startActivity(intent);
      }
   }
	
}
```

### Recebendo dados da(s) última(s) batida(s):

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

public final class ClockInDataObject {

   @SerializedName("clockinCoordinates")
   private String clockinCoordinates;

   @SerializedName("clockinDatetime")
   private String clockinDatetime;

   @SerializedName("employeePersonId")
   private String employeePersonId;

   //...
}
```
 
*Para obter mais detalhes sobre a implementação basta rodar o projeto.*

*Informações sobre a comunicação com o Clock-In podem ser obtidas na [Wiki](https://github.com/totvslabs/clockin-deep-linking-android/wiki).*

## Change-log

Um breve resumo de cada versão pode ser encontrado nas [releases](https://github.com/totvslabs/clockin-deep-linking-android/releases) do projeto.

## Licença de uso

```
© TOTVS Labs
```

- - -

<p align="center">
<a href="https://github.com/totvslabs/clockin-deep-linking-android/blob/master/README.md">English</a>
</p>
