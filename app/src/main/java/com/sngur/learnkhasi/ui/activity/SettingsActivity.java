package com.sngur.learnkhasi.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sngur.learnkhasi.databinding.ActivitySettingsBinding;
import com.sngur.learnkhasi.model.User;
import com.sngur.learnkhasi.model.room.Editors;
import com.sngur.learnkhasi.model.room.StoredEnglishWord;
import com.sngur.learnkhasi.model.room.StoredKhasiWord;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.EditorsDao;
import com.sngur.learnkhasi.roomdb.dao.StoredEnglishWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredKhasiWordDao;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private String topic,gid,safe,currentUserId;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int level,noOfWords,noOfKhasiWords;
    private DatabaseReference khasiWordsRef,englishWordsRef,myRef;
    private static LearnKhasiDatabase db;
    private FirebaseDatabase database;
    private int index;
    private List<Editors> editorsList;
    private int displayLimit;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);
        binding=ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        topic="";
        preferences =getSharedPreferences("LearnKhasi", 0);
        editor=preferences.edit();
        topic=preferences.getString("gid","none");
        level=preferences.getInt("level",0);
        gid=preferences.getString("gid","none");
        noOfKhasiWords=preferences.getInt("noOfKhasiWords",0);
        safe=preferences.getString("safe","on");
        currentUserId=preferences.getString("gid","none");
        displayLimit=preferences.getInt("displayLimit",15);
        noOfKhasiWords=6000;//version 1.0.0
        noOfWords=0;
        index=0; // to get updated count
        db=LearnKhasiDatabase.getDatabase(SettingsActivity.this);
        database = FirebaseDatabase.getInstance();
        countNoOfWords();
