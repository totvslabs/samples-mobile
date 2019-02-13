# Linking para Android

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

   // Ação quando usuário pressiona um botão
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

### Recebendo dados da(s) última(s) batida(s):

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
      intent.setData(null); // limpar intent após opter os dados

      if (data == null) {
         return;
      }

      final List<ClockInObject> clockIns = serializeData(data);
      
      // ...
      // manipular a lista serializada
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
 
*Para obter mais detalhes sobre a implementação basta rodar o projeto.*

*Informações sobre a comunicação com o Clock-In podem ser obtidas na [Wiki](https://github.com/totvslabs/clockin-deep-linking-android/wiki).*

## Change-log

Um breve resumo de cada versão pode ser encontrado nas [releases](https://github.com/totvslabs/clockin-deep-linking-android/releases) do projeto.

## Licença de uso

```
© TOTVS Labs
```
