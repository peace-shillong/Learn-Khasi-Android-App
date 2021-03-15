package com.sngur.learnkhasi.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.adapters.SentenceSearchResultAdapter;
import com.sngur.learnkhasi.databinding.ActivityLessonSentencesBinding;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.StoredSentence;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.StoredSentenceDao;

import java.util.ArrayList;
import java.util.List;

public class LessonSentencesActivity extends AppCompatActivity {

    ActivityLessonSentencesBinding binding;
    String category;
    List<Sentence> sentenceList;
    List<String> sentenceId;
    private DatabaseReference myRef;
    private SentenceSearchResultAdapter sentenceSearchResultAdapter;
    private static LearnKhasiDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_lesson_sentences);
        binding=ActivityLessonSentencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(getIntent().getExtras().getString("lesson")!=null)
            category=getIntent().getExtras().getString("lesson");
        else
            category="Greetings";//default lesson
        sentenceList=new ArrayList<>();
        sentenceId=new ArrayList<>();
        db=LearnKhasiDatabase.getDatabase(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("english_sentence"); //path to english_sentence
        loadDataInRoom();
    }

    private void loadOnline() {
        //Log.e("LOAD","ONLINE");
        myRef.orderByChild("category").equalTo(category).limitToFirst(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.lessonSentenceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                binding.lessonSentenceList.setVisibility(View.VISIBLE);
                if(sentenceList!=null)
                    sentenceList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Sentence sentenceFound = postSnapshot.getValue(Sentence.class);
                    //if(sentenceFound.getCategory().equalsIgnoreCase(category))
                    //{
                    sentenceList.add(sentenceFound);
                    sentenceId.add(postSnapshot.getKey() + "");
                    //store it offline
                    storeSentenceInRoom(postSnapshot.getKey() + "", sentenceFound);
                    //}
                }
                if (sentenceList.size() > 0) {
                    sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getApplicationContext(), sentenceList, sentenceId, "English","lessons");
                    binding.lessonSentenceList.setAdapter(sentenceSearchResultAdapter);
                }
                binding.layout.shimmer.stopShimmer();
                binding.layout.shimmer.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void storeSentenceInRoom(final String sentenceId, final Sentence newSentence) {
        class StoreInRoom extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                StoredSentenceDao storedSentenceDao=db.storedSentenceDao();
                StoredSentence storedSentence = new StoredSentence(sentenceId,"English",newSentence.getSentence(),newSentence.getUserId(),newSentence.getAudioUrl(),newSentence.getReported(),newSentence.getTimestamp(),newSentence.getTranslatedToKhasi(),newSentence.getNoOfVotes(),newSentence.getUserName(),newSentence.getCategory(),newSentence.isTranslatedToEnglish(),newSentence.isTranslatedToHindi(),newSentence.isTranslatedToGaro());
                storedSentenceDao.insert(storedSentence);
                return null;
            }
        }

        StoreInRoom storeInRoom=new StoreInRoom();
        storeInRoom.execute();
    }

    private void loadDataInRoom() {


        class SearchInRoom extends AsyncTask<Void,Boolean,Boolean>{

            @Override
            protected Boolean doInBackground(Void... voids) {
                StoredSentenceDao storedSentenceDao=db.storedSentenceDao();
                List<StoredSentence> storedSentences=new ArrayList<>();
                storedSentences = storedSentenceDao.find50Sentence("English",category);
                if(storedSentences.size()>0)
                {
                    for(StoredSentence storedSentence : storedSentences){
                        //add the stored sentences to sentenceList and sentenceId
                        Sentence newSentence=new Sentence(storedSentence.getSentence(),storedSentence.getUserId(),storedSentence.getAudioUrl(),storedSentence.getReported(),storedSentence.getTimestamp(),storedSentence.getTranslatedToKhasi(),storedSentence.getNoOfVotes(),storedSentence.getUserName(),storedSentence.getCategory(),storedSentence.isTranslatedToEnglish(),storedSentence.isTranslatedToHindi(),storedSentence.isTranslatedToGaro());
                        if(newSentence.getCategory().equals(category)) {
                            if(sentenceList.size()<26) {
                                sentenceList.add(newSentence);
                                sentenceId.add(storedSentence.getSentenceId() + "");
                            }
                        }
                    }
                }
                else{
                    loadOnline();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (sentenceList.size() > 0) {
                    //Log.e("DATA","LOAD OFFLINE");
                    binding.lessonSentenceList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    binding.lessonSentenceList.setVisibility(View.VISIBLE);
                    sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getApplicationContext(), sentenceList, sentenceId, "English","lessons");
                    binding.lessonSentenceList.setAdapter(sentenceSearchResultAdapter);
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
                }
            }
        }
        SearchInRoom searchInRoom=new SearchInRoom();
        searchInRoom.execute();
    }
}