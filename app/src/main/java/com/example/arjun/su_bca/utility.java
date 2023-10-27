package com.example.arjun.su_bca;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.SimpleDateFormat;

public class utility {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLog (String TAG, String message) {
        Log.d(TAG, message);
    }

    static CollectionReference getCollectionReferenceForNotes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("notes")
                .document(currentUser.getUid()).collection("my_notes");
    }

    @SuppressLint("SimpleDateFormat")
    static String timeStampToString(Timestamp timestamp) {
        return new SimpleDateFormat("MM/dd/yyyy").format(timestamp.toDate());
    }

    static String accessLoveCalculator(String str) {
        String[] testArray = str.split(" ");

        String name1;
        String name2;
        if(testArray.length == 3) {
            name1 = testArray[1];
            name2 = testArray[2];
        } else {
            name1 = testArray[1] + " " + testArray[2];
            name2 = testArray[3] + " " + testArray[4];
        }
        return loveCalculator(name1, name2);
    }

    static String loveCalculator(String str1, String str2) {

        String name3 = (str1 + str2).toLowerCase();

        int t = counter(name3, 't');
        int r = counter(name3, 'r');
        int u = counter(name3, 'u');
        int e = counter(name3, 'e');
        int l = counter(name3, 'l');
        int o = counter(name3, 'o');
        int v = counter(name3, 'v');

        int sum1 = t + r + u + e, sum2 = l + o + v + e;

        int love = Integer.parseInt(sum1 + "" + sum2);

        return showResult(love);
    }

    static String showResult(int love) {
        if(love < 10 || love > 90) {
            return "Your score is " + love + ", you go together like coke and mentos.";
        } else if(love >= 50) {
            return "Your score is " + love + ", you're alright together.";
        } else {
            return "Your score is " + love + ".";
        }
    }

    static int counter(String name, char x) {
        int count = 0;
        for(char character : name.toCharArray()) {
            if(character == x) {
                count++;
            }
        }
        return count;
    }

    static void setupGPTKey (Context context) {

        Thread getApiKeyThread = new Thread( () -> {
            try {
                SharedPreferences sharedPreferences = null;
                while (sharedPreferences == null) sharedPreferences = context.getSharedPreferences("App", Context.MODE_PRIVATE);

                boolean isApi = sharedPreferences.getBoolean("isChatGPTAPI", false);

                if (!isApi) {
                    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
                    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                            .setMinimumFetchIntervalInSeconds(5)
                            .build();

                    remoteConfig.setConfigSettingsAsync(configSettings);
                    SharedPreferences finalSharedPreferences = sharedPreferences;
                    remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            finalSharedPreferences.edit()
                                    .putString("ChatGPTAPI", remoteConfig.getString("ChatGPTAPI"))
                                    .putBoolean("isChatGPTAPI", true)
                                    .apply();
                        }
                    });
                }

            } catch (Exception ignored) {}

        } );

        getApiKeyThread.start();

        try {
            getApiKeyThread.join();
        } catch (Exception ignore) {}

    }

}
