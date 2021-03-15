package com.sngur.learnkhasi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sngur.learnkhasi.ui.welcome.WelcomeFragment;
/*
    Proberb 16
        1. Ngi lah ban don ki jingthmu, hynrei khatduh ka rai ka dei jong U Blei.
        2. Phi lah ban pyrkhat ba baroh kaba phi leh ka long kaba dei, hynrei dei U Trai uba bishar ïa ki jingthmu jong phi.
        3. Kyrpad na U Trai ban kyrkhu ïa ki jingthmu jong phi, bad phin ïoh jingjop ha kaba pyntreikam ïa ki.
*/
public class WelcomeActivity extends AppCompatActivity {

    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        FirebaseAuth mAuth= FirebaseAuth.getInstance();;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            //goto Main Activity
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
        else {
            manager = getSupportFragmentManager();
            WelcomeFragment fragment = new WelcomeFragment();
            manager.beginTransaction().replace(R.id.layout_for_fragment, fragment).addToBackStack(null).commit();
        }

    }
}