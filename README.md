# facial-recognition-test
A simple facial recognition app using [Kairos Android SDK](https://github.com/kairosinc/Kairos-SDK-Android)

### Features (so far):
- Let the user register (enroll) her/his face, and if needed, 
register multiple times to increase recognition accuracy;
> Only one admin is supported so far.
- If the admin changes, the user can reset the admin data and register new faces;
- Access button can take the user to the Secret screen, only when the registered admin's face is detected.
- Toast messages will show to give users alerts/notifications.

### How to Use:
- The app uses Kairos SDK(See instructions [here](https://github.com/kairosinc/Kairos-SDK-Android))
- You need to register [here](https://www.kairos.com/signup) to get your app_id and api_key, and put them in MainActivity.java:
```
myKairos = new Kairos();

String app_id = "app_id"; // replace the "app_id" with your own app_id
String api_key = "api_key"; // replace the "app_id" with your own api_key

myKairos.setAuthentication(this, app_id, api_key);
```