package com.sngur.learnkhasi.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.WordSearchResultAdapter;
import com.sngur.learnkhasi.databinding.ActivityLessonWordsBinding;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.StoredEnglishWord;
import com.sngur.learnkhasi.model.room.StoredKhasiWord;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.StoredEnglishWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredKhasiWordDao;

import java.util.ArrayList;
import java.util.List;

public class LessonWordsActivity extends AppCompatActivity {

    ActivityLessonWordsBinding binding;
    int maxLength;
    List<Word> wordList;
    private DatabaseReference myRef;
    private WordSearchResultAdapter wordSearchResultAdapter;
    private static LearnKhasiDatabase db;
    private String audioUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_lesson_words);
        binding=ActivityLessonWordsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db=LearnKhasiDatabase.getDatabase(this);
        //get the max length
        if(getIntent().getExtras().getString("lesson")!=null)
            maxLength= Integer.parseInt(getIntent().getExtras().getString("lesson"));
        else
            maxLength= 3;//default lesson

        //load the data from DB max 65 items
        wordList=new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("english_word"); //path to english_word //StoredEnglishWords
        //loadOffline
        loadDataInRoom();
    }

    private void loadDataInRoom() {
        class SearchInRoom extends AsyncTask<Void, Boolean, Boolean> {

            List<StoredEnglishWord> storedEnglishWordList;

            public SearchInRoom() {
                wordList.clear();
                storedEnglishWordList = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                    StoredEnglishWordDao storedEnglishWordDao=db.storedEnglishWordDao();
                    storedEnglishWordList=storedEnglishWordDao.findAllWords();
                    if(storedEnglishWordList.size()>0)
                    {
                        for(StoredEnglishWord wordFound: storedEnglishWordList) {
                            if(wordList.size()<30)
                            {
                                if(wordFound.getWord()!=null)
                                    if (wordFound.getWord().length() == maxLength) {
                                        if(wordFound.getAudioUrl()==null)
                                            audioUrl="";
                                        else
                                            audioUrl=wordFound.getAudioUrl();
//                                    Log.d("URL",wordFound.getAudioUrl()+" - "+audioUrl);
                                    wordList.add(new Word(wordFound.getWordId(), wordFound.getWord(), wordFound.getUserId(),audioUrl , wordFound.getReported(), wordFound.getTimestamp(), wordFound.getNoOfVotes(), wordFound.getUserName(), wordFound.getWordType(), wordFound.getEnglishMeaning(), wordFound.getKhasiMeaning(), wordFound.isTranslatedToKhasi(), wordFound.isTranslatedToEnglish(), wordFound.isTranslatedToHindi(), wordFound.isTranslatedToGaro()));
                                }
                            }
                        }
                    }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                binding.lessonWordList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                binding.lessonWordList.setVisibility(View.VISIBLE);
                if (wordList.size() > 0) {
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
                    wordSearchResultAdapter = new WordSearchResultAdapter(getApplicationContext(), wordList, "English","lessons");
                    binding.lessonWordList.setAdapter(wordSearchResultAdapter);
                    wordSearchResultAdapter.notifyDataSetChanged();
                }
                else{
                    //load online
                    loadOnline();
                }

            }
        }

        new SearchInRoom().execute();
    }

    private void loadOnline() {
        //Log.e("LOAD","ONLINE");
        myRef.orderByChild("timestamp").limitToFirst(60).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.lessonWordList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                binding.lessonWordList.setVisibility(View.VISIBLE);
                if(wordList!=null)
                    wordList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Word wordFound = postSnapshot.getValue(Word.class);
                    StoredEnglishWord englishWordFound = postSnapshot.getValue(StoredEnglishWord.class);
                    if(wordFound.getWord()!=null)
                        if(wordFound.getWord().length()==maxLength)
                            wordList.add(wordFound);
                    storeWordToDB(englishWordFound,null,"English");
                }
                if (wordList.size() > 0) {
                    wordSearchResultAdapter = new WordSearchResultAdapter(getApplicationContext(), wordList, "English","lessons");
                    binding.lessonWordList.setAdapter(wordSearchResultAdapter);
                    wordSearchResultAdapter.notifyDataSetChanged();
                }
                binding.layout.shimmer.stopShimmer();
                binding.layout.shimmer.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void storeWordToDB(final StoredEnglishWord wordFound, Object o, final String fromLang) {
        class storeWords extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                    StoredEnglishWordDao storedEnglishWordDao=db.storedEnglishWordDao();
                    if(wordFound.getWordId()!=null)
                        storedEnglishWordDao.insert(wordFound);
                return null;
            }
        }
        storeWords storeWords=new storeWords();
        storeWords.execute();
    }
}