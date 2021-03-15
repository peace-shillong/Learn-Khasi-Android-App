package com.sngur.learnkhasi.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.SentenceSearchResultTranslatedSentencesAdapter;
import com.sngur.learnkhasi.databinding.ActivitySentenceViewBinding;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.room.Editors;
import com.sngur.learnkhasi.model.room.UserSentenceReported;
import com.sngur.learnkhasi.model.room.UserSentenceVotes;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.EditorsDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceReportedDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceVotesDao;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class SentenceViewActivity extends AppCompatActivity {

    private ActivitySentenceViewBinding binding;
    private Bundle bundle;
    private String sentenceId,fromLang,toLang,sentence,userId,audioUrl,currentUserId,userName,category,currentUserName;
    private boolean translatedKhasi,translatedEnglish,translatedHindi,translatedGaro;
    private Long timestamp;
    private int reported,noOfVotes;
    private DatabaseReference myRef,khasiSentenceRef,englishSentenceRef,khasiEnglishSentenceRef,rootRef,garoSentenceRef,engGaroSentenceRef,hindiSentenceRef,engHindiSentenceRef;
    private String pathFromLangSentence,pathtoLangSentence,pathfromToLangSentence,pathtoFromLangSentence,pathSentenceVote,pathSentenceReport,pathtoGaroSentence,pathtoHindiSentence,pathtoGaroSentenceVote,pathtoHindiSentenceVote,pathFromEngToGaroSentence,pathFromEngToHindiSentence,pathFromGaroToEngSentence,pathFromHindiToEngSentence;
    private FirebaseDatabase database;
    private List<Sentence> translatedSentencesList,translatedGaroSentencesList,translatedHindiSentencesList;
    private List<String> translatedSentencesKey,translatedGaroSentencesKey,translatedHindiSentencesKey;
    private int english, hindi, garo, khasi, level;
    private SharedPreferences preferences;
    private TextInputEditText translatedSentence;
    private TextInputLayout translatedSentenceHint;
    private ImageButton record,stop,play;
    private Button answerBtn;
    private Sentence englishSentence,khasiSentence,newUserSentence;
    private AlertDialog alertDialog;
    private MediaRecorder myAudioRecorder;
    private File outputFile;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private TextView recTxt;
    private Boolean fileRecorded;
    //firebase storage
    private StorageReference storageReference,sRef;
    Uri fileUploadUri;
    String uploadedFileDownloadUrl;
    private ProgressDialog progressDialog;
    private Sentence updatedSentence;
    private static List<String> votedSentencesId,reportedSentenceId;
    private static List<UserSentenceVotes> votedSentences;
    private static LearnKhasiDatabase db;
    private String updateUserId,msg,activity,key;
    private DatabaseReference userPointsRef;
    private int userPoints;
    private int finalNoOfVotes;
    private MediaPlayer mediaPlayer;
    private List<Editors> editorsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sentence_view);
        binding=ActivitySentenceViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bundle=getIntent().getExtras();
        sentenceId=bundle.getString("sentenceId");
        fromLang=bundle.getString("fromLanguage");
        toLang=bundle.getString("toLanguage");
        sentence=bundle.getString("sentence");
        userId=bundle.getString("userId");
        timestamp=bundle.getLong("timestamp");
        audioUrl=bundle.getString("audioUrl");
        reported=bundle.getInt("reported");
        translatedKhasi=bundle.getBoolean("translatedKhasi");
        noOfVotes=bundle.getInt("noOfVotes");
        //new data as on 29th July
        userName=bundle.getString("userName");
        category=bundle.getString("category");
        translatedEnglish=bundle.getBoolean("translatedEnglish");
        translatedHindi=bundle.getBoolean("translatedHindi");
        translatedGaro=bundle.getBoolean("translatedGaro");
        activity=bundle.getString("activity");

        key="";
        getAPIKey();

        preferences=getSharedPreferences("LearnKhasi", 0);
        khasi=preferences.getInt("khasi",0);
        english=preferences.getInt("english",0);
        hindi=preferences.getInt("hindi",0);
        garo=preferences.getInt("garo",0);
        level=preferences.getInt("level",0);
        currentUserId=preferences.getString("gid","");
        currentUserName=preferences.getString("name","N/A ");
        if(currentUserName.contains(" "))
            currentUserName=currentUserName.substring(0,currentUserName.indexOf(" "));
        /*Algo
              1. ENG to Khasi
              set the sentence
              Get english_sentence Id : sentenceId
              Firebase get child english_khasi_sentences
              get child with key sentenceId in english_khasi_sentence
                to get all the khasi sentences that have been replied to the english sentence
              if(children<10) show answer btn
         */
        binding.fromSentence.setText(sentence);
        if(!audioUrl.equals(""))
        {
            binding.playAudio.setVisibility(View.VISIBLE);
            binding.playAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //play Audio from URL
                    if(mediaPlayer!=null){
                        mediaPlayer.release();
                        mediaPlayer=null;
                        mediaPlayer=new MediaPlayer();
                    }
                    else {
                        mediaPlayer=new MediaPlayer();
                    }
                    try {
                        mediaPlayer.setDataSource(audioUrl);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mediaPlayer.setAudioAttributes(
                                    new AudioAttributes
                                            .Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                            .build());
                        }else
                        {
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        }
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                mediaPlayer.start();
//                            Toast.makeText(context, "Playing Audio", Toast.LENGTH_LONG).show();
                            }
                        });
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                Toast.makeText(SentenceViewActivity.this, "Audio Completed", Toast.LENGTH_LONG).show();
                            }
                        });
                        Toast.makeText(SentenceViewActivity.this, "Playing Audio", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        // make something
                        //Log.d("ERROR",e.getMessage()+"");
                        //e.printStackTrace();
                    }
                }
                }
            );
        }
        db=LearnKhasiDatabase.getDatabase(SentenceViewActivity.this);
        database=FirebaseDatabase.getInstance();
        rootRef=database.getReference();

        //Place for defining the path for Languages
        if(fromLang.equals("KHA") && toLang.equals("ENG")){
            binding.fromSentenceLanguage.setText(R.string.kha);
            binding.fromSentenceLanguage.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            binding.fromSentenceLanguage.setTextColor(getResources().getColor(R.color.colorWhite));

            myRef = database.getReference("khasi_english_sentence"); //path to english sentences
            khasiSentenceRef= database.getReference("english_sentence");
            englishSentenceRef= database.getReference("khasi_sentence");
            khasiEnglishSentenceRef= database.getReference("english_khasi_sentence");
            //paths to firebase
            pathFromLangSentence="khasi_sentence/";
            pathtoLangSentence="english_sentence/";
            pathfromToLangSentence="khasi_english_sentence/";
            pathtoFromLangSentence="english_khasi_sentence/";
            //update paths to firebase
            pathSentenceVote="khasi_sentence_vote/";
            pathSentenceReport="sentence_reported/";
            binding.toSentenceTitle.setText("English Sentences");
        }else if(fromLang.equals("ENG") && toLang.equals("KHA"))
        {
            binding.fromSentenceLanguage.setText(R.string.eng);
            binding.fromSentenceLanguage.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            binding.fromSentenceLanguage.setTextColor(getResources().getColor(R.color.colorWhite));

            myRef = database.getReference("english_khasi_sentence"); //path to english sentences
            khasiSentenceRef= database.getReference("khasi_sentence");
            englishSentenceRef= database.getReference("english_sentence");
            khasiEnglishSentenceRef= database.getReference("khasi_english_sentence");
            //paths to firebase
            pathFromLangSentence="english_sentence/";
            pathtoLangSentence="khasi_sentence/";
            pathfromToLangSentence="english_khasi_sentence/";
            pathtoFromLangSentence="khasi_english_sentence/";
            //update paths to firebase
            pathSentenceVote="english_sentence_vote/";
            pathSentenceReport="sentence_reported/";
            binding.toSentenceTitle.setText("Khasi Sentences");
            //Extra Data for Garo Sentences and Hindi Sentences
            pathtoGaroSentence="garo_sentence/";
            pathtoHindiSentence="hindi_sentence/";
            pathtoGaroSentenceVote="garo_sentence_vote/";
            pathtoHindiSentenceVote="hindi_sentence_vote/";
            pathFromEngToGaroSentence="english_garo_sentence/";
            pathFromGaroToEngSentence="garo_english_sentence/";
            pathFromEngToHindiSentence="english_hindi_sentence/";
            pathFromHindiToEngSentence="hindi_english_sentence/";
            binding.toGaroSentences.setVisibility(View.VISIBLE);
            binding.toHindiSentences.setVisibility(View.VISIBLE);
            //for database ref
            engGaroSentenceRef= database.getReference("english_garo_sentence");
            translatedGaroSentencesList=new ArrayList<>();
            translatedGaroSentencesKey=new ArrayList<>();
            garoSentenceRef=database.getReference("garo_sentence");

            translatedHindiSentencesList=new ArrayList<>();
            translatedHindiSentencesKey=new ArrayList<>();
            hindiSentenceRef=database.getReference("hindi_sentence");
            engHindiSentenceRef=database.getReference("english_hindi_sentence");

        }
        //TODO LATER ENG-GARO, GARO-ENG, HINDI-ENG ENG-HINDI, ENG-PNAR, ENG-WAR, ENG-WEST,ENG-BHOI
        loadEditors();

        binding.category.setText("Category: "+category);
        binding.userName.setText("Added by: "+userName);

        storageReference = FirebaseStorage.getInstance().getReference();

        translatedSentencesList=new ArrayList<>();
        translatedSentencesKey=new ArrayList<>();
        //Record File
        fileRecorded=false;
        //outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        outputFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "Learn Khasi.3gp");
        uploadedFileDownloadUrl="";
        requestPermissions();

        //Like and Unlike ASKED sentence
        votedSentences=new ArrayList<>();
        votedSentencesId=new ArrayList<>();

        userPoints=0;
        binding.noOfVotes.setText(noOfVotes+"");

        reportedSentenceId=new ArrayList<>();
        getUserVoteData();//load the sentence likes data
        getUserReportedData(); //load the sentence reported data
        //Done Test Dynamic data
        binding.voteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean vote=true;
                //fixed this using loop only
                for (int i = 0; i < votedSentencesId.size(); i++) {
                    //undetstood cuz I reversed the list
                    if (votedSentences.get(i).getSentenceId().equals(sentenceId)){
                        vote = votedSentences.get(i).isVote();
                        //Log.d("VOTED DOWN",sentenceId+ " is sentenceId");
                        //Log.d("VOTED DOWN", vote + "-"+votedSentences.get(i).getSentenceId());
                        break;
                    }
                }
                if(vote) {
                    //Update points of user
                    updateUserId=userId;
                    userPointsRef=FirebaseDatabase.getInstance().getReference();
                    userPointsRef.child("user_points/"+updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pointsExist = (int) snapshot.getChildrenCount();
                            if(pointsExist>0)
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    userPoints = postSnapshot.getValue(Integer.class);
                                    //update Like Data
                                    if(userPoints > 0)
                                        userPoints-=5;
                                    updateUserVoteDownData(noOfVotes,false,sentenceId,userPoints);
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteDownData(noOfVotes,false,sentenceId,userPoints);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else{
                    Toast.makeText(SentenceViewActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Vote Up
        binding.voteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "add +ve vote from this user", Toast.LENGTH_SHORT).show();
                //originalSentenceNoOfVotes++;

                boolean vote=false;
                for (int i = 0; i < votedSentencesId.size(); i++) {
                    if (votedSentences.get(i).getSentenceId().equals(sentenceId)) {
                        vote = votedSentences.get(i).isVote();
                        //Log.d("VOTED UP",sentenceId+ " is sentenceId");
                        //Log.d("VOTED UP",  vote + "-"+votedSentencesId.size()+votedSentences.get(i).getSentenceId()+" got sentenceId "+i+"-");
                        break;
                    }
                }
                if (!vote) {
                    //Test on Like/Unlike Does user gets points updated or not
                    //Update points
                    updateUserId=userId;
                    userPointsRef=FirebaseDatabase.getInstance().getReference();
                    userPointsRef.child("user_points/"+updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pointsExist = (int) snapshot.getChildrenCount();
                            if(pointsExist>0)
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    userPoints = postSnapshot.getValue(Integer.class);
                                    //update Like Data
                                    userPoints+=10;
                                    updateUserVoteDownData(noOfVotes,true,sentenceId,userPoints);
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteDownData(noOfVotes,true,sentenceId,userPoints);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else {
                    Toast.makeText(SentenceViewActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                }


            }
        });

        //Report Sentence of user - we will need room
        binding.reportFromSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(level>1) {
                    //check if user has already reported the sentence
                    boolean isReported = false;
                    for (int i = 0; i < reportedSentenceId.size(); i++) {
                        if (reportedSentenceId.get(i).equals(sentenceId)) {
                            isReported = true;
                            Toast.makeText(SentenceViewActivity.this, "You have already reported this sentence", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (!isReported) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SentenceViewActivity.this);
                        builder.setTitle("Report Sentence");
                        builder.setMessage("Is this sentence invalid or wrong?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Report Invalid and store to phone
                                reported += 1;
                                updatedSentence = new Sentence(binding.fromSentence.getText().toString(), userId, audioUrl, reported, timestamp, translatedKhasi, noOfVotes, userName, category, translatedEnglish, translatedHindi, translatedGaro);
                                //pushData for Update
                                HashMap<String, Object> updatedUserSentence = new HashMap<>();
                                updatedUserSentence.put(pathFromLangSentence + sentenceId + "/", updatedSentence);
                                updatedUserSentence.put(pathSentenceReport + currentUserId + "/" + sentenceId, true);//user's reported sentence
                                rootRef.updateChildren(updatedUserSentence, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error != null) {
                                            Toast.makeText(SentenceViewActivity.this, "Unable to report sentence", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(SentenceViewActivity.this, "Sentence reported", Toast.LENGTH_SHORT).show();
                                        }
                                        alertDialog.dismiss();
                                        storeUserReportedData(sentenceId, pathFromLangSentence.substring(0, pathFromLangSentence.length() - 1), true);//reported
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
                else{
                    Toast.makeText(SentenceViewActivity.this, "You can report sentences as invalid once you reach level 2", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getAPIKey() {
        myRef=FirebaseDatabase.getInstance().getReference("status");
        myRef.child("limit").child("temp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                key=snapshot.getValue(String.class);
                //Log.e("LK",key+"");
                binding.answerBtn.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadEditors() {
        class loadEditors extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                EditorsDao editorsDao=db.editorsDao();
                editorsList=editorsDao.getAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //load the data now from fb
                loadDataFromFB();
                allowEditOfSentence();
            }
        }
        new loadEditors().execute();
    }

    private void allowEditOfSentence() {
        Boolean found=false;
        for(Editors user: editorsList)
        {
            if(user.getUserId().equals(currentUserId)) {
                found = true;
                break;
            }
        }
        //Edit Sentence for user
        if(userId.equals(currentUserId) || found)
        {

            binding.editItem.setVisibility(View.VISIBLE);
            binding.editItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //dialogView custom Layout
                    AlertDialog.Builder builder = new AlertDialog.Builder(SentenceViewActivity.this);
                    ViewGroup viewGroup = view.findViewById(android.R.id.content);
                    final View dialogView = LayoutInflater.from(SentenceViewActivity.this).inflate(R.layout.dialog_layout_answer, viewGroup, false);
                    builder.setView(dialogView);
                    TextView dialogTitle;
                    dialogTitle=dialogView.findViewById(R.id.dialogTitle);
                    dialogTitle.setText("Edit Your Sentence");
                    LinearLayout bigLevel=dialogView.findViewById(R.id.bigLevel);
                    bigLevel.setVisibility(View.GONE);
                    translatedSentence=dialogView.findViewById(R.id.translatedSentence);
                    translatedSentenceHint=dialogView.findViewById(R.id.translatedSentenceHint);
                    translatedSentenceHint.setHint("Edit Your Sentence");
                    record=dialogView.findViewById(R.id.record);
                    play=dialogView.findViewById(R.id.play);
                    answerBtn=dialogView.findViewById(R.id.buttonAnswer);
                    translatedSentence.setText(sentence);
                    record.setVisibility(View.GONE);
                    play.setVisibility(View.GONE);
                    answerBtn.setText(R.string.edit_translation);
                    answerBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(sentence.equals(translatedSentence.getText().toString()))
                            {
                                Toast.makeText(SentenceViewActivity.this, "Sentence has not been changed", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                            else if(translatedSentence.getText().toString().contains(" ")){
                                //we can change the timestamp if we want to - we can also show User First Name in UI of all sentences - both translated and original
                                String temp=translatedSentence.getText().toString().toLowerCase()+"";
                                final String editedSentence=temp.substring(0,1).toUpperCase()+temp.substring(1);
                                updatedSentence=new Sentence(editedSentence,currentUserId,audioUrl,reported,timestamp,translatedKhasi,noOfVotes,userName,category,translatedEnglish,translatedHindi,translatedGaro);
                                //pushData for Update
                                HashMap<String, Object> updatedUserSentence = new HashMap<>();
//                                Log.e("DATA",editedSentence);
                                //make this dynamic orignial english_sentence
                                updatedUserSentence.put(pathFromLangSentence+sentenceId+"/", updatedSentence);

                                rootRef.updateChildren(updatedUserSentence, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if(error!=null){
                                            Toast.makeText(SentenceViewActivity.this, "Unable to update sentence", Toast.LENGTH_SHORT).show();
                                        }
                                        Toast.makeText(SentenceViewActivity.this, "Sentence Updated", Toast.LENGTH_SHORT).show();
                                        //holder.translatedSentence.setText(translatedSentence.getText().toString());
                                        binding.fromSentence.setText(editedSentence);
                                        alertDialog.dismiss();
                                    }
                                });
                            }
                            else{
                                translatedSentence.setError("Enter a sentence");
                            }
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }

    private void loadDataFromFB() {
        //Get Data for the sentence
        myRef.child(sentenceId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //LOG
                int noOfKhasiSentences = (int) snapshot.getChildrenCount();
                if(noOfKhasiSentences>0)
                    binding.noKhasiSentences.setVisibility(View.GONE);
                //just for answer button display it based on level
                if(noOfKhasiSentences<=level+1)
                {
                    if(english==1 && khasi==1) //eng - kha
                        binding.answerBtn.setVisibility(View.VISIBLE);
                    if(activity.equals("lessons"))
                        binding.answerBtn.setEnabled(false);
                    binding.answerBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //use custom layout dialog
                            //sentenceId - ENGLISH ID
                            //push and get Khasi Id
                            //insert to khasi_sentence
                            //insert to english_khasi_sentence and khasi_english_sentence with true id
                            //onBackPressed();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SentenceViewActivity.this);
                            ViewGroup viewGroup = findViewById(android.R.id.content);
                            View dialogView = LayoutInflater.from(SentenceViewActivity.this).inflate(R.layout.dialog_layout_answer, viewGroup, false);
                            builder.setView(dialogView);
                            translatedSentence=dialogView.findViewById(R.id.translatedSentence);
                            translatedSentenceHint=dialogView.findViewById(R.id.translatedSentenceHint);
                            if(fromLang.equals("ENG")) {
                                translatedSentenceHint.setHint("Enter the Khasi Sentence");
                            }
                            else if(fromLang.equals("KHA")) {
                                translatedSentenceHint.setHint("Enter the English Sentence");
                                LinearLayout bigLevel=dialogView.findViewById(R.id.bigLevel);
                                bigLevel.setVisibility(View.GONE);
                            }
                            record=dialogView.findViewById(R.id.record);
                            play=dialogView.findViewById(R.id.play);
                            answerBtn=dialogView.findViewById(R.id.buttonAnswer);
                            recTxt=dialogView.findViewById(R.id.recording);
                            stop=dialogView.findViewById(R.id.pause);

                            record.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //check for File and Microphone Permission
                                    requestPermissions();
                                    //fileRecorded=false;
                                    if(level<=2)
                                    {
                                        Toast.makeText(SentenceViewActivity.this, "You need to increase your level to be able to Record Audio Clip "+level, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    //start recording
                                    //hide this icon use the pause icon
                                    if(checkPermission()){
                                        try {
                                            if(myAudioRecorder!=null)
                                            {
                                                myAudioRecorder.release();
                                                myAudioRecorder=null;
                                            }
                                            myAudioRecorder = new MediaRecorder();
                                            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                                            myAudioRecorder.setOutputFile(outputFile.getAbsolutePath());
                                            myAudioRecorder.prepare();
                                            myAudioRecorder.start();

                                            myAudioRecorder.getMaxAmplitude(); //gets amplitude from the MediaRecorder
                                            //record.setEnabled(true);
                                            recTxt.setVisibility(View.VISIBLE);
                                            record.setVisibility(View.GONE);
                                            stop.setVisibility(View.VISIBLE);
                                            //record.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
                                            manageBlinkEffect();
                                        }catch (Exception e)
                                        {
//                                                    Log.e("DATA",e.getMessage()+"-"+e.getCause());
//                                                    e.printStackTrace();
                                            Toast.makeText(SentenceViewActivity.this, "Unable to record", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        Toast.makeText(SentenceViewActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            stop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    //stop recording
                                    //hide this icon use the record icon
                                    play.setEnabled(true);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                                    }
                                    myAudioRecorder.stop();
                                    myAudioRecorder.release();
                                    myAudioRecorder = null;
                                    record.setEnabled(true);
                                    play.setEnabled(true);
                                    fileRecorded=true;
                                    recTxt.setVisibility(View.INVISIBLE);
                                    record.setVisibility(View.VISIBLE);
                                    stop.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
                                }
                            });

                            play.setEnabled(false);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlack)));
                            }
                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(level<=2) {
                                        Toast.makeText(SentenceViewActivity.this, "You need to increase your level to be able to Play Recorded Audio Clip", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        //Check if recorded exist = TRUE
                                        //play recording file
                                        //else Toast start recording to play audio
                                        MediaPlayer mediaPlayer = new MediaPlayer();
                                        try {
                                            mediaPlayer.setDataSource(outputFile.getAbsolutePath());
                                            mediaPlayer.prepare();
                                            mediaPlayer.start();
                                            Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            // make something
                                        }
                                    }
                                }
                            });
                            answerBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(translatedSentence.getText()!=null && translatedSentence.getText().toString().contains(" "))
                                    {
                                        //Toast.makeText(SentenceViewActivity.this, "Answer "+translatedSentence.getText(), Toast.LENGTH_SHORT).show();
                                        answerBtn.setEnabled(false);
                                        if(fileRecorded)
                                            uploadFile(translatedSentence.getText().toString());
                                        else
                                            pushData(translatedSentence.getText().toString());
                                    }
                                    else{
                                        translatedSentence.setError("Please enter a sentence");
                                        Toast.makeText(SentenceViewActivity.this, "Please enter a sentence", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                    });
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //getting list of khasi sentences for the sentenceId
                    translatedSentencesList.clear();
                    translatedSentencesKey.clear();
                    //Log.d("DATA ID","GOT SOME DATA");
                    String khasiSentenceId=postSnapshot.getKey();
                    //sentenceList.add(sentence);
                    //Log.d("ID",postSnapshot.getKey()+" - "+khasiSentenceId);
                    //sentenceId.add(postSnapshot.getKey()+"");
                    //for each key then get khasi_sentence object in Single Data mo
                    khasiSentenceRef.child(khasiSentenceId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                                    Log.d("DATA","GOT SOME DATA");
//                                    String sentence=postSnapshot.getValue().toString();
//                                    //sentenceList.add(sentence);
//                                    Log.d("ID",postSnapshot.getKey()+" - "+sentence);
//                                    //sentenceId.add(postSnapshot.getKey()+"");
//                                }
                            if (!translatedSentencesKey.contains(snapshot.getKey()))
                                translatedSentencesKey.add(snapshot.getKey());
                            //Log.d("DATA", "Translated Sentence Id " + snapshot.getKey());
                            Sentence sentence = snapshot.getValue(Sentence.class);

                            for (int i = 0; i < translatedSentencesKey.size(); i++)
                            {
                                if(i<translatedSentencesList.size()) {
                                    if (sentence.getSentence().equals(translatedSentencesList.get(i).getSentence()))
                                        translatedSentencesList.get(i).setNoOfVotes(sentence.getNoOfVotes());
                                    //try here to change the update of RV item on update of translation
                                    if(snapshot.getKey().equals(translatedSentencesKey.get(i)))
                                    {
                                        if(!sentence.getSentence().equals(translatedSentencesList.get(i).getSentence()))
                                        {
                                            //Updated Sentence
                                            translatedSentencesList.get(i).setSentence(sentence.getSentence()+"");
                                            //Log.d("UPDATED","DATA");
                                        }
                                    }
                                }
                                else
                                    translatedSentencesList.add(sentence);
                            }
                            //if Translated Sentences are there then display here
                            if(translatedSentencesList.size()>0){
                                SentenceSearchResultTranslatedSentencesAdapter mAdapter=new SentenceSearchResultTranslatedSentencesAdapter(translatedSentencesList,SentenceViewActivity.this,currentUserId,sentenceId,translatedSentencesKey,noOfVotes,fromLang,toLang,editorsList);
                                binding.toSentenceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                binding.toSentenceList.setHasFixedSize(true);
                                binding.toSentenceList.setAdapter(mAdapter);
                                //Log.d("DATA","SIZE");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

        if(fromLang.equals("ENG")) {
            //GET DATA for Garo Sentences
            engGaroSentenceRef.child(sentenceId).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //LOG
                    int noOfGaroSentences = (int) snapshot.getChildrenCount();
                    if(noOfGaroSentences>0)
                        binding.noGaroSentences.setVisibility(View.GONE);
                    //load Data
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        //getting list of khasi sentences for the sentenceId
                        translatedGaroSentencesList.clear();
                        translatedGaroSentencesKey.clear();
                        //Log.d("DATA ID","GOT SOME DATA");
                        String garoSentenceId=postSnapshot.getKey();
                        //sentenceList.add(sentence);
                        //Log.d("ID",postSnapshot.getKey()+" - "+garoSentenceId);
                        //sentenceId.add(postSnapshot.getKey()+"");
                        //for each key then get khasi_sentence object in Single Data mo
                        garoSentenceRef.child(garoSentenceId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                                    Log.d("DATA","GOT SOME DATA");
//                                    String sentence=postSnapshot.getValue().toString();
//                                    //sentenceList.add(sentence);
//                                    Log.d("ID",postSnapshot.getKey()+" - "+sentence);
//                                    //sentenceId.add(postSnapshot.getKey()+"");
//                                }
                                if (!translatedGaroSentencesKey.contains(snapshot.getKey()))
                                    translatedGaroSentencesKey.add(snapshot.getKey());
                                //Log.d("DATA", "Translated Sentence Id " + snapshot.getKey());
                                Sentence sentence = snapshot.getValue(Sentence.class);

                                for (int i = 0; i < translatedGaroSentencesKey.size(); i++)
                                {
                                    if(i<translatedGaroSentencesList.size()) {
                                        if (sentence.getSentence().equals(translatedGaroSentencesList.get(i).getSentence()))
                                            translatedGaroSentencesList.get(i).setNoOfVotes(sentence.getNoOfVotes());
                                        //try here to change the update of RV item on update of translation
                                        if(snapshot.getKey().equals(translatedGaroSentencesKey.get(i)))
                                        {
                                            if(!sentence.getSentence().equals(translatedGaroSentencesList.get(i).getSentence()))
                                            {
                                                //Updated Sentence
                                                translatedGaroSentencesList.get(i).setSentence(sentence.getSentence()+"");
                                                //Log.d("UPDATED","DATA");
                                            }
                                        }
                                    }
                                    else
                                        translatedGaroSentencesList.add(sentence);
                                }
                                //if Translated Sentences are there then display here
                                if(translatedGaroSentencesList.size()>0){
                                    SentenceSearchResultTranslatedSentencesAdapter mAdapter=new SentenceSearchResultTranslatedSentencesAdapter(translatedGaroSentencesList,SentenceViewActivity.this,currentUserId,sentenceId,translatedGaroSentencesKey,noOfVotes,fromLang,"GARO",editorsList);
                                    binding.toGaroSentenceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    binding.toGaroSentenceList.setHasFixedSize(true);
                                    binding.toGaroSentenceList.setAdapter(mAdapter);
                                    //Log.d("DATA","SIZE");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    //Answer button display it based on level
                    if(noOfGaroSentences<=level+1)
                    {
                        if(english==1 && garo==1) //eng - kha
                            binding.answerGaroBtn.setVisibility(View.VISIBLE);
                        if(activity.equals("lessons"))
                            binding.answerGaroBtn.setEnabled(false);

                        binding.answerGaroBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //use custom layout dialog
                                //sentenceId - ENGLISH ID
                                //push and get Khasi Id
                                //insert to khasi_sentence
                                //insert to english_khasi_sentence and khasi_english_sentence with true id
                                //onBackPressed();
                                AlertDialog.Builder builder = new AlertDialog.Builder(SentenceViewActivity.this);
                                ViewGroup viewGroup = findViewById(android.R.id.content);
                                View dialogView = LayoutInflater.from(SentenceViewActivity.this).inflate(R.layout.dialog_layout_answer, viewGroup, false);
                                builder.setView(dialogView);
                                translatedSentence=dialogView.findViewById(R.id.translatedSentence);
                                translatedSentenceHint=dialogView.findViewById(R.id.translatedSentenceHint);
                                translatedSentenceHint.setHint("Enter the Garo Sentence");
                                record=dialogView.findViewById(R.id.record);
                                play=dialogView.findViewById(R.id.play);
                                answerBtn=dialogView.findViewById(R.id.buttonAnswer);
                                recTxt=dialogView.findViewById(R.id.recording);
                                stop=dialogView.findViewById(R.id.pause);

                                record.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //check for File and Microphone Permission
                                        requestPermissions();
                                        //fileRecorded=false;
                                        if(level<=2)
                                        {
                                            Toast.makeText(SentenceViewActivity.this, "You need to increase your level to be able to Record Audio Clip "+level, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        //start recording
                                        //hide this icon use the pause icon
                                        if(checkPermission()){
                                            try {
                                                if(myAudioRecorder!=null)
                                                {
                                                    myAudioRecorder.release();
                                                    myAudioRecorder=null;
                                                }
                                                myAudioRecorder = new MediaRecorder();
                                                myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                                myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                                myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                                                myAudioRecorder.setOutputFile(outputFile.getAbsolutePath());
                                                myAudioRecorder.prepare();
                                                myAudioRecorder.start();

                                                myAudioRecorder.getMaxAmplitude(); //gets amplitude from the MediaRecorder
                                                //record.setEnabled(true);
                                                recTxt.setVisibility(View.VISIBLE);
                                                record.setVisibility(View.GONE);
                                                stop.setVisibility(View.VISIBLE);
                                                //record.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
                                                manageBlinkEffect();
                                            }catch (Exception e)
                                            {
                                                //Log.e("DATA",e.getMessage()+"-"+e.getCause());
                                                //e.printStackTrace();
                                                Toast.makeText(SentenceViewActivity.this, "Unable to record", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else{
                                            Toast.makeText(SentenceViewActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                stop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //stop recording
                                        //hide this icon use the record icon
                                        play.setEnabled(true);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                                        }
                                        myAudioRecorder.stop();
                                        myAudioRecorder.release();
                                        myAudioRecorder = null;
                                        record.setEnabled(true);
                                        play.setEnabled(true);
                                        fileRecorded=true;
                                        recTxt.setVisibility(View.INVISIBLE);
                                        record.setVisibility(View.VISIBLE);
                                        stop.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
                                    }
                                });

                                play.setEnabled(false);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlack)));
                                }
                                play.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(level<=2) {
                                            Toast.makeText(SentenceViewActivity.this, "You need to increase your level to be able to Play Recorded Audio Clip", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            //Check if recorded exist = TRUE
                                            //play recording file
                                            //else Toast start recording to play audio
                                            MediaPlayer mediaPlayer = new MediaPlayer();
                                            try {
                                                mediaPlayer.setDataSource(outputFile.getAbsolutePath());
                                                mediaPlayer.prepare();
                                                mediaPlayer.start();
                                                Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                                            } catch (Exception e) {
                                                // make something
                                            }
                                        }
                                    }
                                });
                                answerBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(translatedSentence.getText()!=null && translatedSentence.getText().toString().contains(" "))
                                        {
                                            //Toast.makeText(SentenceViewActivity.this, "Answer "+translatedSentence.getText(), Toast.LENGTH_SHORT).show();
                                            answerBtn.setEnabled(false);
                                            if(fileRecorded)
                                                uploadGaroFile(translatedSentence.getText().toString());
                                            else
                                                pushGaroData(translatedSentence.getText().toString());
                                        }
                                        else{
                                            translatedSentence.setError("Please enter a sentence");
                                            Toast.makeText(SentenceViewActivity.this, "Please enter a sentence", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });
                    }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }


            });
            //GET DATA for Hindi Sentences
            engHindiSentenceRef.child(sentenceId).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //LOG
                    int noOfHindiSentences = (int) snapshot.getChildrenCount();
                    if(noOfHindiSentences>0)
                        binding.noHindiSentences.setVisibility(View.GONE);
                    //load Data
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        //getting list of khasi sentences for the sentenceId
                        translatedHindiSentencesList.clear();
                        translatedHindiSentencesKey.clear();
                        //Log.d("DATA ID","GOT SOME DATA");
                        String garoSentenceId=postSnapshot.getKey();
                        //sentenceList.add(sentence);
                        //Log.d("ID",postSnapshot.getKey()+" - "+garoSentenceId);
                        //sentenceId.add(postSnapshot.getKey()+"");
                        //for each key then get khasi_sentence object in Single Data mo
                        hindiSentenceRef.child(garoSentenceId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                                    Log.d("DATA","GOT SOME DATA");
//                                    String sentence=postSnapshot.getValue().toString();
//                                    //sentenceList.add(sentence);
//                                    Log.d("ID",postSnapshot.getKey()+" - "+sentence);
//                                    //sentenceId.add(postSnapshot.getKey()+"");
//                                }
                                if (!translatedHindiSentencesKey.contains(snapshot.getKey()))
                                    translatedHindiSentencesKey.add(snapshot.getKey());
                                //Log.d("DATA", "Translated Sentence Id " + snapshot.getKey());
                                Sentence sentence = snapshot.getValue(Sentence.class);

                                for (int i = 0; i < translatedHindiSentencesKey.size(); i++)
                                {
                                    if(i<translatedHindiSentencesList.size()) {
                                        if (sentence.getSentence().equals(translatedHindiSentencesList.get(i).getSentence()))
                                            translatedHindiSentencesList.get(i).setNoOfVotes(sentence.getNoOfVotes());
                                        //try here to change the update of RV item on update of translation
                                        if(snapshot.getKey().equals(translatedHindiSentencesKey.get(i)))
                                        {
                                            if(!sentence.getSentence().equals(translatedHindiSentencesList.get(i).getSentence()))
                                            {
                                                //Updated Sentence
                                                translatedHindiSentencesList.get(i).setSentence(sentence.getSentence()+"");
                                                //Log.d("UPDATED","DATA");
                                            }
                                        }
                                    }
                                    else
                                        translatedHindiSentencesList.add(sentence);
                                }
                                //if Translated Sentences are there then display here
                                if(translatedHindiSentencesList.size()>0){
                                    SentenceSearchResultTranslatedSentencesAdapter mAdapter=new SentenceSearchResultTranslatedSentencesAdapter(translatedHindiSentencesList,SentenceViewActivity.this,currentUserId,sentenceId,translatedHindiSentencesKey,noOfVotes,fromLang,"HIN",editorsList);
                                    binding.toHindiSentenceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    binding.toHindiSentenceList.setHasFixedSize(true);
                                    binding.toHindiSentenceList.setAdapter(mAdapter);
                                    //Log.d("DATA","SIZE");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    //Answer button display it based on level
                    if(noOfHindiSentences<=level+1)
                    {
                        //Log.d("HINDI",hindi+" is value");
                        if(english==1 && hindi==1) //eng - kha
                            binding.answerHindiBtn.setVisibility(View.VISIBLE);
                        if(activity.equals("lessons"))
                            binding.answerHindiBtn.setEnabled(false);
                        binding.answerHindiBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //use custom layout dialog
                                //sentenceId - ENGLISH ID
                                //push and get Khasi Id
                                //insert to khasi_sentence
                                //insert to english_khasi_sentence and khasi_english_sentence with true id
                                //onBackPressed();
                                AlertDialog.Builder builder = new AlertDialog.Builder(SentenceViewActivity.this);
                                ViewGroup viewGroup = findViewById(android.R.id.content);
                                View dialogView = LayoutInflater.from(SentenceViewActivity.this).inflate(R.layout.dialog_layout_answer, viewGroup, false);
                                builder.setView(dialogView);
                                translatedSentence=dialogView.findViewById(R.id.translatedSentence);
                                translatedSentenceHint=dialogView.findViewById(R.id.translatedSentenceHint);
                                translatedSentenceHint.setHint("Enter the Hindi Sentence");
                                record=dialogView.findViewById(R.id.record);
                                play=dialogView.findViewById(R.id.play);
                                answerBtn=dialogView.findViewById(R.id.buttonAnswer);
                                recTxt=dialogView.findViewById(R.id.recording);
                                stop=dialogView.findViewById(R.id.pause);

                                record.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //check for File and Microphone Permission
                                        requestPermissions();
                                        //fileRecorded=false;
                                        if(level<=2)
                                        {
                                            Toast.makeText(SentenceViewActivity.this, "You need to increase your level to be able to Record Audio Clip "+level, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        //start recording
                                        //hide this icon use the pause icon
                                        if(checkPermission()){
                                            try {
                                                if(myAudioRecorder!=null)
                                                {
                                                    myAudioRecorder.release();
                                                    myAudioRecorder=null;
                                                }
                                                myAudioRecorder = new MediaRecorder();
                                                myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                                myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                                myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                                                myAudioRecorder.setOutputFile(outputFile.getAbsolutePath());
                                                myAudioRecorder.prepare();
                                                myAudioRecorder.start();

                                                myAudioRecorder.getMaxAmplitude(); //gets amplitude from the MediaRecorder
                                                //record.setEnabled(true);
                                                recTxt.setVisibility(View.VISIBLE);
                                                record.setVisibility(View.GONE);
                                                stop.setVisibility(View.VISIBLE);
                                                //record.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
                                                manageBlinkEffect();
                                            }catch (Exception e)
                                            {
//                                                Log.e("DATA",e.getMessage()+"-"+e.getCause());
//                                                e.printStackTrace();
                                                Toast.makeText(SentenceViewActivity.this, "Unable to record", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else{
                                            Toast.makeText(SentenceViewActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                stop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //stop recording
                                        //hide this icon use the record icon
                                        play.setEnabled(true);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                                        }
                                        myAudioRecorder.stop();
                                        myAudioRecorder.release();
                                        myAudioRecorder = null;
                                        record.setEnabled(true);
                                        play.setEnabled(true);
                                        fileRecorded=true;
                                        recTxt.setVisibility(View.INVISIBLE);
                                        record.setVisibility(View.VISIBLE);
                                        stop.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
                                    }
                                });

                                play.setEnabled(false);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlack)));
                                }
                                play.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(level<=2) {
                                            Toast.makeText(SentenceViewActivity.this, "You need to increase your level to be able to Play Recorded Audio Clip", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            //Check if recorded exist = TRUE
                                            //play recording file
                                            //else Toast start recording to play audio
                                            MediaPlayer mediaPlayer = new MediaPlayer();
                                            try {
                                                mediaPlayer.setDataSource(outputFile.getAbsolutePath());
                                                mediaPlayer.prepare();
                                                mediaPlayer.start();
                                                Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                                            } catch (Exception e) {
                                                // make something
                                            }
                                        }
                                    }
                                });
                                answerBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(translatedSentence.getText()!=null && translatedSentence.getText().toString().contains(" "))
                                        {
                                            //Toast.makeText(SentenceViewActivity.this, "Answer "+translatedSentence.getText(), Toast.LENGTH_SHORT).show();
                                            answerBtn.setEnabled(false);
                                            if(fileRecorded)
                                                uploadHindiFile(translatedSentence.getText().toString());
                                            else
                                                pushHindiData(translatedSentence.getText().toString());
                                        }
                                        else{
                                            translatedSentence.setError("Please enter a sentence");
                                            Toast.makeText(SentenceViewActivity.this, "Please enter a sentence", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });
                    }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }


            });
        }
    }

    private void updateUserVoteDownData(int votes,final boolean likeData,final String sentenceId, int userPoints) {
        finalNoOfVotes = votes;
        msg="Unlike";
        //simplify the like and unlike post
        if(likeData)
        {
            msg="Liked";
            finalNoOfVotes += 1;
        }else{
            if (finalNoOfVotes != 0)
                finalNoOfVotes -= 1;
        }

        //data to update
        HashMap<String, Object> updatedUserData = new HashMap<>();
        //DONE test change to dynamic - KHA-ENG
        //updatedUserData.put("english_sentence/"+originalSentenceId+"/noOfVotes", originalSentenceNoOfVotes);

        updatedUserData.put(pathSentenceVote + currentUserId + "/" + sentenceId, likeData);
        updatedUserData.put(pathFromLangSentence + sentenceId + "/noOfVotes", finalNoOfVotes);
        updatedUserData.put("user_points/"+updateUserId+"/points",userPoints);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(SentenceViewActivity.this, "Unable to vote", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(SentenceViewActivity.this, msg, Toast.LENGTH_SHORT).show();
                storeUserVoteData(sentenceId, pathFromLangSentence.substring(0,pathFromLangSentence.length()-1), likeData);//updated
                binding.noOfVotes.setText(finalNoOfVotes+"");
            }
        });
    }

    private static void storeUserVoteData(final String sentenceId, final String fromLanguage, final boolean vote) {

        class storeUserVoteData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                UserSentenceVotesDao userSentenceVotesDao=db.userSentenceVotesDao();
                UserSentenceVotes userSentenceVotes=new UserSentenceVotes(sentenceId,fromLanguage,vote);
                userSentenceVotesDao.insert(userSentenceVotes);
                //insert vote in db
                //Log.d("DATA","Stored Data - "+sentenceId+"-"+vote);
                getUserVoteData();
                return null;
            }
        }
        storeUserVoteData storeUserVoteData=new storeUserVoteData();
        storeUserVoteData.execute();
    }

    private void storeUserReportedData(final String sentenceId, final String substring,final boolean value) {
        class storeUserReportedData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                UserSentenceReportedDao userSentenceReportedDao=db.userSentenceReportedDao();
                UserSentenceReported userSentenceReported=new UserSentenceReported(sentenceId,substring,value);
                userSentenceReportedDao.insert(userSentenceReported);
                //insert vote in db
                //Log.d("DATA","Stored Data - "+sentenceId+"-"+value);
                getUserReportedData();
                return null;
            }
        }
        storeUserReportedData storeUserReportedData=new storeUserReportedData();
        storeUserReportedData.execute();
    }

    private void getUserReportedData() {
        class getUserReportedData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                reportedSentenceId.clear();
                UserSentenceReportedDao userSentenceReportedDao=db.userSentenceReportedDao();
                reportedSentenceId=userSentenceReportedDao.getUserReportedSentenceId();
                //get data from db
                return null;
            }
        }
        getUserReportedData getUserReportedData=new getUserReportedData();
        getUserReportedData.execute();
    }

    private static void getUserVoteData(){
        class getUserVoteData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                votedSentencesId.clear();
                votedSentences.clear();
                UserSentenceVotesDao userSentenceVotesDao=db.userSentenceVotesDao();
                votedSentences=userSentenceVotesDao.getUserVotes();
                votedSentencesId=userSentenceVotesDao.getUserVotedSentenceId();
                //get data from db
                return null;
            }
        }
        getUserVoteData getUserVoteData=new getUserVoteData();
        getUserVoteData.execute();
    }

    @SuppressLint("WrongConstant")
    private void manageBlinkEffect() {
        ObjectAnimator anim = ObjectAnimator.ofInt(recTxt, "textColor", Color.WHITE, Color.RED,
                Color.WHITE);
        anim.setDuration(1200);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
        //Log.d("DATA","blink");
    }

    private boolean checkPermission() {
        if( android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
        else
            return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(SentenceViewActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private void uploadHindiFile(final String translatedSentence) {
        sRef=storageReference.child("sentence").child(currentUserId).child(pathtoHindiSentence.substring(0,pathtoHindiSentence.length()-1)).child(new Date().getTime()+"");
        fileUploadUri= Uri.fromFile(new File(outputFile.getAbsolutePath()));
        progressDialog = new ProgressDialog(SentenceViewActivity.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        sRef.putFile(fileUploadUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadedFileDownloadUrl=uri.toString();
                                pushHindiData(translatedSentence);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SentenceViewActivity.this, "File Uploaded, Failed to add translated sentence", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
                Toast.makeText(SentenceViewActivity.this, "Cannot Upload Audio Clip", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot
                        .getTotalByteCount();
                progressDialog.setMessage("Uploaded "+(int)progress+"%");
            }
        });
    }

    private void pushHindiData(String translatedSentence) {
        String hindiSentenceId=hindiSentenceRef.push().getKey();
        //improvement Capitalize the First Letter in translated text - 28th July
        String temp=translatedSentence.toLowerCase()+"";
        translatedSentence=temp.substring(0,1).toUpperCase()+temp.substring(1);
        //original Sentence
        englishSentence=new Sentence(sentence,userId,audioUrl,reported,timestamp,translatedKhasi,noOfVotes,userName,category,translatedEnglish,true,translatedGaro);
        //new Sentence
        newUserSentence=new Sentence(translatedSentence,currentUserId,uploadedFileDownloadUrl,0,new Date().getTime(),translatedKhasi,0,currentUserName,category,true,false,false);

        HashMap<String, Object> updatedUserData = new HashMap<>();

        updatedUserData.put(pathtoHindiSentence+ hindiSentenceId, newUserSentence);
        updatedUserData.put(pathFromLangSentence+sentenceId, englishSentence);
        updatedUserData.put(pathFromEngToHindiSentence+sentenceId+"/" + hindiSentenceId, true);
        updatedUserData.put(pathFromHindiToEngSentence+hindiSentenceId+"/" + sentenceId, true);
        //push hindi_khasi

//        updatedUserData.put("hindi_sentence/" + khasiSentenceId, khasiSentence);
//        updatedUserData.put("english_sentence/"+sentenceId, englishSentence);
//        updatedUserData.put("english_hindi_sentence/"+sentenceId+"/" + khasiSentenceId, true);
//        updatedUserData.put("hindi_english_sentence/"+khasiSentenceId+"/" + sentenceId, true);

        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null)
                {
                    Toast.makeText(SentenceViewActivity.this, "Error Adding Translation", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(SentenceViewActivity.this, "Translation Added", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                translatedHindi=true;
                answerBtn.setEnabled(true);
                if(fromLang.equals("KHA"))
                    translatedKhasi=true;
                else
                    translatedEnglish=true;
            }
        });
    }

    private void uploadGaroFile(final String translatedSentence) {
        sRef=storageReference.child("sentence").child(currentUserId).child(pathtoGaroSentence.substring(0,pathtoGaroSentence.length()-1)).child(new Date().getTime()+"");
        fileUploadUri= Uri.fromFile(new File(outputFile.getAbsolutePath()));
        progressDialog = new ProgressDialog(SentenceViewActivity.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        sRef.putFile(fileUploadUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadedFileDownloadUrl=uri.toString();
                                pushGaroData(translatedSentence);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SentenceViewActivity.this, "File Uploaded, Failed to add translated sentence", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
                Toast.makeText(SentenceViewActivity.this, "Cannot Upload Audio Clip", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot
                        .getTotalByteCount();
                progressDialog.setMessage("Uploaded "+(int)progress+"%");
            }
        });
    }

    private void pushGaroData(String translatedSentence) {
        String garoSentenceId=garoSentenceRef.push().getKey();
        //improvement Capitalize the First Letter in translated text - 28th July
        String temp=translatedSentence.toLowerCase()+"";
        translatedSentence=temp.substring(0,1).toUpperCase()+temp.substring(1);
        //original Sentence
        englishSentence=new Sentence(sentence,userId,audioUrl,reported,timestamp,translatedKhasi,noOfVotes,userName,category,translatedEnglish,translatedHindi,true);
        //new Sentence
        newUserSentence=new Sentence(translatedSentence,currentUserId,uploadedFileDownloadUrl,0,new Date().getTime(),translatedKhasi,0,currentUserName,category,true,false,true);

        HashMap<String, Object> updatedUserData = new HashMap<>();

        updatedUserData.put(pathtoGaroSentence+ garoSentenceId, newUserSentence);
        updatedUserData.put(pathFromLangSentence+sentenceId, englishSentence);
        updatedUserData.put(pathFromEngToGaroSentence+sentenceId+"/" + garoSentenceId, true);
        updatedUserData.put(pathFromGaroToEngSentence+garoSentenceId+"/" + sentenceId, true);

//        updatedUserData.put("garo_sentence/" + garoSentenceId, newUserSentence);
//        updatedUserData.put("english_sentence/"+sentenceId, englishSentence);
//        updatedUserData.put("english_garo_sentence/"+sentenceId+"/" + garoSentenceId, true);
//        updatedUserData.put("garo_english_sentence/"+garoSentenceId+"/" + sentenceId, true);

        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null)
                {
                    Toast.makeText(SentenceViewActivity.this, "Error Adding Translation", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(SentenceViewActivity.this, "Translation Added", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                translatedGaro=true;
                if(fromLang.equals("KHA"))
                    translatedKhasi=true;
                else
                translatedEnglish=true;
                answerBtn.setEnabled(true);
            }
        });
    }

    private void uploadFile(final String translatedSentence) {
        //for every type of language we will use different nodes
        sRef=storageReference.child("sentence").child(currentUserId).child(pathtoLangSentence.substring(0,pathtoLangSentence.length()-1)).child(new Date().getTime()+"_"+currentUserId);
        fileUploadUri= Uri.fromFile(new File(outputFile.getAbsolutePath()));
        progressDialog = new ProgressDialog(SentenceViewActivity.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        sRef.putFile(fileUploadUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadedFileDownloadUrl=uri.toString();
                                pushData(translatedSentence);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SentenceViewActivity.this, "File Uploaded, Failed to add translated sentence", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
                Toast.makeText(SentenceViewActivity.this, "Cannot Upload Audio Clip", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot
                        .getTotalByteCount();
                progressDialog.setMessage("Uploaded "+(int)progress+"%");
            }
        });
    }

    private void pushData(String translatedSentence) {
//        Firebase ref = new Firebase("https://<YOUR-FIREBASE-APP>.firebaseio.com");
//// Generate a new push ID for the new post
//        Firebase newPostRef = ref.child("posts").push();
//        String newPostKey = newPostRef.getKey();
//// Create the data we want to update
//        Map newPost = new HashMap();
//        newPost.put("title", "New Post");
//        newPost.put("content", "Here is my new post!");
//        Map updatedUserData = new HashMap();
//        updatedUserData.put("users/posts/" + newPostKey, true);
//        updatedUserData.put("posts/" + newPostKey, newPost);
//// Do a deep-path update
//        ref.updateChildren(updatedUserData, new Firebase.CompletionListener() {
//            @Override
//            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
//                if (firebaseError != null) {
//                    System.out.println("Error updating data: " + firebaseError.getMessage());
//                }
//            }
//        });

        //1. Upload audioFile if file is recorded
        //2. If recorded successfully and get download Url
        //3. Push Data

        String khasiSentenceId=khasiSentenceRef.push().getKey();
        //improvement Capitalize the First Letter in translated text - 28th July
        String temp=translatedSentence.toLowerCase()+"";
        translatedSentence=temp.substring(0,1).toUpperCase()+temp.substring(1);
        //original Sentence
        englishSentence=new Sentence(sentence,userId,audioUrl,reported,timestamp,true,noOfVotes,userName,category,true,translatedHindi,translatedGaro);
        //new Sentence
        khasiSentence=new Sentence(translatedSentence,currentUserId,uploadedFileDownloadUrl,0,new Date().getTime(),true,0,currentUserName,category,true,false,false);

        HashMap<String, Object> updatedUserData = new HashMap<>();

        updatedUserData.put(pathtoLangSentence+ khasiSentenceId, khasiSentence);
        updatedUserData.put(pathFromLangSentence+sentenceId, englishSentence);
        updatedUserData.put(pathfromToLangSentence+sentenceId+"/" + khasiSentenceId, true);
        updatedUserData.put(pathtoFromLangSentence+khasiSentenceId+"/" + sentenceId, true);

//        updatedUserData.put("khasi_sentence/" + khasiSentenceId, khasiSentence);
//        updatedUserData.put("english_sentence/"+sentenceId, englishSentence);
//        updatedUserData.put("english_khasi_sentence/"+sentenceId+"/" + khasiSentenceId, true);
//        updatedUserData.put("khasi_english_sentence/"+khasiSentenceId+"/" + sentenceId, true);

        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null)
                {
                    Toast.makeText(SentenceViewActivity.this, "Error Adding Translation", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(SentenceViewActivity.this, "Translation Added", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                answerBtn.setEnabled(true);
                //SEND FCM
                //send push noti to userId using Json object
                JSONObject notification=new JSONObject();
                JSONObject notificationMessage=new JSONObject();
                JSONObject notificationData=new JSONObject();
                RequestQueue queue = Volley.newRequestQueue(SentenceViewActivity.this);
                String url="https://fcm.googleapis.com/fcm/send ";
                if(!key.equals("")) {
                    if (fromLang.equalsIgnoreCase("Eng") && toLang.equalsIgnoreCase("Kha")) {
                        if (!translatedKhasi) {
                            try {
                                //Log.e("LK","MESSAGE "+"/topics/"+userId);
                                notification.put("title", "Sentence Translated");
                                notification.put("body", "Your sentence " + sentence + " has been translated by " + currentUserName);
                                notification.put("click_action", "com.sngur.learnkhasi.NOTIFICATION");
                                notificationData.put("title", "Sentence");
                                notificationData.put("message", "Sentence");
                                notificationMessage.put("notification", notification);
                                notificationMessage.put("data", notificationData);
                                notificationMessage.put("to", "/topics/" + userId);
                                translatedKhasi = true;
                            } catch (JSONException e) {
                                //e.printStackTrace();
                                Toast.makeText(SentenceViewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    } else if (fromLang.equalsIgnoreCase("Kha") && toLang.equalsIgnoreCase("Eng")) {
                        if (!translatedEnglish) {
                            //translatedKhasi=true;
                            //send push noti to userId using Json object
                            //JSONObject notification=new JSONObject();
                            //JSONObject notificationMessage=new JSONObject();
                            try {

                                notification.put("title", "Sentence Translated");
                                notification.put("body", "Your sentence " + sentence + " has been translated by " + currentUserName);
                                notification.put("click_action", "com.sngur.learnkhasi.NOTIFICATION");
                                notificationData.put("title", "Sentence");
                                notificationData.put("message", "Sentence");
                                notificationMessage.put("notification", notification);
                                notificationMessage.put("to", "/topics/" + userId);
                                translatedEnglish = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    JsonObjectRequest sendNotification= new JsonObjectRequest(Request.Method.POST, url, notificationMessage, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Log.e("LK","DONE"+response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(SentenceViewActivity.this, "Unable to send notification", Toast.LENGTH_SHORT).show();
                        }
                    }
                    )
                    {
                        //header params
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("Content-Type","application/json");
                            params.put("Authorization","key="+key);//never store this and never reuse it
                            return params;
                        }
                    };
                    queue.add(sendNotification);
                }
                //SEND FCM

                translatedKhasi=true;
                translatedEnglish=true;// todo change later
            }
        });
//        rootRef.updateChildren(updatedUserData, new Firebase.CompletionListener() {
//            @Override
//            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
//                if (firebaseError != null) {
//                    System.out.println("Error updating data: " + firebaseError.getMessage());
//                }
//            }
//        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
        }
                break;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("OnDestroy");
        if(myAudioRecorder!=null)
            myAudioRecorder.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sentence_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            //share image
            Toast.makeText(this, "Sharing sentence, please wait", Toast.LENGTH_SHORT).show();
            binding.screen2.setDrawingCacheEnabled(true);
            try {
                Bitmap bitmap = getBitmapFromView(binding.screen2, binding.screen2.getChildAt(0).getHeight(), binding.screen2.getChildAt(0).getWidth());
                //Create File and  then store in Downloads Folder
                File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "Learn Khasi Sentence.jpg");
                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.flush();
                stream.close();
                //if SDK below else
                //Uri uri = Uri.fromFile(file);
                Uri uri  = FileProvider.getUriForFile(SentenceViewActivity.this, "com.sngur.learnkhasi.provider", file);
                //share
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TEXT, "Shared using Learn Khasi App.\n Download it now from https://play.google.com/store/apps/details?id=com.sngur.learnkhasi");
                startActivity(Intent.createChooser(intent, "Share Sentence"));

            } catch (FileNotFoundException e) {
//                e.printStackTrace();
            } catch (Exception e) {
                Toast.makeText(SentenceViewActivity.this,"Unable to Save Image to Storage, Please check app permission",Toast.LENGTH_SHORT).show();
            }//e.printStackTrace();
//Log.d("LOG", "Image is too large, Please Crop the image with a smaller size");

        }
        else  if (id == R.id.action_fav) {
            Toast.makeText(this, "Adding to Favorites", Toast.LENGTH_SHORT).show();
//            saveWord();
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap getBitmapFromView(View screen, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);//added this line of code for bg to be WHITE if screen not scrolled
        Drawable bgDrawable = screen.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        screen.draw(canvas);
        return bitmap;
    }
}