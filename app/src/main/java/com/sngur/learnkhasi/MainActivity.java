package com.sngur.learnkhasi;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.sngur.learnkhasi.ui.activity.UserSentenceListActivity;
import com.sngur.learnkhasi.ui.activity.UserWordsListActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
/*
    Proberb 16
    20. Pynleit jingmut ïa kaei ba la hikai ïa phi, bad phin ïoh manbha; shaniah ha U Trai bad phin long uba kmen.
    25. Kaei kaba phi pyrkhat ba ka long ka lynti kaba dei ka lah ban ïalam sha ka jingïap.
 */
public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_DESCRIPTION = "Learn Khasi";
    private static final CharSequence CHANNEL_NAME = "Learn Khasi App";
    public static final String CHANNEL_ID = "101";
    public static final int minVotes=10;
    private SharedPreferences preferences;
    private String gid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        preferences=getSharedPreferences("LearnKhasi", 0);
        gid=preferences.getString("gid","none");
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_learn,R.id.navigation_words,R.id.navigation_sentences,R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //load Notification onLoad of App
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        Intent intent_o = getIntent();
        //todo test and check
        if (intent_o.hasExtra("data")||intent_o.hasExtra("title") || intent_o.hasExtra("message")) {
            //bundle must contain all info sent in "data" field of the notification
            //Log.e("Message","Got some data");
            String someData = intent_o.getStringExtra("message");
            //Log.e("Message",someData+"");
//            String someMessage = intent_o.getStringExtra("message");
//            Log.d("Message",someMessage+"");
            Toast.makeText(this, "Your "+someData+" has been translated", Toast.LENGTH_SHORT).show();
            Intent intent;
            if(someData.contains("Word"))
            {
                //open User Word Activity
                intent = new Intent(this, UserWordsListActivity.class);
            }
            else
            {
                //open User Sentence Activity
                intent = new Intent(this, UserSentenceListActivity.class);
            }
            intent.putExtra("userId",gid);
            intent.putExtra("type","all");
            startActivity(intent);
        }

    }

    public static final boolean isInternetOn(Context context, Activity activity) {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)activity.getSystemService(context.CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {

            // if connected with internet

            //Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
            //Toast.makeText(context, " Check internet connectivity ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    public void watchIntroVideo(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=EIHw6PtUmoI")));
    }

    public void visitDonateSite(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://rzp.io/l/learnkhasi")));
    }
    public void learnAboutDonate(View view){
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://learnkhasi.in/donate")));
    }
}