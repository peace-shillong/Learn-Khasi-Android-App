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
import android.speech.tts.TextToSpeech;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.WordSearchResultTranslatedWordsAdapter;
import com.sngur.learnkhasi.databinding.ActivityWordViewBinding;
import com.sngur.learnkhasi.model.Counter;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.Editors;
import com.sngur.learnkhasi.model.room.FavoriteWord;
import com.sngur.learnkhasi.model.room.UserSentenceReported;
import com.sngur.learnkhasi.model.room.UserWordReported;
import com.sngur.learnkhasi.model.room.UserWordVoted;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.EditorsDao;
import com.sngur.learnkhasi.roomdb.dao.FavoriteWordDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceReportedDao;
import com.sngur.learnkhasi.roomdb.dao.UserWordReportedDao;
import com.sngur.learnkhasi.roomdb.dao.UserWordVotesDao;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class WordViewActivity extends AppCompatActivity {

    //75% DONE
    ActivityWordViewBinding binding;
    private Bundle bundle;
    private String wordId,wordIdToWord,fromLang,toLang,word,wordToWord,userId,userIdToWord,audioUrl,currentUserId,userName,wordType,englishMeaning,khasiMeaning,currentUserName,displayKhasiMeaning,displayEnglishMeaning,displayMeaning;
    private boolean translatedKhasi,translatedEnglish,translatedHindi,translatedGaro,translatedKhasiToWord,translatedEnglishToWord,translatedHindiToWord,translatedGaroToWord;
    private String audioUrlToWord,userNameToWord,wordTypeToWord,englishMeaningToWord,khasiMeaningToWord;
    private Long timestamp,timestampToWord;
    private int reported,noOfVotes,noOfVotesToWord,reportedToWord;
    private FirebaseDatabase database;
    private DatabaseReference rootRef,fromToWordRef,toFromWordRef,fromWordRef,toWordRef,garoWordRef,garoToEngRef,engToGaroRef,garoToKhaRef,khaToGaroRef;
    private int english, hindi, garo, khasi, level;
    private SharedPreferences preferences;
    private AlertDialog alertDialog;
    private MediaRecorder myAudioRecorder;
    private List<Word> translatedWordList,translatedGaroWordList;
    private int maxVotedWord;

    private File outputFile;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private TextView recTxt,translatedWord,englishMeaningTxt,khasiMeaningTxt;
    private TextInputLayout translatedWordHint;
    private Boolean fileRecorded;
    private ImageButton record,stop,play;
    private Button answerBtn;
    //firebase storage
    private StorageReference storageReference,sRef;
    Uri fileUploadUri;
    String uploadedFileDownloadUrl;
    private ProgressDialog progressDialog;
    private Word updatedWord;

    private static LearnKhasiDatabase db;
    private MediaPlayer mediaPlayer;
    private String pathFromLangWord,pathToLangWord,pathFromToLangWord,pathToFromLangWord,pathToWordVote,pathFromWordVote,pathWordReport;
    private Word englishWord,khasiWord,garoWord;
    private static List<String> votedWordId,reportedWordId;
    private static List<UserWordVoted> votedWords;
    private String updateUserId,msg,pathGaroWord,pathFromGaroToEngWord,pathFromGaroToKhaWord,pathFromEngToGaroWord,pathFromKhaToGaroWord;
    private DatabaseReference userPointsRef,myRef;
    private int userPoints,userToWordPoints;
    private int finalNoOfVotes;
    private List<String> wordTypeList;
    private Spinner wordTypeSpinner;
    private ArrayAdapter<String> arrayAdapter;
    private TextToSpeech tts;
    private String toWordTTS;
    private TextInputEditText translatedWordTxt;
    private List<Editors> editorsList;
    public static Editors editor;
    private String key,activity;

    //TO DO here in the activity
    //get userId and fromLanguage and toLanguage from intent
    // if currentUser has translated the sentence for the first time then send push notification to topic : userId

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word_view);
        binding=ActivityWordViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestPermissions();//request Storage Permission
        db=LearnKhasiDatabase.getDatabase(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        votedWordId=new ArrayList<>();
        reportedWordId=new ArrayList<>();
        votedWords=new ArrayList<>();
        getUserVoteDataWordView();
        getUserReportedData(); //load the reported words
        userPoints=0;
        userToWordPoints=0;
        //get the data from prev activity and display
        bundle=getIntent().getExtras();
        wordId=bundle.getString("wordId");
        wordIdToWord="";//This is the Id of the toWord displayed in the right side of the activity
        fromLang=bundle.getString("fromLanguage");
        toLang=bundle.getString("toLanguage");
        word=bundle.getString("word");
        userId=bundle.getString("userId");
        userIdToWord="";
        timestamp=bundle.getLong("timestamp");
        audioUrl=bundle.getString("audioUrl");
        reported=bundle.getInt("reported");
        translatedKhasi=bundle.getBoolean("translatedKhasi");
        noOfVotes=bundle.getInt("noOfVotes");
        noOfVotesToWord=0;
        //new data as on 29th July
        userName=bundle.getString("userName");
        wordType=bundle.getString("wordType");
        translatedEnglish=bundle.getBoolean("translatedEnglish");
        translatedHindi=bundle.getBoolean("translatedHindi");
        translatedGaro=bundle.getBoolean("translatedGaro");
        englishMeaning=bundle.getString("englishMeaning");
        khasiMeaning=bundle.getString("khasiMeaning");
        activity=bundle.getString("activity");
        if(englishMeaning==null)
            englishMeaning="";
        if(khasiMeaning==null)
            khasiMeaning="";
        preferences=getSharedPreferences("LearnKhasi", 0);
        khasi=preferences.getInt("khasi",0);
        english=preferences.getInt("english",0);
        hindi=preferences.getInt("hindi",0);
        garo=preferences.getInt("garo",0);
        level=preferences.getInt("level",1);
        currentUserId=preferences.getString("gid","");
        currentUserName=preferences.getString("name","N/A ");
        if(currentUserName.contains(" ")) // fix for users with only one word name
            currentUserName=currentUserName.substring(0,currentUserName.indexOf(" "));
//        audioUrlToWord="";

        wordTypeList=new ArrayList<>();
        wordTypeList.add("Noun");
        wordTypeList.add("Verb");
        wordTypeList.add("Pronoun");
        wordTypeList.add("Preposition");
        wordTypeList.add("Adjective");
        wordTypeList.add("Adverb");
        wordTypeList.add("Conjunction");
        wordTypeList.add("Interjection");
        arrayAdapter = new ArrayAdapter<String>(WordViewActivity.this, android.R.layout.simple_list_item_1, wordTypeList);

        editorsList=new ArrayList<>();
        loadEditors();

        //From Word Display
        binding.fromWord.setText(word);
        displayKhasiMeaning="";
        displayEnglishMeaning="";
        displayMeaning="";
        if(!khasiMeaning.equals("")) {
            displayKhasiMeaning="Khasi Meaning:\n" + khasiMeaning + "\n";
            displayMeaning=displayKhasiMeaning;
            binding.fromWordMeaning.setText(displayKhasiMeaning);
        }
        if(!englishMeaning.equals("")) {
            displayEnglishMeaning="\nEnglish Meaning:\n" + englishMeaning;
            displayMeaning=binding.fromWordMeaning.getText().toString() +englishMeaning;
            binding.fromWordMeaning.setText(displayMeaning);
        }
        binding.wordTypeFromWord.setText(wordType);
        binding.noOfVotesFromWord.setText(""+noOfVotes);
        binding.fromWordLanguage.setText(fromLang);
        binding.toWordLanguage.setText(toLang);
        binding.userNameFromWord.setText("Added by: "+userName);
        maxVotedWord=-1;
        fileRecorded=false;
        //load the translated Word
        database=FirebaseDatabase.getInstance();
        rootRef=database.getReference();
        //outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        outputFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "Learn Khasi.3gp");
        //for garo words
        pathGaroWord="garo_word/";
        pathFromGaroToEngWord="garo_english_word/";
        pathFromGaroToKhaWord="garo_khasi_word/";
        pathFromEngToGaroWord="english_garo_word/";
        pathFromKhaToGaroWord="khasi_garo_word/";
        translatedGaroWordList=new ArrayList<>();
        garoWordRef=database.getReference("garo_word");
        garoToEngRef=database.getReference("garo_english_word");
        engToGaroRef=database.getReference("english_garo_word");
        garoToKhaRef=database.getReference("garo_khasi_word");;
        khaToGaroRef=database.getReference("khasi_garo_word");
        wordToWord="";
        pathWordReport="word_reported/";
        if(fromLang.equalsIgnoreCase("Eng") && toLang.equalsIgnoreCase("Kha"))
        {
            binding.fromWordLanguage.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            binding.toWordLanguage.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            binding.fromWordLanguage.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.toWordLanguage.setTextColor(getResources().getColor(R.color.colorWhite));
            fromToWordRef=database.getReference("english_khasi_word");
            fromWordRef=database.getReference("english_word");
            toWordRef=database.getReference("khasi_word");
            toFromWordRef=database.getReference("khasi_english_word");
            //paths to firebase
            pathFromLangWord="english_word/";
            pathToLangWord="khasi_word/";
            pathFromToLangWord="english_khasi_word/";
            pathToFromLangWord="khasi_english_word/";
            //update paths to firebase
            pathToWordVote="khasi_word_vote/";
            pathFromWordVote="english_word_vote/";
            //pathWordReport="word_reported/";//words reported by user
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.playAudioFromWord.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
                binding.playAudioFromWord.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            }
            //use TTS
            binding.playAudioFromWord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if(tts==null)
                        tts = new TextToSpeech(WordViewActivity.this, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status == TextToSpeech.SUCCESS) {
                                    int result = tts.setLanguage(Locale.US);
                                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                        Toast.makeText(WordViewActivity.this, "This Language is not supported in your phone", Toast.LENGTH_SHORT).show();
                                    } else {
                                        tts.setLanguage(Locale.US);
                                        tts.speak(word, TextToSpeech.QUEUE_ADD, null);
                                        tts=null;
                                    }
                                } else
                                    Toast.makeText(WordViewActivity.this, "Unable to play audio", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch (Exception e)
                    {

                    }
                }
            });


        }
        else if(fromLang.equalsIgnoreCase("Kha") && toLang.equalsIgnoreCase("Eng"))
        {
            binding.fromWordLanguage.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            binding.toWordLanguage.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            binding.fromWordLanguage.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.toWordLanguage.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.toWordsTitle.setText(R.string.similar_eng_word);
            fromToWordRef=database.getReference("khasi_english_word");
            fromWordRef=database.getReference("khasi_word");
            toWordRef=database.getReference("english_word");
            toFromWordRef=database.getReference("english_khasi_word");

            pathFromLangWord="khasi_word/";
            pathToLangWord="english_word/";
            pathFromToLangWord="khasi_english_word/";
            pathToFromLangWord="english_khasi_word/";
            //update paths to firebase
            pathFromWordVote="english_word_vote/";
            pathToWordVote="khasi_word_vote/";
            //pathWordReport="word_reported/";//words reported by user


        }
        else if(fromLang.equalsIgnoreCase("Garo") && toLang.equalsIgnoreCase("Eng"))//ENG-Garo
        {
            binding.fromWordLanguage.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            binding.toWordLanguage.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            binding.fromWordLanguage.setTextColor(getResources().getColor(R.color.colorWhite));
            binding.toWordLanguage.setTextColor(getResources().getColor(R.color.colorWhite));
            fromToWordRef=database.getReference("garo_english_word");
            fromWordRef=database.getReference("garo_word");
            toWordRef=database.getReference("english_word");
            toFromWordRef=database.getReference("english_garo_word");
            pathFromLangWord="garo_word/";
            pathToLangWord="english_word/";
            pathFromToLangWord="garo_english_word/";
            pathToFromLangWord="english_garo_word/";
            //update paths to firebase
            pathFromWordVote="english_word_vote/";
            pathToWordVote="garo_word_vote/";

            binding.toGaroCard.setVisibility(View.GONE);
            binding.toWordsTitle.setText(R.string.similar_eng_word);

        }
