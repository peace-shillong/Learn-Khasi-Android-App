package com.sngur.learnkhasi.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.FragmentHomeBinding;
import com.sngur.learnkhasi.model.Counter;
import com.sngur.learnkhasi.model.room.FavoriteWord;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.ui.activity.FavoriteWordActivity;
import com.sngur.learnkhasi.ui.activity.SettingsActivity;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import static com.sngur.learnkhasi.MainActivity.isInternetOn;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private int noOfKhasiWords,noOfEnglishWords,noOfLessons,noOfKhasiSentences,noOfRegisteredUsers,noOfFavWords,noOfGaroWords,appVersionCode;
    private DatabaseReference myRef,imgRef;
    private FirebaseAuth mAuth;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private LearnKhasiDatabase db;
    private String topic,subscribed;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //getActivity().setTitle(R.string.title_home);
        //View root = inflater.inflate(R.layout.fragment_home, container, false);
        binding=FragmentHomeBinding.inflate(getLayoutInflater(),container,false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef=database.getReference("status");
        mAuth = FirebaseAuth.getInstance();
        preferences =getActivity().getSharedPreferences("LearnKhasi", 0);
        editor=preferences.edit();
        editor.commit();
        editor.apply();
        appVersionCode=3; // todo change this
        if(isInternetOn(getActivity().getApplicationContext(),getActivity())) {
            getCount();
        }
        else {
            //Snackbar snackbar=Snackbar.make(binding.getRoot(), "Check your internet connectivity", Snackbar.LENGTH_SHORT);
            //snackbar.show();
            Toast.makeText(getContext(), "Check your Internet connectivity", Toast.LENGTH_SHORT).show();
            loadDataOffline();
        }
        noOfFavWords=0;
        db=LearnKhasiDatabase.getDatabase(getContext());
        //if day is Friday-Sunday display donateHome
        Calendar c1  =Calendar.getInstance();
        if (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c1.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            binding.donateHome.setVisibility(View.VISIBLE);

        //Subscribe to Topic if not subscribed
        subscribed=preferences.getString("subscribed","none");
        topic=preferences.getString("gid","none");
        if(!topic.equals("none") && subscribed.equals("none")) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    String msg = "Login Successfully";//+topic;
                    if (!task.isSuccessful()) {
                        msg = "You are now Logged in";
                    }
                    //Log.d("topic", msg);
                    editor.putString("subscribed","yes");
                    editor.commit();
                    editor.apply();
                    try {
                        Snackbar snackbar = Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }catch (Exception e)
                    {}
                }
            });
        }

        binding.favWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), FavoriteWordActivity.class));
            }
        });

        binding.viewFavWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), FavoriteWordActivity.class));
            }
        });

        //Toast for donating to app
        binding.englishWordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Words");
            }
        });
        binding.usersCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Users");
            }
        });
        binding.lessonsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Lessons");
            }
        });
        binding.khasiWordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Words");
            }
        });
        binding.sentencesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Sentences");
            }
        });
        binding.editors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMessage("Editors");
            }
        });

        //get quickTip Image
        imgRef=FirebaseDatabase.getInstance().getReference("status");
        imgRef.child("tips").child("quickTip").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String img=snapshot.getValue(String.class);
                if(!img.equals("")) {
                    binding.quickTipText.setVisibility(View.VISIBLE);
                    binding.quickTipImg.setVisibility(View.VISIBLE);
                    Picasso.get().load(img).placeholder(R.color.colorPrimary).into(binding.quickTipImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }

    private void showMessage(final String type) {
        String msg="";
        switch (type){
            case "Words":
                msg="The number of words are updated regularly. Help us make this a live counter and updated daily by becoming a sponsor";
                break;
            case "Users":
                msg="The number of users are updated regularly. Help us make this a live counter and updated daily by becoming a sponsor";
                break;
            case "Lessons":
                msg="Lessons are added on a need basis. Become a sponsor and support us in making new lessons.";
                break;
            case "Sentences":
                msg="The number of sentences are updated regularly. Help us make this a live counter and updated daily by becoming a sponsor";
                break;
            case "Editors":
                msg="We don't have any editors yet. We will update our editors list later in our website.";
                break;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Info");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Learn more", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(type.equals("Editors"))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://learnkhasi.in/features")));
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://learnkhasi.in/donate")));
            }
        });
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    Snackbar.make(binding.getRoot(),"Press Learn more to become a sponsor today.", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void loadDataOffline() {
        noOfKhasiWords=preferences.getInt("noOfKhasiWords",0);
        noOfEnglishWords=preferences.getInt("noOfEnglishWords",0);
        noOfKhasiSentences=preferences.getInt("noOfKhasiSentences",0);
        noOfLessons=preferences.getInt("noOfLessons",0);
        noOfRegisteredUsers=preferences.getInt("noOfRegisteredUsers",0);
        noOfGaroWords=preferences.getInt("noOfGaroWords",0);
//        Log.e("DATA","LOAD IN PREF "+noOfKhasiWords);
        binding.noOfKhasiWords.setText(noOfKhasiWords+" Khasi words");
        binding.noOfEnglishWords.setText(noOfEnglishWords+" English words asked by users like you.");
        binding.noOfKhasiSentences.setText(noOfKhasiSentences+" Sentences translated by users like you.");
        binding.noOfLessons.setText(noOfLessons+" Lessons");
        binding.noOfUsers.setText(noOfRegisteredUsers+" Registered Users");
        binding.noOfGaroWords.setText(noOfGaroWords+" Garo words");
    }

    private void getCount() {

        //This loads all the data from db - and takes up bandwidth
//        myRef.child("khasi_word").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                noOfKhasiWords = (int) snapshot.getChildrenCount();
//                binding.noOfKhasiWords.setText(noOfKhasiWords+" Khasi words");
//                editor.putInt("noOfKhasiWords",noOfKhasiWords);
//                editor.commit();
//                editor.apply();
//                Log.e("DATA","STORED IN PREF "+noOfKhasiWords);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//
//
//        myRef.child("english_word").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                noOfEnglishWords = (int) snapshot.getChildrenCount();
//                binding.noOfEnglishWords.setText(noOfEnglishWords+" English words asked by users like you.");
//                editor.putInt("noOfEnglishWords",noOfEnglishWords);
//                editor.commit();
//                editor.apply();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//
//
//        myRef.child("khasi_sentence").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                noOfKhasiSentences = (int) snapshot.getChildrenCount();
//                binding.noOfKhasiSentences.setText(noOfKhasiSentences+" Sentences translated by users like you.");
//                editor.putInt("noOfKhasiSentences",noOfKhasiSentences);
//                editor.commit();
//                editor.apply();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//        myRef.child("users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                noOfRegisteredUsers = (int) snapshot.getChildrenCount();
//                //Log.d("DATA",noOfRegisteredUsers+" users "+snapshot.getChildrenCount());
//                binding.noOfUsers.setText(noOfRegisteredUsers+" Registered Users");
//                editor.putInt("noOfRegisteredUsers",noOfRegisteredUsers);
//                editor.commit();
//                editor.apply();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//        myRef.child("lessons").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                noOfLessons = (int) snapshot.getChildrenCount();
//                binding.noOfLessons.setText(noOfLessons+" Lessons");
//                editor.putInt("noOfLessons",noOfLessons);
//                editor.commit();
//                editor.apply();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        //refer to toWordRef.child(toWordId).addValueEventListener in WordViewActivity
        myRef.child("counter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Counter counter=snapshot.getValue(Counter.class);
                noOfEnglishWords=counter.getNoOfEnglishWords();
                noOfKhasiSentences=counter.getNoOfTranslatedSentences();
                noOfKhasiWords=counter.getNoOfKhasiWords();
                noOfRegisteredUsers=counter.getNoOfUsers();
                noOfLessons=counter.getNoOfLessons();
                noOfGaroWords=counter.getNoOfGaroWords();
                binding.noOfKhasiWords.setText(noOfKhasiWords+" Khasi words");
                binding.noOfLessons.setText(noOfLessons+" Lessons");
                binding.noOfEnglishWords.setText(noOfEnglishWords+" English words asked by users like you.");
                binding.noOfKhasiSentences.setText(noOfKhasiSentences+" Sentences translated by users like you.");
                binding.noOfUsers.setText(noOfRegisteredUsers+" Registered Users");
                binding.noOfGaroWords.setText(noOfGaroWords+" Garo words");

                editor.putInt("noOfRegisteredUsers",noOfRegisteredUsers);
                editor.putInt("noOfKhasiSentences",noOfKhasiSentences);
                editor.putInt("noOfEnglishWords",noOfEnglishWords);
                editor.putInt("noOfKhasiWords",noOfKhasiWords);
                editor.putInt("noOfLessons",noOfLessons);
                editor.putInt("noOfGaroWords",noOfGaroWords);

                //check for new app version update then show update
                if(appVersionCode<counter.getAppVersionCode())
                {
                    binding.updateApp.setVisibility(View.VISIBLE);
                    if(!counter.getNewFeatures().equals(""))
                        binding.newFeatures.setText(counter.getNewFeatures());
                    binding.updateAppBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.sngur.learnkhasi")));
                        }
                    });
                }

                editor.commit();
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //getNo of Fav Words
        getNoOfFavWords();
    }

    private void getNoOfFavWords() {
        class getFavCount extends AsyncTask<Void,Void,Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                noOfFavWords=db.favoriteWordDao().getNumberOfWords();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                binding.favorites.setText(noOfFavWords+" favorite words");
            }
        }
        new getFavCount().execute();
    }

}