package com.sngur.learnkhasi.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sngur.learnkhasi.adapters.WordSearchResultAdapter;
import com.sngur.learnkhasi.databinding.ActivityFavoriteWordBinding;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.FavoriteWord;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;

import java.util.ArrayList;
import java.util.List;

public class FavoriteWordActivity extends AppCompatActivity {

    ActivityFavoriteWordBinding binding;
    LearnKhasiDatabase db;
    List<FavoriteWord> wordList;
    List <Word> khasiFavWordsList,englishFavWordsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_favorite_word);
        binding=ActivityFavoriteWordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Favorite words");
        //Load all Fav Words
        //set it to RV and hide TV
        //Reuse WordSearch Adapter
        db=LearnKhasiDatabase.getDatabase(this);
        wordList=new ArrayList<>();
        khasiFavWordsList=new ArrayList<>();
        englishFavWordsList=new ArrayList<>();
        loadFavWords();
    }

    private void loadFavWords() {
        class loadWords extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                wordList=db.favoriteWordDao().getAllWords();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //Log.e("FAV",wordList.size()+" - "+wordList.get(0).getFromLanguage());
                if(wordList.size()>0){
                    binding.favoriteKhasiWordsList.setLayoutManager(new LinearLayoutManager(FavoriteWordActivity.this));
                    binding.favoriteKhasiWordsList.setHasFixedSize(true);

                    binding.favoriteEnglishWordsList.setLayoutManager(new LinearLayoutManager(FavoriteWordActivity.this));
                    binding.favoriteEnglishWordsList.setHasFixedSize(true);

                    binding.favWordTextView.setVisibility(View.GONE);
                    for(FavoriteWord word:wordList)
                    {
                        //convert favWord to word
                        Word normalWord=new Word(word.getWordId(),word.getWord(),word.getUserId(),word.getAudioUrl(),word.getReported(),word.getTimestamp(),word.getNoOfVotes(),word.getUserName(),word.getWordType(),word.getEnglishMeaning(),word.getKhasiMeaning(),word.isTranslatedToKhasi(),word.isTranslatedToEnglish(),word.isTranslatedToHindi(),word.isTranslatedToGaro());
                        if(word.getFromLanguage().contains("ENG"))
                        {
                            englishFavWordsList.add(normalWord);
                        }
                        else
                        {
                            khasiFavWordsList.add(normalWord);
                        }
                    }
                    if(englishFavWordsList.size()>0) {
                        WordSearchResultAdapter adapter=new WordSearchResultAdapter(FavoriteWordActivity.this,englishFavWordsList,"English");
                        binding.favoriteEnglishWordsList.setAdapter(adapter);
                    }
                    if(khasiFavWordsList.size()>0) {
                        WordSearchResultAdapter adapter=new WordSearchResultAdapter(FavoriteWordActivity.this,khasiFavWordsList,"Khasi");
                        binding.favoriteKhasiWordsList.setAdapter(adapter);
                    }
                }
            }
        }

        new loadWords().execute();
    }
}