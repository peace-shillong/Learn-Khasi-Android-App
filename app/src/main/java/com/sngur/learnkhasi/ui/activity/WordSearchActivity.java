package com.sngur.learnkhasi.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.SentenceSearchResultAdapter;
import com.sngur.learnkhasi.adapters.WordSearchResultAdapter;
import com.sngur.learnkhasi.databinding.ActivityWordSearchBinding;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.StoredEnglishWord;
import com.sngur.learnkhasi.model.room.StoredKhasiWord;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.StoredEnglishWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredKhasiWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredSentenceDao;

import java.util.ArrayList;
import java.util.List;

public class WordSearchActivity extends AppCompatActivity {

    ActivityWordSearchBinding binding;
    private String word,name,fromLang,selectedWordType;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference myRef;
    private int noOfWords,level,limitedAccess;
    private List<Word> wordList;
    private List<String> wordType;
    private SearchView searchView; // for searching in the view all list in offline mode
    private WordSearchResultAdapter wordSearchResultAdapter;
    private static LearnKhasiDatabase db;
    private Integer numberOfWordsInRoom;
    private Boolean noSearch;
    private AlertDialog alertDialog;
    private Boolean translatedEnglish,translatedKhasi;
    private int offset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_word_search);
        binding=ActivityWordSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Dictionary");
        word=getIntent().getExtras().getString("word");
        fromLang=getIntent().getExtras().getString("fromLang");
        binding.wordSearch.setText("Searching for\n "+word);
        binding.askBtn.setEnabled(false);
        preferences =getSharedPreferences("LearnKhasi", 0);
        editor=preferences.edit();
        name=preferences.getString("name","N/A ");
        level=preferences.getInt("level",1);
        if(level==3)
            limitedAccess=preferences.getInt("online",551);
        else
            limitedAccess=preferences.getInt("online",151);
        //Log.d("DATA",name+"-"+word);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(fromLang.equals("Khasi")) {
            myRef = database.getReference("khasi_word"); //path to english sentences
            translatedEnglish=false;
            translatedKhasi=true;
        }
        else {
            myRef = database.getReference("english_word"); //path to english_word
            translatedEnglish=true;
            translatedKhasi=false;
        }
        //myRef = database.getReference();

        if(!name.equals("N/A ")) {
            binding.askBtn.setEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.askBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            }
        }
        else
        {
            binding.loginPlease.setVisibility(View.VISIBLE);
        }
        noOfWords=-1;
        wordList=new ArrayList<>();
        wordType=new ArrayList<>();
        wordType.add("Noun");
        wordType.add("Verb");
        wordType.add("Pronoun");
        wordType.add("Preposition");
        wordType.add("Adjective");
        wordType.add("Adverb");
        wordType.add("Conjunction");
        wordType.add("Interjection");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(WordSearchActivity.this, android.R.layout.simple_list_item_1, wordType);
        binding.wordType.setAdapter(arrayAdapter);

        db=LearnKhasiDatabase.getDatabase(this);
        numberOfWordsInRoom=0;
        noSearch=false;
        offset=0;
        //search();
        countNumberOfWords();

        //Btn to Ask
        binding.askBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Confirm Ask : with Dialog
                android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(WordSearchActivity.this);
                builder.setTitle("Ask Word");
                builder.setMessage("Do you want to ask this word?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //push data to sentence node in FBDB
                        String newWordId=myRef.push().getKey();
                        //Log.e("DATA",newWordId);
                        //Object Map
                        if(name.contains(" "))
                            name=name.substring(0,name.indexOf(" "));
                        if(binding.wordType.getSelectedItem()!=null)
                            selectedWordType=binding.wordType.getSelectedItem().toString();
                        Word newWord=new Word(newWordId+"", word+"",preferences.getString("gid","none")+"","",0, System.currentTimeMillis(),0,name,selectedWordType,"","",false,false,false,false);
                        myRef.child(newWordId).setValue(newWord);
                        Toast.makeText(WordSearchActivity.this, "Asked", Toast.LENGTH_SHORT).show();
                        //finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog=builder.create();
                alertDialog.show();
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
                    storedEnglishWordDao.insert(storedEnglishWord);
                }
                else{
                    StoredKhasiWordDao storedKhasiWordDao=db.storedKhasiWordDao();
                    storedKhasiWordDao.insert(storedKhasiWord);
                }
                return null;
            }
        }
        storeWords storeWords=new storeWords();
        storeWords.execute();
    }

    private  void countNumberOfWords(){
        @SuppressLint("StaticFieldLeak")
        class CountNumberOfWords extends AsyncTask<Void,Void,Void> {
            @Override
            protected Void doInBackground(Void... objects) {
                wordList.clear();
                if(fromLang.equals("Khasi")) {
                    StoredKhasiWordDao storedKhasiWordDao = db.storedKhasiWordDao();
                    int count=storedKhasiWordDao.getCount();
                    if(offset<count) {
                        List<StoredKhasiWord> khasiWordList = storedKhasiWordDao.findAllWords();
                        for (StoredKhasiWord khasiWord : khasiWordList) {
                            wordList.add(new Word(khasiWord.getWordId(), khasiWord.getWord(), khasiWord.getUserId(), khasiWord.getAudioUrl(), khasiWord.getReported(), khasiWord.getTimestamp(), khasiWord.getNoOfVotes(), khasiWord.getUserName(), khasiWord.getWordType(), khasiWord.getEnglishMeaning(), khasiWord.getKhasiMeaning(), khasiWord.isTranslatedToKhasi(), khasiWord.isTranslatedToEnglish(), khasiWord.isTranslatedToHindi(), khasiWord.isTranslatedToGaro()));
                        }
                        offset+=1000;
                    }
                }
                else{
                    StoredEnglishWordDao storedEnglishWordDao = db.storedEnglishWordDao();
                    int count=storedEnglishWordDao.getCount();
                    if(offset<count) {
                        List<StoredEnglishWord> englishWordList = storedEnglishWordDao.findAllWords();
                        for (StoredEnglishWord wordFound : englishWordList) {
                            wordList.add(new Word(wordFound.getWordId(), wordFound.getWord(), wordFound.getUserId(), wordFound.getAudioUrl(), wordFound.getReported(), wordFound.getTimestamp(), wordFound.getNoOfVotes(), wordFound.getUserName(), wordFound.getWordType(), wordFound.getEnglishMeaning(), wordFound.getKhasiMeaning(), wordFound.isTranslatedToKhasi(), wordFound.isTranslatedToEnglish(), wordFound.isTranslatedToHindi(), wordFound.isTranslatedToGaro()));
                        }
                        offset += 1000;
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //New Algorithm for words searching and viewing all
                if(word.equals(""))
                {
                    //View All Data from database
                    if(wordList.size()>0)
                    {
                        binding.noResult.setVisibility(View.GONE);
                        binding.noItems.setVisibility(View.GONE);
                        wordSearchResultAdapter = new WordSearchResultAdapter(getApplicationContext(), wordList,  fromLang);
                        binding.wordSearchResult.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        binding.wordSearchResult.setVisibility(View.VISIBLE);
                        binding.wordSearchResult.setAdapter(wordSearchResultAdapter);
                        //Log.e("DATA","LOAD OFFLINE "+wordList.size());
                        binding.layout.shimmer.stopShimmer();
                        binding.layout.shimmer.setVisibility(View.GONE);
                    }
//                    Log.e("DATA","LOAD LOCAL "+wordList.size()+" "+fromLang);
                }
                else {
                    //make word Title case: Dog, Cat,
                    word=word.substring(0,1).toUpperCase()+word.substring(1).toLowerCase();
                    //Search Offline first then search online
                    loadDataInRoom();
                }
            }
        }
        CountNumberOfWords countNumberOfSentences=new CountNumberOfWords();
        countNumberOfSentences.execute();

    }

    private void loadDataInRoom() {
        class SearchInRoom extends AsyncTask<Void, Boolean, Boolean> {

            List<StoredEnglishWord> storedEnglishWordList;
            List<StoredKhasiWord> storedKhasiWordList;
            String query;
            public SearchInRoom() {
                wordList.clear();
                storedKhasiWordList = new ArrayList<>();
                storedEnglishWordList = new ArrayList<>();
                query =word+"%";
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                //Log.e("DATA","LOADING");
                if(fromLang.equals("Khasi")){
                    StoredKhasiWordDao storedKhasiWordDao=db.storedKhasiWordDao();
                    storedKhasiWordList=storedKhasiWordDao.findWord(query);
                    if(storedKhasiWordList.size()>0)
                    {
                        for(StoredKhasiWord khasiWord:storedKhasiWordList) {
                            wordList.add(new Word(khasiWord.getWordId(),khasiWord.getWord(),khasiWord.getUserId(),khasiWord.getAudioUrl(),khasiWord.getReported(),khasiWord.getTimestamp(),khasiWord.getNoOfVotes(),khasiWord.getUserName(),khasiWord.getWordType(),khasiWord.getEnglishMeaning(),khasiWord.getKhasiMeaning(),khasiWord.isTranslatedToKhasi(),khasiWord.isTranslatedToEnglish(),khasiWord.isTranslatedToHindi(),khasiWord.isTranslatedToGaro()));
                        }
                    }
                }
                else if(fromLang.equals("English")) {
                    StoredEnglishWordDao storedEnglishWordDao=db.storedEnglishWordDao();
                    storedEnglishWordList=storedEnglishWordDao.findWord(query);
                    if(storedEnglishWordList.size()>0)
                    {
                        for(StoredEnglishWord wordFound: storedEnglishWordList) {
                            wordList.add(new Word(wordFound.getWordId(),wordFound.getWord(),wordFound.getUserId(),wordFound.getAudioUrl(),wordFound.getReported(),wordFound.getTimestamp(),wordFound.getNoOfVotes(),wordFound.getUserName(),wordFound.getWordType(),wordFound.getEnglishMeaning(),wordFound.getKhasiMeaning(),wordFound.isTranslatedToKhasi(),wordFound.isTranslatedToEnglish(),wordFound.isTranslatedToHindi(),wordFound.isTranslatedToGaro()));
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(wordList.size()>0)
                {
                    binding.noResult.setVisibility(View.GONE);
                    binding.noItems.setVisibility(View.GONE);
                    //loads data offline
                    wordSearchResultAdapter = new WordSearchResultAdapter(getApplicationContext(), wordList,  fromLang);
                    binding.wordSearchResult.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    binding.wordSearchResult.setVisibility(View.VISIBLE);
                    binding.wordSearchResult.setAdapter(wordSearchResultAdapter);
                    //Log.e("DATA","LOAD OFFLINE "+wordList.size());
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
                    //show advance online search - it has limits 50 per month for non-sponsors
                    binding.advSearch.setVisibility(View.VISIBLE);
                    binding.advanceSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //show alert
                            if(level<4)
                            {
                                //show alert
                                AlertDialog.Builder builder=new AlertDialog.Builder(WordSearchActivity.this);
                                builder.setTitle("Advance Search Online");
                                if(limitedAccess>0) {
                                    builder.setMessage("You have " + limitedAccess + " free advance search left.");
                                    builder.setCancelable(true);
                                    builder.setPositiveButton("Search Word", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //search
                                            search();
                                            limitedAccess--;
                                            binding.advSearch.setVisibility(View.GONE);
                                            editor.putInt("online", limitedAccess);
                                            editor.commit();
                                            editor.apply();
                                            Snackbar.make(binding.getRoot(), getResources().getString(R.string.search), BaseTransientBottomBar.LENGTH_LONG).show();
                                        }
                                    });
                                    builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Snackbar.make(binding.getRoot(), getResources().getString(R.string.search), BaseTransientBottomBar.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                else{
                                    builder.setMessage("You have used all your quota for advance search!");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Snackbar.make(binding.getRoot(), getResources().getString(R.string.search), BaseTransientBottomBar.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                AlertDialog alertDialog=builder.create();
                                alertDialog.show();
                            }
                            else{
                                search();
                                binding.advSearch.setVisibility(View.GONE);
                            }
                        }
                    });
                }else
                {
                    //search online
                    search();
                    //Log.e("DATA","LOAD ONLINE");
                }
            }
        }

        new SearchInRoom().execute();

    }

    private void search() {
//        Log.e("Search Word",word+ " ");
        //binding.noResult.setVisibility(View.VISIBLE);
        myRef.orderByChild("word").startAt(word).endAt(word+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noOfWords = (int) snapshot.getChildrenCount();
                //binding.noOfKhasiWords.setText(noOfKhasiWords+" Khasi words");
//                Log.e("DATA WORDS",noOfWords+"");
                if(noOfWords>0)
                {
                    if(level<3)
                        binding.noResult.setVisibility(View.GONE);
                    binding.wordSearchResult.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    binding.wordSearchResult.setVisibility(View.VISIBLE);
                    //display the list of words
                    if(wordList!=null)
                        wordList.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Word wordFound = postSnapshot.getValue(Word.class);
//                        Log.e("DATA", fromLang + " LANG KEY DATA: " + postSnapshot.getKey());
                        wordList.add(wordFound);
                        if(fromLang.equals("Khasi"))
                        {
                            //store khasi word
                            StoredKhasiWord khasiWordFound = postSnapshot.getValue(StoredKhasiWord.class);
//                            Log.e("DATA", fromLang + " LANG KEY DATA: " + postSnapshot.getKey());
                            storeWordToDB(null,khasiWordFound,"Khasi");
                        }
                        else
                        {
                            StoredEnglishWord englishWordFound = postSnapshot.getValue(StoredEnglishWord.class);
//                            Log.e("DATA", fromLang + " LANG KEY DATA: " + postSnapshot.getKey());
                            storeWordToDB(englishWordFound,null,"English");
                        }
                        //store word in room
                        //storeSentenceInRoom(postSnapshot.getKey() + "", newSentence);
//                        Log.e("Khasi", wordFound.getWord() +" : "+postSnapshot.getKey()  + " - " + word);
                    }
                    if (wordList.size() > 0) {
                        binding.noResult.setVisibility(View.GONE);
                        binding.noItems.setVisibility(View.GONE);
                        wordSearchResultAdapter = new WordSearchResultAdapter(getApplicationContext(), wordList,  fromLang);
                        binding.wordSearchResult.setAdapter(wordSearchResultAdapter);
                    } else {
                        binding.noResult.setVisibility(View.VISIBLE);
                        binding.wordSearchResult.setVisibility(View.GONE);
                    }
                    binding.noItems.setVisibility(View.GONE);
                    //Log.e("DATA", "Load data from REALTIME");
                    if(level>=5)
                        binding.noResult.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.noResult.setVisibility(View.VISIBLE);
                    binding.wordSearchResult.setVisibility(View.GONE);
                    //Log.e("DATA WORDS",noOfWords+"");
                    //if(binding.noResult.getVisibility()==View.VISIBLE)
                    //Toast.makeText(WordSearchActivity.this, "Visible", Toast.LENGTH_SHORT).show();
                }
                binding.layout.shimmer.stopShimmer();
                binding.layout.shimmer.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    public void openWordView(View view) {
//        startActivity(new Intent(this,WordViewActivity.class));
//    }
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // show search menu on top
    if(word.equals("")) {
        getMenuInflater().inflate(R.menu.filter_search_all, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                if(wordSearchResultAdapter!=null)
                    wordSearchResultAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if(wordSearchResultAdapter!=null)
                    wordSearchResultAdapter.getFilter().filter(query);
                return false;
            }
        });
    }
    return true;
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}