//        Log.e("WORDS",noOfKhasiWords+" - ");
        binding.notificationOn.setClickable(false);
        binding.notificationOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.notificationOn.isChecked())
                {
                    //Snackbar.make(binding.getRoot(),"Notifications cannot be turned off in this version", BaseTransientBottomBar.LENGTH_SHORT).show();
//                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            String msg = "Login Successfully";//+topic;
//                            if (!task.isSuccessful()) {
//                                msg = "You are now Logged in";
//                            }
//                            //Log.e("topic", msg);
//                            Snackbar snackbar=Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT);
//                            snackbar.show();
//                        }
//                    });
                    binding.notificationOn.setChecked(true);
                    binding.notificationOn.setEnabled(true);
                }
                else {
                    Snackbar.make(binding.getRoot(), "Notifications cannot be turned off in this version", BaseTransientBottomBar.LENGTH_SHORT).show();
                    binding.notificationOn.setChecked(true);
                    binding.notificationOn.setEnabled(true);
                }
            }
        });

        if(safe.equals("off"))
        {
            binding.safeSearch.setChecked(false);
//            binding.safeSearch.setEnabled(false);
        }
        else{
            binding.safeSearch.setChecked(true);
//            binding.safeSearch.setEnabled(true);
        }

        binding.safeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.safeSearch.isChecked())
                {
                    //turn off safe search
                    editor.putString("safe","on");
                    Toast.makeText(SettingsActivity.this, "Safe Search is now on", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //turn on safe search
                    editor.putString("safe","off");
                    Toast.makeText(SettingsActivity.this, "Safe Search is now off", Toast.LENGTH_SHORT).show();
                }
                editor.apply();
                editor.commit();
            }
        });

        //setting limit for Editors only
        editorsList=new ArrayList<>();
        loadEditors();

        binding.terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://learnkhasi.in/terms")));
            }
        });

        binding.privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://learnkhasi.in/privacy")));
            }
        });

        binding.donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://learnkhasi.in/donate")));
            }
        });

        //sponsors
        binding.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show AlertDialog
                AlertDialog.Builder builder=new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Download");
                builder.setMessage("Offline Dictionary is available to sponsors only for now");
                builder.setCancelable(true);
                builder.setPositiveButton("Learn more", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://learnkhasi.in/donate")));
                    }
                });
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(level>=4)
                            checkIfSponsor();
                        else
                            Snackbar.make(binding.getRoot(),"Press Learn more to become a sponsor today.", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }
        });
    }

    private void loadEditors() {
        class loadEditors extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                EditorsDao editorsDao = db.editorsDao();
                editorsList = editorsDao.getAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                final Editors editorId=new Editors(currentUserId);
                Boolean found=false;
                for(Editors user: editorsList)
                {
                    if(user.getUserId().equals(currentUserId)) {
                        found = true;
                        break;
                    }
                }
                if(found){
                    binding.displayLimit.setVisibility(View.VISIBLE);
                    binding.limitNo.setText(displayLimit+"");
                    binding.setLimit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int limit;
                            if(binding.limitNo.getText().toString().equals(""))
                            {
                                limit=15;
                            }
                            else
                            {
                                try {
                                    limit = Integer.parseInt(binding.limitNo.getText().toString());
                                }catch (Exception e){
                                    limit=15;
                                }
                            }
                            editor.putInt("displayLimit",limit);
                            editor.commit();
                            editor.apply();
                            Toast.makeText(SettingsActivity.this, "Display Limit Set", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
        new loadEditors().execute();
    }

    private void checkIfSponsor() {
//        Log.e("WORDS",noOfKhasiWords+ " - "+noOfWords);
        if(noOfWords<noOfKhasiWords) {
            //check online if sponsor
            myRef=database.getReference();
            myRef.child("users").child(gid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        User userData = snapshot.getValue(User.class);
                        level=userData.getLevelId();
                        if(level>=4)
                            downloadData();
                        else
                            Snackbar.make(binding.getRoot(),"Unable to download dictionary offline", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else
            Snackbar.make(binding.getRoot(),"You have downloaded the latest dictionary offline", BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    private void downloadData() {
        Snackbar.make(binding.getRoot(),"Downloading the latest words in the background. Do not close this screen.", BaseTransientBottomBar.LENGTH_SHORT).show();
        //load all words with no limit - as of now limit to 7300 : 10MB of data
        khasiWordsRef=database.getReference();
        englishWordsRef=database.getReference();
        //display progress dialog with no exit
        progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setTitle("Downloading... Please wait.");
        progressDialog.setMessage("This window will close once download has completed successfully.");
        progressDialog.show();
        progressDialog.setCancelable(false);

        khasiWordsRef.child("khasi_word").limitToFirst(7300).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //load data to sql
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    StoredKhasiWord wordFound = postSnapshot.getValue(StoredKhasiWord.class);
                    storeWordToDB(null,wordFound,"Khasi");
                }
                Snackbar.make(binding.getRoot(),"Downloaded Khasi Words 90% completed. Please stay connected to the internet as we verify the data.", BaseTransientBottomBar.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        englishWordsRef.child("english_word").limitToFirst(7300).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    StoredEnglishWord wordFound = postSnapshot.getValue(StoredEnglishWord.class);
                    storeWordToDB(wordFound,null,"English");
                }
                //enable the alert dialog
                progressDialog.setCancelable(true);
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Snackbar.make(binding.getRoot(),"Downloaded English Words 90% completed. Please stay connected to the internet as we verify the data.", BaseTransientBottomBar.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.setCancelable(true);
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    private void storeWordToDB(final StoredEnglishWord storedEnglishWord,final StoredKhasiWord storedKhasiWord, final String fromLang) {
        class storeWords extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if(fromLang.equalsIgnoreCase("English"))
                {
                    StoredEnglishWordDao storedEnglishWordDao=db.storedEnglishWordDao();
                    if(storedEnglishWord.getWordId()!=null)
                        storedEnglishWordDao.insert(storedEnglishWord);
                }
                else{
                    StoredKhasiWordDao storedKhasiWordDao=db.storedKhasiWordDao();
                    if(storedKhasiWord.getWordId()!=null)
                        storedKhasiWordDao.insert(storedKhasiWord);
                    index++;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(index==6000) {
                    countNoOfWords();
                    index++;
                }
            }
        }
        storeWords storeWords=new storeWords();
        storeWords.execute();
    }

    private void countNoOfWords() {
        class countWords extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                StoredKhasiWordDao storedKhasiWordDao=db.storedKhasiWordDao();
                noOfWords=storedKhasiWordDao.getCount();
                return null;
            }
        }
        new countWords().execute();
    }

}