//        Log.e("DATA",wordId+" - "+fromLang+" "+toLang);
        translatedWordList=new ArrayList<>();//list of Translated Words

        //upVote & downVote the From Word
        binding.voteUpFromWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean vote=false;
                for (int i = 0; i < votedWordId.size(); i++) {
                    if (votedWords.get(i).getWordId().equals(wordId)) {
                        vote = votedWords.get(i).isVote();
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
                                    updateUserVoteDownData(noOfVotes,true,wordId,userPoints,"fromWord");
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteDownData(noOfVotes,true,wordId,userPoints,"fromWord");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //hide VoteUp
                    binding.voteUpFromWord.setVisibility(View.GONE);
                    binding.voteDownFromWord.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(WordViewActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.voteDownFromWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean vote=true;
                //fixed this using loop only
                for (int i = 0; i < votedWordId.size(); i++) {
                    //undetstood cuz I reversed the list
                    if (votedWords.get(i).getWordId().equals(wordId)){
                        vote = votedWords.get(i).isVote();
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
                                    updateUserVoteDownData(noOfVotes,false,wordId,userPoints,"fromWord");
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteDownData(noOfVotes,false,wordId,userPoints,"fromWord");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //hide voteDown
                    binding.voteUpFromWord.setVisibility(View.VISIBLE);
                    binding.voteDownFromWord.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(WordViewActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //toWord UpVote & downVote
        binding.voteUpToWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fixed on 10
               userVoteUpToWord();
            }
        });

        binding.voteDownToWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserVoteDataWordId();
            }
        });

        //Report the fromWord
        binding.reportFromWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(level>1) {
                    //check if user has already reported the sentence
                    boolean isReported = false;
                    for (int i = 0; i < reportedWordId.size(); i++) {
                        if (reportedWordId.get(i).equals(wordId)) {
                            isReported = true;
                            Toast.makeText(WordViewActivity.this, "You have already reported this word", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (!isReported) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WordViewActivity.this);
                        builder.setTitle("Report Word");
                        builder.setMessage("Is this word invalid or wrong?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Report Invalid and store to phone
                                reported += 1;
                                updatedWord = new Word(wordId+"",binding.fromWord.getText().toString(), userId, audioUrl, reported, timestamp, noOfVotes,  userName, wordType, englishMeaning,khasiMeaning, translatedKhasi,translatedEnglish, translatedHindi, translatedGaro);
                                //report and negative 5 points
                                userPointsRef=FirebaseDatabase.getInstance().getReference();
                                userPointsRef.child("user_points/"+userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int pointsExist = (int) snapshot.getChildrenCount();
                                        if(pointsExist>0)
                                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                                userPoints = postSnapshot.getValue(Integer.class);
                                                //minus points
                                                userPoints-=5;
                                                //int deducedPoint=userPoints;
                                                reportFromWord();
                                                break;
                                            }
                                        else{
                                            //new data for user
                                            //Update Like data
                                            reportFromWord();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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
                    Toast.makeText(WordViewActivity.this, "You can report words as invalid once you reach level 2", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Report the toWord
        binding.reportToWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(level>1) {
                    //check if user has already reported the sentence
                    boolean isReported = false;
                    for (int i = 0; i < reportedWordId.size(); i++) {
                        if (reportedWordId.get(i).equals(wordIdToWord)) {
                            isReported = true;
                            Toast.makeText(WordViewActivity.this, "You have already reported this word", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (!isReported) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WordViewActivity.this);
                        builder.setTitle("Report Word");
                        builder.setMessage("Is this word invalid or wrong?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Report Invalid and store to phone
                                reportedToWord += 1;
                                updatedWord = new Word(wordIdToWord+"",binding.toWord.getText().toString(), userIdToWord, audioUrlToWord, reportedToWord, timestampToWord, noOfVotesToWord,  userNameToWord, wordTypeToWord, englishMeaningToWord,khasiMeaningToWord, translatedKhasiToWord,translatedEnglishToWord, translatedHindiToWord, translatedGaroToWord);

                                userPointsRef=FirebaseDatabase.getInstance().getReference();
                                userPointsRef.child("user_points/"+userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int pointsExist = (int) snapshot.getChildrenCount();
                                        if(pointsExist>0)
                                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                                userPoints = postSnapshot.getValue(Integer.class);
                                                //minus points
                                                userPoints-=5;
                                                //int deducedPoint=userPoints;
                                                reportToWord();
                                                break;
                                            }
                                        else{
                                            //new data for user
                                            //Update Like data
                                            reportToWord();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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
                    Toast.makeText(WordViewActivity.this, "You can report words as invalid once you reach level 2", Toast.LENGTH_SHORT).show();
                }
            }
        });

        key="";
        getAPIKey();
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

    private void loadFromToWordFromFB() {
        fromToWordRef.child(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int noOfTranslatedWords= (int) snapshot.getChildrenCount();
                //Log.e("WORDS",noOfTranslatedWords+"");
                if(noOfTranslatedWords>0)
                    binding.noKhasiWords.setVisibility(View.GONE);
                if(noOfTranslatedWords<=level+1) {
                    if (english == 1 && khasi == 1) //eng - kha
                        binding.answerBtn.setVisibility(View.VISIBLE);
                    //Disable button for lessons Activity
                    if(activity.equals("lessons"))
                        binding.answerBtn.setEnabled(false);
                    //Add Khasi/English Word Translation
                    binding.answerBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Toast.makeText(WordViewActivity.this, "What is ", Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(WordViewActivity.this);
                            ViewGroup viewGroup = findViewById(android.R.id.content);
                            View dialogView = LayoutInflater.from(WordViewActivity.this).inflate(R.layout.dialog_layout_translated_word, viewGroup, false);
                            builder.setView(dialogView);
                            translatedWord = dialogView.findViewById(R.id.translatedWord);
                            translatedWordHint = dialogView.findViewById(R.id.translatedWordHint);
                            if (fromLang.equals("ENG")) {
                                translatedWordHint.setHint("Enter the Khasi Word");
                            }
                            else if (fromLang.equals("KHA")) {
                                translatedWordHint.setHint("Enter the English Word");
                                LinearLayout bigLevel=dialogView.findViewById(R.id.bigLevel);
                                bigLevel.setVisibility(View.GONE);
                            }
                            record = dialogView.findViewById(R.id.record);
                            play = dialogView.findViewById(R.id.play);
                            answerBtn = dialogView.findViewById(R.id.buttonAnswer);
                            recTxt = dialogView.findViewById(R.id.recording);
                            stop = dialogView.findViewById(R.id.pause);
                            englishMeaningTxt = dialogView.findViewById(R.id.englishMeaning);
                            khasiMeaningTxt = dialogView.findViewById(R.id.khasiMeaning);
                            wordTypeSpinner=dialogView.findViewById(R.id.translatedWordType);
                            wordTypeSpinner.setAdapter(arrayAdapter);
                            wordTypeSpinner.setSelection(wordTypeList.indexOf(wordType));
                            record.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //check for File and Microphone Permission
                                    requestPermissions();
                                    //fileRecorded=false;
                                    if (level <= 2) {
                                        Toast.makeText(WordViewActivity.this, "You need to increase your level to be able to Record Audio Clip " + level, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    //start recording
                                    //hide this icon use the pause icon
                                    if (checkPermission()) {
                                        try {
                                            if (myAudioRecorder != null) {
                                                myAudioRecorder.release();
                                                myAudioRecorder = null;
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
                                        } catch (Exception e) {

//                                            Log.e("DATA", e.getMessage() + "-" + e.getCause());
//                                            e.printStackTrace();
                                            Toast.makeText(WordViewActivity.this, "Unable to record", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(WordViewActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
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
                                    fileRecorded = true;
                                    recTxt.setVisibility(View.INVISIBLE);
                                    record.setVisibility(View.VISIBLE);
                                    stop.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Audio Recorded successfully", Toast.LENGTH_LONG).show();
                                }
                            });

                            play.setEnabled(false);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlack)));
                            }
                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (level <= 2) {
                                        Toast.makeText(WordViewActivity.this, "You need to increase your level to be able to Play Recorded Audio Clip", Toast.LENGTH_SHORT).show();
                                    } else {
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
                            InputFilter filter = new InputFilter() {
                                public CharSequence filter(CharSequence source, int start, int end,
                                                           Spanned dest, int dstart, int dend) {
                                    for (int i = start; i < end; i++) {
                                        if (Character.isWhitespace(source.charAt(i))) {
                                            return "";
                                        }
                                    }
                                    return null;
                                }
                            };
                            translatedWord.setFilters(new InputFilter[]{filter});
                            answerBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (translatedWord.getText() != null) {
                                        if(translatedWord.getText().length()>=2)
                                        {
                                            answerBtn.setEnabled(false);
                                            if (fileRecorded)
                                                uploadFile(translatedWord.getText().toString());
                                            else
                                                pushData(translatedWord.getText().toString());
                                        }
                                        else {
                                            Toast.makeText(WordViewActivity.this, "Enter more than 2 characters for the word", Toast.LENGTH_SHORT).show();
                                            translatedWord.setError("Please enter more than 2 characters for the word");
                                        }

                                    } else {
                                        translatedWord.setError("Please enter a word");
                                        Toast.makeText(WordViewActivity.this, "Please enter a word", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                    });
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //getting list of khasi word for the wordId
                    translatedWordList.clear();
                    //Log.d("DATA ID","GOT SOME DATA");
                    String toWordId=postSnapshot.getKey();
                    //wordList.add(word);
                    //Log.d("ID",postSnapshot.getKey()+" - "+toWordId);
                    //wordId.add(postSnapshot.getKey()+"");
                    //for each key then get khasi_word object in Single Data mo
                    toWordRef.child(toWordId).addValueEventListener(new ValueEventListener() {
                        int index=0;
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //Log.d("DATA", "Translated Word Id " + snapshot.getKey()+" - size "+translatedWordList.size());
                            Word word = snapshot.getValue(Word.class);
                            //Log.e("NEW WORD ID",word.getWordId());
                            //remove for each data then add new data bug in translated list
                            Word toRemove=null;
                            try {
                                for (Word newTranslatedWord : translatedWordList) {
                                    if (newTranslatedWord.getWordId().equalsIgnoreCase(word.getWordId())) {
                                        toRemove=newTranslatedWord;
                                        //Log.e("DAA", "ERR");
                                        //translatedWordList.add(word); //add new data
                                    }
                                }
                                if(toRemove!=null) {
                                    translatedWordList.remove(toRemove);//remove the old data
                                    //translatedWordList.add(word);//add the new data
                                }
                                //if(toRemove!=null && !toRemove.getWordId().equalsIgnoreCase(word.getWordId()))

                            }catch (Exception e)
                            {
                                //do nothing
                            }
                            translatedWordList.add(word);
                            //if Translated words are there then display here
                            if(translatedWordList.size()>0){
                                //Log.e("DATAS",translatedWordList.get(0).getWord()+" DATA");
                                WordSearchResultTranslatedWordsAdapter mAdapter=new WordSearchResultTranslatedWordsAdapter(translatedWordList,WordViewActivity.this,currentUserId,noOfVotes,fromLang,toLang,wordId,votedWordId,votedWords,editorsList);
                                binding.toWordsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                binding.toWordsList.setHasFixedSize(true);
                                binding.toWordsList.setAdapter(mAdapter);
                                //Log.d("DATA","SIZE");
                                try{
                                for(Word translatedWord: translatedWordList)
                                    if(translatedWord.getNoOfVotes()>maxVotedWord) {
                                        audioUrlToWord=null;
                                        maxVotedWord = translatedWord.getNoOfVotes();
                                        wordIdToWord= translatedWord.getWordId();
                                        userIdToWord= translatedWord.getUserId();
                                        binding.toWord.setText(translatedWord.getWord());
                                        wordToWord=translatedWord.getWord()+"";
                                        toWordTTS=translatedWord.getWord();
                                        wordTypeToWord=translatedWord.getWordType();
                                        binding.wordTypeToWord.setText(wordTypeToWord);
                                        userNameToWord=translatedWord.getUserName()+"";
                                        binding.userNameToWord.setText("Added by: "+userNameToWord);
                                        noOfVotesToWord=translatedWord.getNoOfVotes();
                                        binding.noOfVotesToWord.setText(""+noOfVotesToWord);
                                        binding.wordTo.setVisibility(View.VISIBLE);
                                        binding.wordDivider.setVisibility(View.VISIBLE);
                                        if(translatedWord.getAudioUrl()!=null)
                                            audioUrlToWord=translatedWord.getAudioUrl()+"";
                                        timestampToWord=translatedWord.getTimestamp();
                                        translatedKhasiToWord=translatedWord.isTranslatedToKhasi();
                                        translatedEnglishToWord=translatedWord.isTranslatedToEnglish();
                                        translatedGaroToWord=translatedWord.isTranslatedToGaro();
                                        translatedHindiToWord=translatedWord.isTranslatedToHindi();
                                        //play khasi word
                                        //Log.e("DATAS tolang",toLang);
                                        if(audioUrlToWord!=null && !audioUrlToWord.equals("") && !audioUrlToWord.equals("null") && !toLang.equalsIgnoreCase("ENG"))
                                        {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                binding.playAudioToWord.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                                            }
                                            //binding.playAudioToWord.setImageDrawable(getDrawable(R.drawable.ic_baseline_volume_up_24));
//                                            Log.d("IMAGE",audioUrlToWord+audioUrlToWord+" - "+!audioUrlToWord.equals("")+!toLang.equalsIgnoreCase("ENG"));
                                            binding.playAudioToWord.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
                                            binding.playAudioToWord.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    //play audio from url
                                                    if(mediaPlayer!=null){
                                                        mediaPlayer.release();
                                                        mediaPlayer=null;
                                                        mediaPlayer=new MediaPlayer();
                                                    }
                                                    else {
                                                        mediaPlayer=new MediaPlayer();
                                                    }
                                                    try {
                                                        mediaPlayer.setDataSource(audioUrlToWord);
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
                                                                Toast.makeText(WordViewActivity.this, "Playing Audio", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                            @Override
                                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                                Toast.makeText(WordViewActivity.this, "Audio Completed", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                        //Toast.makeText(WordViewActivity.this, "Playing Audio", Toast.LENGTH_LONG).show();
                                                    } catch (Exception e) {
                                                        // make something
                                                        //Log.d("ERROR",e.getMessage()+"");
//                                                                    e.printStackTrace();
                                                    }
                                                }
                                            });

                                        }
                                        englishMeaningToWord=translatedWord.getEnglishMeaning();
                                        khasiMeaningToWord=translatedWord.getKhasiMeaning();

                                        //Play English TTS
                                        if(toLang.equalsIgnoreCase("ENG")) {
                                            binding.playAudioToWord.setColorFilter(getResources().getColor(R.color.colorPrimaryDark));
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                binding.playAudioToWord.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
//                                                    binding.playAudioToWord.setImageDrawable(getDrawable(R.drawable.ic_baseline_volume_up_24));
                                                binding.playAudioToWord.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
                                            }
                                            binding.playAudioToWord.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    tts = new TextToSpeech(WordViewActivity.this, new TextToSpeech.OnInitListener() {
                                                        @Override
                                                        public void onInit(int status) {
                                                            if (status == TextToSpeech.SUCCESS) {
                                                                int result = tts.setLanguage(Locale.US);
                                                                if (result == TextToSpeech.LANG_MISSING_DATA ||
                                                                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                                                    Toast.makeText(WordViewActivity.this, "This Language is not supported", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    tts.setLanguage(Locale.US);
                                                                    tts.speak(toWordTTS, TextToSpeech.QUEUE_ADD, null);
                                                                }
                                                            } else
                                                                Toast.makeText(WordViewActivity.this, "Unable to play audio", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                        //set English and Khasi Meaning
                                        if(!displayKhasiMeaning.equals("")) {
                                            displayKhasiMeaning=displayKhasiMeaning+"\n"+translatedWord.getKhasiMeaning();
                                        }
                                        else if(!translatedWord.getKhasiMeaning().equals("")){
                                            displayKhasiMeaning="Khasi Meaning:\n"+translatedWord.getKhasiMeaning();
                                        }

                                        if(!displayEnglishMeaning.equals("")) {
                                            displayMeaning=displayKhasiMeaning+"\n"+displayEnglishMeaning+"\n"+translatedWord.getEnglishMeaning();
                                            // Log.e("Meaning",displayMeaning);
                                            binding.fromWordMeaning.setText(displayMeaning);
                                        }
                                        else if(!translatedWord.getEnglishMeaning().equals("")){
                                            displayMeaning=displayKhasiMeaning+"\nEnglish Meaning:\n"+translatedWord.getEnglishMeaning();
                                            binding.fromWordMeaning.setText(displayMeaning);
                                            //Log.e("Meaning",displayMeaning);
                                        }
                                        //Log.e("Meaning 2",displayEnglishMeaning);
                                    }

                            }catch(Exception e){
                                    Toast.makeText(WordViewActivity.this, "Unbale to complete operation", Toast.LENGTH_SHORT).show();
                                }
                            }
                            index++;
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
                //Load FromTOWord
                loadFromToWordFromFB();

                //load other Words KHA-GARO
                if(fromLang.equalsIgnoreCase("Kha") && toLang.equalsIgnoreCase("Eng"))
                    loadKhaToGaroWords();
                if(fromLang.equalsIgnoreCase("Eng") && toLang.equalsIgnoreCase("Kha"))
                    loadEngToGaroWords();

                //Edit From Word only
                editor=new Editors(currentUserId);
                Boolean found=false;
                for(Editors user: editorsList)
                {
                    if(user.getUserId().equals(currentUserId)) {
                        found = true;
                        break;
                    }
                }
                if(currentUserId.equals(userId) || found)
                {
                    //Log.d("SIZE",editorsList.size()+"");
                    binding.editFromWordItem.setVisibility(View.VISIBLE);
                    binding.editFromWordItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(WordViewActivity.this);
                            ViewGroup viewGroup = view.findViewById(android.R.id.content);
                            final View dialogView = LayoutInflater.from(WordViewActivity.this).inflate(R.layout.dialog_layout_translated_word, viewGroup, false);
                            builder.setView(dialogView);
                            LinearLayout bigLevel=dialogView.findViewById(R.id.bigLevel);
                            bigLevel.setVisibility(View.GONE);
                            translatedWordTxt=dialogView.findViewById(R.id.translatedWord);
                            englishMeaningTxt=dialogView.findViewById(R.id.englishMeaning);
                            khasiMeaningTxt=dialogView.findViewById(R.id.khasiMeaning);
                            record=dialogView.findViewById(R.id.record);
                            play=dialogView.findViewById(R.id.play);
                            answerBtn=dialogView.findViewById(R.id.buttonAnswer);
                            translatedWordTxt.setText(binding.fromWord.getText());
                            englishMeaningTxt.setText(englishMeaning);
                            khasiMeaningTxt.setText(khasiMeaning);
                            wordTypeSpinner=dialogView.findViewById(R.id.translatedWordType);
                            wordTypeSpinner.setAdapter(arrayAdapter);
                            //set the spinner view selected Id
                            wordTypeSpinner.setSelection(wordTypeList.indexOf(binding.wordTypeFromWord.getText()));
                            //Log.e("DATA",englishMeaningTxt.getText()+"");
                            //add filter for no space
                            InputFilter filter = new InputFilter() {
                                public CharSequence filter(CharSequence source, int start, int end,
                                                           Spanned dest, int dstart, int dend) {
                                    for (int i = start; i < end; i++) {
                                        if (Character.isWhitespace(source.charAt(i))) {
                                            return "";
                                        }
                                    }
                                    return null;
                                }
                            };
                            translatedWordTxt.setFilters(new InputFilter[]{filter});
                            record.setVisibility(View.GONE);
                            play.setVisibility(View.GONE);
                            answerBtn.setText(R.string.edit_translation);
                            answerBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(word.equals(translatedWordTxt.getText().toString())  && englishMeaning.equals(englishMeaningTxt.getText().toString()) && khasiMeaning.equals(khasiMeaningTxt.getText().toString()) )                            {
                                        Toast.makeText(WordViewActivity.this, "Word and meanings have not been changed", Toast.LENGTH_SHORT).show();
                                        alertDialog.dismiss();
                                    }
                                    else if (translatedWordTxt.getText().toString().length()>=2){
                                        //translatedWordList.get(position).getUserId change to currentId later
                                        word=translatedWordTxt.getText().toString();
                                        word=word.substring(0,1).toUpperCase()+word.substring(1).toLowerCase();
                                        updatedWord=new Word(wordId+"",word,userId,audioUrl,reported,timestamp,noOfVotes,userName,wordTypeSpinner.getSelectedItem().toString(),englishMeaningTxt.getText().toString(),khasiMeaningTxt.getText().toString(),translatedKhasi,translatedEnglish,translatedHindi,translatedGaro);
                                        //pushData for Update
                                        HashMap<String, Object> updatedUserWord = new HashMap<>();
                                        //Log.d("DATA",votedWordId.get(position));
                                        //khasi_sentence original - had a problem set to pathtoLangSentence
                                        updatedUserWord.put(pathFromLangWord+wordId+"/", updatedWord);
                                        answerBtn.setEnabled(false);
                                        rootRef.updateChildren(updatedUserWord, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if(error!=null){
                                                    Toast.makeText(WordViewActivity.this, "Unable to update word", Toast.LENGTH_SHORT).show();
                                                }
                                                Toast.makeText(WordViewActivity.this, "Word Updated", Toast.LENGTH_SHORT).show();
                                                //holder.translatedSentence.setText(translatedSentence.getText().toString());
                                                //translatedWordList.get(position).setSentence(translatedSentence.getText().toString());
                                                //notifyItemChanged(position);
                                                //notifyDataSetChanged();
                                                binding.fromWord.setText(word);
                                                alertDialog.dismiss();
                                                answerBtn.setEnabled(true);

                                            }
                                        });
                                    }
                                    else{
                                        //display error
                                        translatedWordTxt.setError("Please enter more than 2 characters for the word");
                                    }
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                    });
                }
            }
        }
        new loadEditors().execute();
    }

    private void userVoteUpToWord() {
        class userVoteUpToWord extends AsyncTask<Void,Void,Void>{
            boolean vote;

            @Override
            protected Void doInBackground(Void... voids) {
                vote=false;
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                //fixed it with db
                vote=userWordVotesDao.getUserVoteById(wordIdToWord);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

//                for (int i = 0; i < votedWordId.size(); i++) {
//                    if (votedWords.get(i).getWordId().equals(wordId)) {
//                        vote = votedWords.get(i).isVote();
//                        break;
//                    }
//                }
                if (!vote) {
                    //Test on Like/Unlike Does user gets points updated or not
                    //Update points
                    updateUserId=userIdToWord;
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
                                    updateUserVoteDownData(noOfVotesToWord,true,wordIdToWord,userPoints,"toWord");
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteDownData(noOfVotesToWord,true,wordIdToWord,userPoints,"toWord");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //Hide
                    binding.voteUpToWord.setVisibility(View.GONE);
                    binding.voteDownToWord.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(WordViewActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getUserVoteDataWordId() {
        class getUserVote extends AsyncTask<Void,Void,Void>
        {
            Boolean vote;
            @Override
            protected Void doInBackground(Void... voids) {
                vote=true;
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                //fixed it with db
                vote=userWordVotesDao.getUserVoteById(wordIdToWord);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                //trying to find in loop
                for (int i = 0; i < votedWordId.size(); i++) {
                    //undetstood cuz I reversed the list
                    if (votedWords.get(i).getWordId().equals(wordIdToWord)){
                        vote = votedWords.get(i).isVote();
                        break;
                    }
                }
                if(vote) {
                    //Update points of user
                    updateUserId=userIdToWord;
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
                                    updateUserVoteDownData(noOfVotesToWord,false,wordIdToWord,userPoints,"toWord");
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteDownData(noOfVotesToWord,false,wordIdToWord,userPoints,"toWord");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //Hide
                    binding.voteUpToWord.setVisibility(View.VISIBLE);
                    binding.voteDownToWord.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(WordViewActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                }
            }
        }
        new getUserVote().execute();
    }

    private void reportToWord() {
        //pushData for Update
        HashMap<String, Object> updatedUserWord = new HashMap<>();
        if(userToWordPoints>0)
            updatedUserWord.put("user_points/"+userIdToWord+"/points",userToWordPoints);
        updatedUserWord.put(pathToLangWord + wordIdToWord + "/", updatedWord);
        updatedUserWord.put(pathWordReport + currentUserId + "/" + wordIdToWord, true);//user's reported word
        rootRef.updateChildren(updatedUserWord, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(WordViewActivity.this, "Unable to report word", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(WordViewActivity.this, "Word reported", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
                storeUserReportedData(wordIdToWord, pathToLangWord.substring(0, pathToLangWord.length() - 1), true);//reported
            }
        });
    }

    private void reportFromWord() {
        //pushData for Update
        HashMap<String, Object> updatedUserWord = new HashMap<>();
        if(userPoints>0)
            updatedUserWord.put("user_points/"+updateUserId+"/points",userPoints);
        updatedUserWord.put(pathFromLangWord + wordId + "/", updatedWord);
        updatedUserWord.put(pathWordReport + currentUserId + "/" + wordId, true);//user's reported sentence
        rootRef.updateChildren(updatedUserWord, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(WordViewActivity.this, "Unable to report word", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(WordViewActivity.this, "Word reported", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
                storeUserReportedData(wordId, pathFromLangWord.substring(0, pathFromLangWord.length() - 1), true);//reported
            }
        });
    }

    //load khasi_garo_words
    private void loadKhaToGaroWords() {
        translatedGaroWordList=new ArrayList<>();
//        Log.e("KHA","KHA_GARO "+wordId);
        khaToGaroRef.child(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int noOfTranslatedWords= (int) snapshot.getChildrenCount();
//                Log.e("WORDS",noOfTranslatedWords+" - " +wordId);
                if(noOfTranslatedWords>0)
                    binding.noGaroWords.setVisibility(View.GONE);
                if(noOfTranslatedWords<=level+1) {
                    if (english == 1 && garo == 1) //eng - kha
                        binding.answerGaroBtn.setVisibility(View.VISIBLE);
                    if(activity.equals("lessons"))
                        binding.answerGaroBtn.setEnabled(false);
                    //Add Garo Word Translation
                    binding.answerGaroBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Toast.makeText(WordViewActivity.this, "What is ", Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(WordViewActivity.this);
                            ViewGroup viewGroup = findViewById(android.R.id.content);
                            View dialogView = LayoutInflater.from(WordViewActivity.this).inflate(R.layout.dialog_layout_translated_word, viewGroup, false);
                            builder.setView(dialogView);
                            translatedWord = dialogView.findViewById(R.id.translatedWord);
                            translatedWordHint = dialogView.findViewById(R.id.translatedWordHint);
                            translatedWordHint.setHint("Enter the Garo Word");
                            record = dialogView.findViewById(R.id.record);
                            play = dialogView.findViewById(R.id.play);
                            answerBtn = dialogView.findViewById(R.id.buttonAnswer);
                            recTxt = dialogView.findViewById(R.id.recording);
                            stop = dialogView.findViewById(R.id.pause);
                            englishMeaningTxt = dialogView.findViewById(R.id.englishMeaning);
                            khasiMeaningTxt = dialogView.findViewById(R.id.khasiMeaning);
                            wordTypeSpinner=dialogView.findViewById(R.id.translatedWordType);
                            wordTypeSpinner.setAdapter(arrayAdapter);
                            wordTypeSpinner.setSelection(wordTypeList.indexOf(wordType));
                            record.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //check for File and Microphone Permission
                                    requestPermissions();
                                    //fileRecorded=false;
                                    if (level <= 2) {
                                        Toast.makeText(WordViewActivity.this, "You need to increase your level to be able to Record Audio Clip " + level, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    //start recording
                                    //hide this icon use the pause icon
                                    if (checkPermission()) {
                                        try {
                                            if (myAudioRecorder != null) {
                                                myAudioRecorder.release();
                                                myAudioRecorder = null;
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
                                        } catch (Exception e) {
//                                            Log.e("DATA", e.getMessage() + "-" + e.getCause());
//                                            e.printStackTrace();
                                            Toast.makeText(WordViewActivity.this, "Unable to record", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(WordViewActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
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
                                    fileRecorded = true;
                                    recTxt.setVisibility(View.INVISIBLE);
                                    record.setVisibility(View.VISIBLE);
                                    stop.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Audio Recorded successfully", Toast.LENGTH_LONG).show();
                                }
                            });

                            play.setEnabled(false);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlack)));
                            }
                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (level <= 2) {
                                        Toast.makeText(WordViewActivity.this, "You need to increase your level to be able to Play Recorded Audio Clip", Toast.LENGTH_SHORT).show();
                                    } else {
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
                            InputFilter filter = new InputFilter() {
                                public CharSequence filter(CharSequence source, int start, int end,
                                                           Spanned dest, int dstart, int dend) {
                                    for (int i = start; i < end; i++) {
                                        if (Character.isWhitespace(source.charAt(i))) {
                                            return "";
                                        }
                                    }
                                    return null;
                                }
                            };
                            translatedWord.setFilters(new InputFilter[]{filter});
                            answerBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (translatedWord.getText() != null) {
                                        if(translatedWord.getText().length()>=2)
                                        {
                                            answerBtn.setEnabled(false);
                                            if (fileRecorded)
                                                uploadGaroFile(translatedWord.getText().toString());
                                            else
                                                pushKhaGaroData(translatedWord.getText().toString());
                                        }
                                        else {
                                            Toast.makeText(WordViewActivity.this, "Enter more than 2 characters for the word", Toast.LENGTH_SHORT).show();
                                            translatedWord.setError("Please enter more than 2 characters for the word");
                                        }

                                    } else {
                                        translatedWord.setError("Please enter a word");
                                        Toast.makeText(WordViewActivity.this, "Please enter a word", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                    });
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //getting list of khasi word for the wordId
                    translatedGaroWordList.clear();
                    //Log.d("DATA ID","GOT SOME DATA");
                    String toWordId=postSnapshot.getKey();
                    //wordList.add(word);
                    //Log.d("ID",postSnapshot.getKey()+" - "+toWordId);
                    //wordId.add(postSnapshot.getKey()+"");
                    //for each key then get khasi_word object in Single Data mo
                    garoWordRef.child(toWordId).addValueEventListener(new ValueEventListener() {
                        //int index=0;
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //Log.d("DATA", "Translated Word Id " + snapshot.getKey()+" - size "+translatedWordList.size());
                            Word word = snapshot.getValue(Word.class);
//                            Log.e("NEW WORD ID",word.getWordId());
                            //remove for each data then add new data bug in translated list
                            Word toRemove=null;
                            try {
                                for (Word newTranslatedWord : translatedGaroWordList) {
                                    if (newTranslatedWord.getWordId().equalsIgnoreCase(word.getWordId())) {
                                        toRemove=newTranslatedWord;
                                        //Log.e("DAA", "ERR");
                                        //translatedWordList.add(word); //add new data
                                    }
                                }
                                if(toRemove!=null) {
                                    translatedGaroWordList.remove(toRemove);//remove the old data
                                    //translatedWordList.add(word);//add the new data
                                }
                            }catch (Exception e)
                            {
                                //do nothing
                            }
                            translatedGaroWordList.add(word);
                            //if Translated words are there then display here
                            if(translatedGaroWordList.size()>0){
//                                Log.e("DATAS",translatedGaroWordList.get(0).getWord()+" DATA");
                                //WordSearchResult - Garo Separately
                                WordSearchResultTranslatedWordsAdapter mAdapter=new WordSearchResultTranslatedWordsAdapter(translatedGaroWordList,WordViewActivity.this,currentUserId,noOfVotes,fromLang,"GARO",wordId,votedWordId,votedWords,editorsList);
                                binding.toGaroWordsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                binding.toGaroWordsList.setHasFixedSize(true);
                                binding.toGaroWordsList.setAdapter(mAdapter);
                            }
                            //index++;
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
    }

    //load english_garo_words
    private void loadEngToGaroWords() {
//        Log.e("ENG","ENG_KHA_GARO "+wordId);
        engToGaroRef.child(wordId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int noOfTranslatedWords= (int) snapshot.getChildrenCount();
//                Log.e("WORDS",noOfTranslatedWords+" - " +wordId);
                if(noOfTranslatedWords>0)
                    binding.noGaroWords.setVisibility(View.GONE);
                if(noOfTranslatedWords<=level+1) {
                    if (khasi == 1 && garo == 1) //eng - kha
                        binding.answerGaroBtn.setVisibility(View.VISIBLE);
                    if(activity.equals("lessons"))
                        binding.answerGaroBtn.setEnabled(false);
                    //Add Garo Word Translation
                    binding.answerGaroBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Toast.makeText(WordViewActivity.this, "What is ", Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(WordViewActivity.this);
                            ViewGroup viewGroup = findViewById(android.R.id.content);
                            View dialogView = LayoutInflater.from(WordViewActivity.this).inflate(R.layout.dialog_layout_translated_word, viewGroup, false);
                            builder.setView(dialogView);
                            translatedWord = dialogView.findViewById(R.id.translatedWord);
                            translatedWordHint = dialogView.findViewById(R.id.translatedWordHint);
                            translatedWordHint.setHint("Enter the Garo Word");
                            record = dialogView.findViewById(R.id.record);
                            play = dialogView.findViewById(R.id.play);
                            answerBtn = dialogView.findViewById(R.id.buttonAnswer);
                            recTxt = dialogView.findViewById(R.id.recording);
                            stop = dialogView.findViewById(R.id.pause);
                            englishMeaningTxt = dialogView.findViewById(R.id.englishMeaning);
                            khasiMeaningTxt = dialogView.findViewById(R.id.khasiMeaning);
                            wordTypeSpinner=dialogView.findViewById(R.id.translatedWordType);
                            wordTypeSpinner.setAdapter(arrayAdapter);
                            wordTypeSpinner.setSelection(wordTypeList.indexOf(wordType));
                            record.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //check for File and Microphone Permission
                                    requestPermissions();
                                    //fileRecorded=false;
                                    if (level <= 2) {
                                        Toast.makeText(WordViewActivity.this, "You need to increase your level to be able to Record Audio Clip " + level, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    //start recording
                                    //hide this icon use the pause icon
                                    if (checkPermission()) {
                                        try {
                                            if (myAudioRecorder != null) {
                                                myAudioRecorder.release();
                                                myAudioRecorder = null;
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
                                        } catch (Exception e) {
//                                            Log.e("DATA", e.getMessage() + "-" + e.getCause());
//                                            e.printStackTrace();
                                            Toast.makeText(WordViewActivity.this, "Unable to record", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(WordViewActivity.this, "Please allow permissions", Toast.LENGTH_SHORT).show();
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
                                    fileRecorded = true;
                                    recTxt.setVisibility(View.INVISIBLE);
                                    record.setVisibility(View.VISIBLE);
                                    stop.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Audio Recorded successfully", Toast.LENGTH_LONG).show();
                                }
                            });

                            play.setEnabled(false);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                play.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlack)));
                            }
                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (level <= 2) {
                                        Toast.makeText(WordViewActivity.this, "You need to increase your level to be able to Play Recorded Audio Clip", Toast.LENGTH_SHORT).show();
                                    } else {
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
                            InputFilter filter = new InputFilter() {
                                public CharSequence filter(CharSequence source, int start, int end,
                                                           Spanned dest, int dstart, int dend) {
                                    for (int i = start; i < end; i++) {
                                        if (Character.isWhitespace(source.charAt(i))) {
                                            return "";
                                        }
                                    }
                                    return null;
                                }
                            };
                            translatedWord.setFilters(new InputFilter[]{filter});
                            answerBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (translatedWord.getText() != null) {
                                        if(translatedWord.getText().length()>=2)
                                        {
                                            answerBtn.setEnabled(false);
                                            if (fileRecorded)
                                                uploadGaroFile(translatedWord.getText().toString());
                                            else
                                                pushGaroData(translatedWord.getText().toString());
                                        }
                                        else {
                                            Toast.makeText(WordViewActivity.this, "Enter more than 2 characters for the word", Toast.LENGTH_SHORT).show();
                                            translatedWord.setError("Please enter more than 2 characters for the word");
                                        }

                                    } else {
                                        translatedWord.setError("Please enter a word");
                                        Toast.makeText(WordViewActivity.this, "Please enter a word", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                    });
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //getting list of khasi word for the wordId
                    translatedGaroWordList.clear();
                    //Log.d("DATA ID","GOT SOME DATA");
                    String toWordId=postSnapshot.getKey();
                    //wordList.add(word);
//                    Log.e("ID",postSnapshot.getKey()+" - "+toWordId);
                    //wordId.add(postSnapshot.getKey()+"");
                    //for each key then get khasi_word object in Single Data mo
                    garoWordRef.child(toWordId).addValueEventListener(new ValueEventListener() {
                        //int index=0;
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //Log.d("DATA", "Translated Word Id " + snapshot.getKey()+" - size "+translatedWordList.size());
                            Word word = snapshot.getValue(Word.class);
                            //Log.e("NEW WORD ID",word.getWordId());
                            //remove for each data then add new data bug in translated list
                            Word toRemove=null;
                            try {
                                for (Word newTranslatedWord : translatedGaroWordList) {
                                    if (newTranslatedWord.getWordId().equalsIgnoreCase(word.getWordId())) {
                                        toRemove=newTranslatedWord;
                                        //Log.e("DAA", "ERR");
                                        //translatedWordList.add(word); //add new data
                                    }
                                }
                                if(toRemove!=null) {
                                    translatedGaroWordList.remove(toRemove);//remove the old data
                                    //translatedWordList.add(word);//add the new data
                                }
                            }catch (Exception e)
                            {
                                //do nothing
                            }
                            translatedGaroWordList.add(word);
                            //if Translated words are there then display here
                            if(translatedGaroWordList.size()>0){
//                                Log.e("DATAS",translatedGaroWordList.get(0).getWord()+" DATA");
                                //WordSearchResult - Garo Separately
                                WordSearchResultTranslatedWordsAdapter mAdapter=new WordSearchResultTranslatedWordsAdapter(translatedGaroWordList,WordViewActivity.this,currentUserId,noOfVotes,fromLang,"GARO",wordId,votedWordId,votedWords,editorsList);
                                binding.toGaroWordsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                binding.toGaroWordsList.setHasFixedSize(true);
                                binding.toGaroWordsList.setAdapter(mAdapter);
                            }
                            //index++;
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
    }

    //Store Reported Word
    private void storeUserReportedData(final String wordId, final String substring, final boolean value) {
        class storeUserReportedData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                UserWordReportedDao userWordReportedDao=db.userWordReportedDao();
                UserWordReported userWordReported=new UserWordReported(wordId,substring,value);
                userWordReportedDao.insert(userWordReported);
                //insert vote in db
                //Log.d("DATA","Stored Data - "+wordId+"-"+value);
                getUserReportedData();
                return null;
            }
        }
        storeUserReportedData storeUserReportedData=new storeUserReportedData();
        storeUserReportedData.execute();
    }

    //Update User/Word Votes fromWord/ToWord
    private void updateUserVoteDownData(int noOfVotes, final boolean likeData, final String localWordId, int userPoints, final String whichWord) {
        finalNoOfVotes = noOfVotes;
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
        //updatedUserData.put("english_word/"+originalwordId+"/noOfVotes", originalwordNoOfVotes);
        if(whichWord.equals("fromWord")) {
            updatedUserData.put(pathFromWordVote + currentUserId + "/" + localWordId, likeData);
            updatedUserData.put(pathFromLangWord + localWordId + "/noOfVotes", finalNoOfVotes);
        }
        else{
            updatedUserData.put(pathToWordVote + currentUserId + "/" + localWordId, likeData);
            updatedUserData.put(pathToLangWord + localWordId + "/noOfVotes", finalNoOfVotes);
        }

        updatedUserData.put("user_points/"+updateUserId+"/points",userPoints);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(WordViewActivity.this, "Unable to vote", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(WordViewActivity.this, msg, Toast.LENGTH_SHORT).show();
                if(whichWord.equals("fromWord")) {
                    storeUserVoteData(localWordId, pathFromLangWord.substring(0, pathFromLangWord.length() - 1), likeData);//updated
                    binding.noOfVotesFromWord.setText(finalNoOfVotes + "");
                }
                else {
                    storeUserVoteData(localWordId, pathToLangWord.substring(0, pathToLangWord.length() - 1), likeData);//updated
                    binding.noOfVotesToWord.setText(finalNoOfVotes + "");
                }
            }
        });
    }

    private void storeUserVoteData(final String wordId,  final String fromLanguage, final boolean vote) {
        class storeUserVoteData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                UserWordVoted userWordVoted=new UserWordVoted(wordId,fromLanguage,vote);
                userWordVotesDao.insert(userWordVoted);
                //insert vote in db
                //Log.d("DATA","Stored Data - "+wordId+"-"+vote);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getUserVoteDataWordView();
            }
        }
        storeUserVoteData storeUserVoteData=new storeUserVoteData();
        storeUserVoteData.execute();
    }

    private void getUserReportedData() {
        class getUserReportedData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                reportedWordId.clear();
                UserWordReportedDao userWordReportedDao=db.userWordReportedDao();
                reportedWordId=userWordReportedDao.getUserReportedWordId();
                //get data from db
                return null;
            }
        }
        getUserReportedData getUserReportedData=new getUserReportedData();
        getUserReportedData.execute();
    }

    public static void getUserVoteDataWordView() {
        class getUserVoteData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                votedWordId.clear();
                votedWords.clear();
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                votedWords=userWordVotesDao.getUserVotes();
                votedWordId=userWordVotesDao.getUserVotedWordId();
                //get data from db
                return null;
            }
        }
        getUserVoteData getUserVoteData=new getUserVoteData();
        getUserVoteData.execute();
    }

    private void pushData(String wordTranslated) {
        String toWordId=toWordRef.push().getKey();
        //improvement Capitalize the First Letter in translated text - 28th July
        String temp=wordTranslated.toLowerCase()+"";
        wordTranslated=temp.substring(0,1).toUpperCase()+temp.substring(1);
        //original Word
        englishWord=new Word(wordId,word,userId,audioUrl,reported,timestamp,noOfVotes,userName,wordType,englishMeaning,khasiMeaning,true, true,translatedHindi,translatedGaro);
        //new word
        khasiWord=new Word(toWordId,wordTranslated,currentUserId,uploadedFileDownloadUrl,0,new Date().getTime(),0,currentUserName,wordTypeSpinner.getSelectedItem().toString(),englishMeaningTxt.getText()+"",khasiMeaningTxt.getText()+"",true,true,translatedHindi,translatedGaro);

        HashMap<String, Object> updatedUserData = new HashMap<>();

        updatedUserData.put(pathToLangWord+ toWordId, khasiWord);
        updatedUserData.put(pathFromLangWord+wordId, englishWord);
        updatedUserData.put(pathFromToLangWord+wordId+"/" + toWordId, true);
        updatedUserData.put(pathToFromLangWord+toWordId+"/" + wordId, true);

//        updatedUserData.put("khasi_word/" + khasiwordId, khasiword);
//        updatedUserData.put("english_word/"+wordId, englishword);
//        updatedUserData.put("english_khasi_word/"+wordId+"/" + khasiwordId, true);
//        updatedUserData.put("khasi_english_word/"+khasiwordId+"/" +wordId, true);


        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null)
                {
                    Toast.makeText(WordViewActivity.this, "Error Adding Translation", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(WordViewActivity.this, "Translation Added", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                answerBtn.setEnabled(true);
                //send push notification if it was translated for the first time
                //send push noti to userId using Json object
                JSONObject notification=new JSONObject();
                JSONObject notificationMessage=new JSONObject();
                JSONObject notificationData=new JSONObject();
                RequestQueue queue = Volley.newRequestQueue(WordViewActivity.this);
                String url="https://fcm.googleapis.com/fcm/send ";
                if(!key.equals("")) {
                    if (fromLang.equalsIgnoreCase("Eng") && toLang.equalsIgnoreCase("Kha")) {
                        if (!translatedKhasi) {
                            //translatedKhasi=true;
                            try {
                                //Log.e("LK","MESSAGE "+"/topics/"+userId);
                                notification.put("title", "Word Translated");
                                notification.put("body", "Your word " + word + " has been translated by " + currentUserName);
                                notification.put("click_action", "com.sngur.learnkhasi.NOTIFICATION");
                                notificationData.put("title", "Word");
                                notificationData.put("message", "Word");
                                notificationMessage.put("notification", notification);
                                notificationMessage.put("data", notificationData);
                                notificationMessage.put("to", "/topics/" + userId);

                                //Log.e("LK",notificationMessage.toString());

                                translatedKhasi = true;
                            } catch (JSONException e) {
                                //e.printStackTrace();
                                Toast.makeText(WordViewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    } else if (fromLang.equalsIgnoreCase("Kha") && toLang.equalsIgnoreCase("Eng")) {
                        if (!translatedEnglish) {
                            //translatedKhasi=true;
                            //send push noti to userId using Json object
                            //JSONObject notification=new JSONObject();
                            //JSONObject notificationMessage=new JSONObject();
                            try {

                                notification.put("title", "Word Translated");
                                notification.put("body", "Your word " + word + " has been translated by " + currentUserName);
                                notification.put("click_action", "com.sngur.learnkhasi.NOTIFICATION");
                                notificationData.put("title", "Word");
                                notificationData.put("message", "Word");
                                notificationMessage.put("notification", notification);
                                notificationMessage.put("to", "/topics/" + userId);
                                notificationMessage.put("data", notificationData);
                                translatedEnglish = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    //PUSH NOTI
                    JsonObjectRequest sendNotification= new JsonObjectRequest(Request.Method.POST, url, notificationMessage, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //Log.e("LK","DONE"+response);
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(WordViewActivity.this, "Unable to send notification", Toast.LENGTH_SHORT).show();
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
                translatedKhasi=true;
                translatedEnglish=true;
            }
        });
    }

    private void uploadFile(final String wordTranslated) {
        //for every type of language we will use different nodes
        sRef=storageReference.child("word").child(currentUserId).child(pathToLangWord.substring(0,pathToLangWord.length()-1)).child(new Date().getTime()+"");
        fileUploadUri= Uri.fromFile(new File(outputFile.getAbsolutePath()));
        progressDialog = new ProgressDialog(WordViewActivity.this);
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
                                pushData(wordTranslated);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(WordViewActivity.this, "File Uploaded, Failed to add translated word", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
                Toast.makeText(WordViewActivity.this, "Cannot Upload Audio Clip", Toast.LENGTH_LONG).show();
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

    //Garo Word
    //push Garo Word when fromLang=Khasi
    private void pushKhaGaroData(String wordTranslated) {
        String toWordId=garoWordRef.push().getKey(); //add new item to garo_word
        //improvement Capitalize the First Letter in translated text - 28th July
        String temp=wordTranslated.toLowerCase()+"";
        wordTranslated=temp.substring(0,1).toUpperCase()+temp.substring(1);
        //original Word --fromWord
        khasiWord=new Word(wordId,word,userId,audioUrl,reported,timestamp,noOfVotes,userName,wordType,englishMeaning,khasiMeaning,true, translatedEnglish,translatedHindi,true);

        //new garo_word
        garoWord=new Word(toWordId,wordTranslated,currentUserId,uploadedFileDownloadUrl,0,new Date().getTime(),0,currentUserName,wordTypeSpinner.getSelectedItem().toString(),englishMeaningTxt.getText()+"",khasiMeaningTxt.getText()+"",translatedKhasi,translatedEnglish,false,true);

        HashMap<String, Object> updatedUserData = new HashMap<>();

        //to word --toWord
        if(!wordToWord.equals("")) {
            englishWord = new Word(wordIdToWord, wordToWord, userIdToWord, audioUrlToWord, reportedToWord, timestampToWord, noOfVotesToWord, userNameToWord, wordTypeToWord, englishMeaningToWord, khasiMeaningToWord+ "", translatedKhasiToWord, translatedEnglishToWord, translatedHindiToWord, true);
            updatedUserData.put(pathToLangWord + wordIdToWord, englishWord);//eng
            updatedUserData.put(pathFromEngToGaroWord+wordIdToWord+"/" + toWordId, true); //translated
            updatedUserData.put(pathFromGaroToEngWord+toWordId+"/" + wordIdToWord, true);
        }

        updatedUserData.put(pathFromLangWord+wordId, khasiWord);//kha
        updatedUserData.put(pathGaroWord+toWordId, garoWord);//garoword
        updatedUserData.put(pathFromKhaToGaroWord + wordId + "/" + toWordId, true); //translated
        updatedUserData.put(pathFromGaroToKhaWord + toWordId + "/" + wordId, true);

//        updatedUserData.put("khasi_word/" + khasiwordId, khasiword);
//        updatedUserData.put("english_word/"+wordId, englishword);
//        updatedUserData.put("english_khasi_word/"+wordId+"/" + khasiwordId, true);
//        updatedUserData.put("khasi_english_word/"+khasiwordId+"/" +wordId, true);

        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null)
                {
                    Toast.makeText(WordViewActivity.this, "Error Adding Translation", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(WordViewActivity.this, "Translation Added", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                translatedKhasi=true;
                translatedGaro=true;
                answerBtn.setEnabled(true);
            }
        });
    }


    //push Garo Word when fromLang=English
    private void pushGaroData(String wordTranslated) {
        String toWordId=garoWordRef.push().getKey(); //add new item to garo_word
        //improvement Capitalize the First Letter in translated text - 28th July
        String temp=wordTranslated.toLowerCase()+"";
        wordTranslated=temp.substring(0,1).toUpperCase()+temp.substring(1);
        //original Word --fromWord
        englishWord=new Word(wordId,word,userId,audioUrl,reported,timestamp,noOfVotes,userName,wordType,englishMeaning,khasiMeaning,translatedKhasi, true,translatedHindi,true);

        //new garo_word
        garoWord=new Word(toWordId,wordTranslated,currentUserId,uploadedFileDownloadUrl,0,new Date().getTime(),0,currentUserName,wordTypeSpinner.getSelectedItem().toString(),englishMeaningTxt.getText()+"",khasiMeaningTxt.getText()+"",translatedKhasi,translatedEnglish,false,true);

        HashMap<String, Object> updatedUserData = new HashMap<>();

        //to word --toWord
        if(!wordToWord.equals("")) {
            khasiWord = new Word(wordIdToWord, wordToWord, userIdToWord, audioUrlToWord, reportedToWord, timestampToWord, noOfVotesToWord, userNameToWord, wordTypeToWord, englishMeaningToWord, khasiMeaningToWord+ "", translatedKhasiToWord, translatedEnglishToWord, translatedHindiToWord, true);
            updatedUserData.put(pathToLangWord + wordIdToWord, khasiWord);//khasiword
            updatedUserData.put(pathFromKhaToGaroWord + wordIdToWord + "/" + toWordId, true); //translated
            updatedUserData.put(pathFromGaroToKhaWord + toWordId + "/" + wordIdToWord, true);
        }
//        Log.e("NEW ID ","ENG "+wordId + " GARO "+toWordId+ " - KHA "+wordIdToWord);
        updatedUserData.put(pathFromLangWord+wordId, englishWord);//englishword
        updatedUserData.put(pathGaroWord+toWordId, garoWord);//garoword
        updatedUserData.put(pathFromEngToGaroWord+wordId+"/" + toWordId, true); //translated
        updatedUserData.put(pathFromGaroToEngWord+toWordId+"/" + wordId, true);


//        updatedUserData.put("khasi_word/" + khasiwordId, khasiword);
//        updatedUserData.put("english_word/"+wordId, englishword);
//        updatedUserData.put("english_khasi_word/"+wordId+"/" + khasiwordId, true);
//        updatedUserData.put("khasi_english_word/"+khasiwordId+"/" +wordId, true);

        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null)
                {
                    Toast.makeText(WordViewActivity.this, "Error Adding Translation", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(WordViewActivity.this, "Translation Added", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                translatedGaro=true;
                translatedEnglish=true;
            }
        });
    }

    private void uploadGaroFile(final String wordTranslated) {
        //for every type of language we will use different nodes
        sRef=storageReference.child("word").child(currentUserId).child(pathGaroWord.substring(0,pathGaroWord.length()-1)).child(new Date().getTime()+"");
        fileUploadUri= Uri.fromFile(new File(outputFile.getAbsolutePath()));
        progressDialog = new ProgressDialog(WordViewActivity.this);
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
                                if(fromLang.equalsIgnoreCase("Eng"))
                                    pushGaroData(wordTranslated);
                                else
                                    pushKhaGaroData(wordTranslated);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(WordViewActivity.this, "File Uploaded, Failed to add translated word", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
                Toast.makeText(WordViewActivity.this, "Cannot Upload Audio Clip", Toast.LENGTH_LONG).show();
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

    @SuppressLint("WrongConstant")
    private void manageBlinkEffect() {
        ObjectAnimator anim = ObjectAnimator.ofInt(recTxt, "textColor", Color.WHITE, Color.RED,
                Color.WHITE);
        anim.setDuration(1200);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(WordViewActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            //share image
            Toast.makeText(this, "Sharing word, please wait", Toast.LENGTH_SHORT).show();
            binding.screen.setDrawingCacheEnabled(true);
            try {
                Bitmap bitmap = getBitmapFromView(binding.screen, binding.screen.getChildAt(0).getHeight(), binding.screen.getChildAt(0).getWidth());
                //Create File and  then store in Downloads Folder
                File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "Learn Khasi Word Share.jpg");
                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.flush();
                stream.close();
                    //if SDK below else
                    //Uri uri = Uri.fromFile(file);
                    Uri uri  = FileProvider.getUriForFile(WordViewActivity.this, "com.sngur.learnkhasi.provider", file);
                    //share
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_TEXT, "Shared using Learn Khasi App.\n Download it now from https://play.google.com/store/apps/details?id=com.sngur.learnkhasi");
                    startActivity(Intent.createChooser(intent, "Share Word"));

                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
                } catch (Exception e) {
                    Toast.makeText(WordViewActivity.this,"Unable to Save Image to Storage, Please check app permission",Toast.LENGTH_SHORT).show();
                }//e.printStackTrace();
//Log.d("LOG", "Image is too large, Please Crop the image with a smaller size");

        }
        else  if (id == R.id.action_fav) {
            Toast.makeText(this, "Adding to Favorites", Toast.LENGTH_SHORT).show();
            saveWord();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveWord() {
        class saveWord extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                //save word here
                FavoriteWordDao favoriteWordDao=db.favoriteWordDao();
                //Log.e("FAV",fromLang+" "+toLang);
                FavoriteWord favWord=new FavoriteWord(wordId,word,userId,audioUrl,reported,timestamp,noOfVotes,userName,wordType,englishMeaning,khasiMeaning,translatedKhasi,translatedEnglish,translatedHindi,translatedGaro,fromLang,toLang);
                favoriteWordDao.insert(favWord);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(WordViewActivity.this, "Added to your favorite words", Toast.LENGTH_SHORT).show();
            }
        }
        new saveWord().execute();
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