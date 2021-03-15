package com.sngur.learnkhasi.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.SentenceSearchResultAdapter;
import com.sngur.learnkhasi.databinding.ActivitySentenceSearchBinding;
import com.sngur.learnkhasi.model.Category;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.room.StoredSentence;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.StoredSentenceDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SentenceSearchActivity extends AppCompatActivity {

    ActivitySentenceSearchBinding binding;
    private String sentence;
    private String name;
    private static String fromLang;
    private String category;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference myRef,categoryRef;
    private int noOfSentences;
    private List<Sentence> sentenceList;
    private List<String> sentenceId,categories;
    private SearchView searchView;// for searching in the view all list in offline mode
    private SentenceSearchResultAdapter sentenceSearchResultAdapter;
    private static LearnKhasiDatabase db;
    private Integer numberOfSentencesInRoom;
    private Boolean noSearch;
    private AlertDialog alertDialog;
    private int level,limitedAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_sentence_search);
        binding=ActivitySentenceSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Sentences");
        sentence=getIntent().getExtras().getString("sentence");
        fromLang=getIntent().getExtras().getString("fromLang");
        binding.sentenceSearch.setText("Searching for\n"+sentence);
        binding.askBtn.setEnabled(false);
        preferences =getSharedPreferences("LearnKhasi", 0);
        editor=preferences.edit();
        name=preferences.getString("name","N/A "); // see if user is logged in
        level=preferences.getInt("level",1);
        if(level==3)
            limitedAccess=preferences.getInt("online",551);
        else
            limitedAccess=preferences.getInt("online",151);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(fromLang.equals("Khasi"))
            myRef = database.getReference("khasi_sentence"); //path to english sentences
        else
            myRef = database.getReference("english_sentence"); //path to english sentences
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
        //intialist data
        noOfSentences=-1;
        sentenceList=new ArrayList<>();
        sentenceId=new ArrayList<>();
        categories=new ArrayList<>();
        categoryRef=database.getReference();
        //search(); // don't call search here
        category="Conversations";
        loadCategory();
        //load DB instance
        db=LearnKhasiDatabase.getDatabase(this);
        //load Count based on fromLang
        numberOfSentencesInRoom=0;
        noSearch=false;
        countNumberOfSentences();

        binding.askBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Confirm Ask : with Dialog
                android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(SentenceSearchActivity.this);
                builder.setTitle("Ask Sentence");
                builder.setMessage("Do you want to ask this sentence?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //push data to sentence node in FBDB
                        String newSentenceId=myRef.push().getKey();
                        //Object Map
                        if(name.contains(" "))
                            name=name.substring(0,name.indexOf(" "));
                        if(binding.category.getSelectedItem()!=null)
                            category=binding.category.getSelectedItem().toString();
                        Sentence newSentence=new Sentence(sentence,preferences.getString("gid","none"),"",0, System.currentTimeMillis(),false,0,name,category,false,false,false);
                        myRef.child(newSentenceId).setValue(newSentence);
                        Toast.makeText(SentenceSearchActivity.this, "Asked", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                android.app.AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }
        });

    }

    private void loadCategory() {

        //No need to load Categories online
//        categoryRef.child("category").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                categories.clear();
//                //got error here
//                for(DataSnapshot postSnapshot : snapshot.getChildren()){
//                    Category category= postSnapshot.getValue(Category.class);
//                    categories.add(category.getName());
//                }
//                if(categories.size()>0)
//                {
//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SentenceSearchActivity.this, android.R.layout.simple_list_item_1, categories);
//                    binding.category.setAdapter(arrayAdapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        categories.add("Conversations");
        categories.add("Greetings");
        categories.add("Time and Date");
        categories.add("Directions and Places");
        categories.add("Shopping");
        categories.add("Eating and Table Manners");
        categories.add("Formal Request");
        categories.add("Question");
        categories.add("Others");
        if(categories.size()>0)
        {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SentenceSearchActivity.this, android.R.layout.simple_list_item_1, categories);
            binding.category.setAdapter(arrayAdapter);
        }
    }

    private void search() {
        sentence=sentence.trim();
        if(!sentence.equals(""))
            Toast.makeText(this, "Searching for "+sentence, Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this, "All Sentences", Toast.LENGTH_SHORT).show();
        }
        //ROOM August 2020 Completed on 30th Aug 2020
        // 1. Search in Room First : DONE
        // 2. If Found then List the data and Goto SearchViewActivity
        // 3. If Not in Room Then load all data from Server if count is not the same and Store in Sentence Table : DONE
        // Testing DONE - Similarly for words also
        // A. Create Room Model, Dao: Search, Count and Insert DONE
        // B. get Count based on fromLang then Search returns List of Stored Sentences : DONE
        // C. Add StoredSentence to the Lists SentenceList and SentenceId :DONE
        //Test it
        //sentenceList.clear();
        //sentenceId.clear();
        binding.sentenceSearchResult.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        loadDataInRoom();
            //then don't load from server
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // show search menu on top
        if(sentence.equals("")) {
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
                    if(sentenceSearchResultAdapter.getFilter()!=null)
                        sentenceSearchResultAdapter.getFilter().filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    // filter recycler view when text is changed
                    if(sentenceSearchResultAdapter.getFilter()!=null)
                        sentenceSearchResultAdapter.getFilter().filter(query);
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

    private  void countNumberOfSentences(){
        @SuppressLint("StaticFieldLeak")
        class CountNumberOfSentences extends AsyncTask<Void,Void,Void> {

             @Override
             protected Void doInBackground(Void... objects) {
                 StoredSentenceDao storedSentenceDao=db.storedSentenceDao();
                 numberOfSentencesInRoom=storedSentenceDao.getNumberOfSentences(fromLang);
                 return null;
             }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                search();
            }
        }
        CountNumberOfSentences countNumberOfSentences=new CountNumberOfSentences();
        countNumberOfSentences.execute();

    }

    private void loadDataInRoom() {
        class SearchInRoom extends AsyncTask<Void,Boolean,Boolean>{
            Boolean viewAll;
            public SearchInRoom(Boolean noSearch) {
                viewAll=noSearch;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);

                if(sentenceList.size()>0)
                {
                    sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getApplicationContext(), sentenceList, sentenceId, fromLang);
                    binding.sentenceSearchResult.setAdapter(sentenceSearchResultAdapter);
                    binding.noItems.setVisibility(View.GONE);
                    binding.sentenceSearchResult.setVisibility(View.VISIBLE);
                    //Log.d("DATA","Load data from Room");
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
                    //search
                    if(!sentence.equals(""))
                    binding.advSearch.setVisibility(View.VISIBLE);
                    binding.advanceSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(level<4)
                            {
                                //show alert
                                android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(SentenceSearchActivity.this);
                                builder.setTitle("Advance Search Online");
                                builder.setCancelable(true);

                                if(limitedAccess>0) {
                                    builder.setMessage("You have " + limitedAccess + " free advance search left.");
                                    builder.setPositiveButton("Search Sentence", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //search
                                            sentenceList.clear();
                                            sentenceId.clear();
                                            loadOnline();
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
                                android.app.AlertDialog alertDialog=builder.create();
                                alertDialog.show();
                            }
                            else{
                                loadOnline();
                                binding.advSearch.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else
                {
                    loadOnline();
                }
            }


            @Override
            protected Boolean doInBackground(Void... voids) {
                StoredSentenceDao storedSentenceDao=db.storedSentenceDao();
                List<StoredSentence> storedSentences=new ArrayList<>();
                //if(!sentence.equals("")) {
                if(!noSearch) {
                    String query ="%"+sentence+"%";
                    query = query.replaceAll("[^A-Za-z0-9]", "%");
                    //Log.d("QUERY",query);
                    storedSentences = storedSentenceDao.findSentence(query, fromLang);
                }
                else{ //load All Sentences from Room
                    storedSentences = storedSentenceDao.findSentence(fromLang);
                }
                if(storedSentences.size()>0)
                {
                    for(StoredSentence storedSentence : storedSentences){
                        //add the stored sentences to sentenceList and sentenceId
                        Sentence newSentence=new Sentence(storedSentence.getSentence(),storedSentence.getUserId(),storedSentence.getAudioUrl(),storedSentence.getReported(),storedSentence.getTimestamp(),storedSentence.getTranslatedToKhasi(),storedSentence.getNoOfVotes(),storedSentence.getUserName(),storedSentence.getCategory(),storedSentence.isTranslatedToEnglish(),storedSentence.isTranslatedToHindi(),storedSentence.isTranslatedToGaro());
                        sentenceList.add(newSentence);
                        sentenceId.add(storedSentence.getSentenceId() + "");
                    }
                }
                return storedSentences.size() > 0;
            }

        }

        SearchInRoom searchInRoom=new SearchInRoom(noSearch);
        searchInRoom.execute();
    }

    private void loadOnline() {
        //search online and store sentences if required
        //startAt(sentence).endAt(sentence+"\uf8ff")
        if(sentence.equals("")) // load All Sentences from Room and save bandwidth
        {
            noSearch=true;
            loadDataInRoom();
        }
        else {
            //loading all data from realtime database : Modified to search for starting word : on Dec 2020
            //startAt(firstWord).endAt(firstWord+"\uf8ff")
//            Log.e("SENTENCE",sentence);
            //if(sentence.indexOf(" ")!=null)
            String firstWord="What ";
            try {
             firstWord = sentence.substring(0, sentence.indexOf(" ") - 1);
            }catch (Exception e)
            {
                firstWord=sentence;
                Toast.makeText(this, "Please do not enter blank spaces", Toast.LENGTH_SHORT).show();
            }
            myRef.orderByChild("sentence").startAt(firstWord).endAt(firstWord+"\uf8ff").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    sentenceList.clear();
                    sentenceId.clear();
                    noOfSentences = (int) snapshot.getChildrenCount();
                    //binding.noOfKhasiWords.setText(noOfKhasiWords+" Khasi words");
                    //Log.d("DATA WORDS",noOfSentences+"");
                    //if (noOfSentences != numberOfSentencesInRoom)
                    //Christians daily need to be in repentance
                    //To stay away from sin
                    //And
                    //Log.e("DATA", numberOfSentencesInRoom + " No of Sentences " + noOfSentences);
                    //if(numberOfSentencesInRoom!=noOfSentences)
                    if (noOfSentences > 0) {
                        binding.noResult.setVisibility(View.GONE);
                        binding.sentenceSearchResult.setVisibility(View.VISIBLE);
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Sentence newSentence = postSnapshot.getValue(Sentence.class);
                            //Log.d("DATA", fromLang + " LANG KEY DATA: " + postSnapshot.getKey());
                            //noSearch - view all
                            //assert newSentence != null;//got error here with Khasi sentence
                            String tempSentence = "";
                            String temp = "";
                            if (newSentence != null && sentence != null) {
                                tempSentence = sentence.toLowerCase();
                                if (newSentence.getSentence() != null)
                                    temp = newSentence.getSentence().toLowerCase();
                                else
                                    temp = "";
                            }
                            if (temp.contains(tempSentence) || sentence.equals("")) {
                                //Log.d("Khasi", newSentence.getSentence() + " - " + sentence);
                                sentenceList.add(newSentence);
                                sentenceId.add(postSnapshot.getKey() + "");
                            }
                            //store all sentences in room
                            storeSentenceInRoom(postSnapshot.getKey() + "", newSentence);
//                            Log.e("Khasi", newSentence.getSentence() +" : "+postSnapshot.getKey()  + " - " + sentence);
                        }
                        if (sentenceList.size() > 0) {
                            sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getApplicationContext(), sentenceList, sentenceId, fromLang);
                            binding.sentenceSearchResult.setAdapter(sentenceSearchResultAdapter);
                        } else {
                            binding.noResult.setVisibility(View.VISIBLE);
                            binding.sentenceSearchResult.setVisibility(View.GONE);
                            //Log.e("DATA","NO RESULT");
                        }
                        binding.noItems.setVisibility(View.GONE);
                        //binding.layout.shimmer.stopShimmer();
                        //binding.layout.shimmer.setVisibility(View.GONE);
                        //Log.d("DATA", "Load data from REALTIME");
                    }
                    else{
                        binding.noResult.setVisibility(View.VISIBLE);
                    }
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
//                else
//                {
//                    binding.noResult.setVisibility(View.VISIBLE);
//                    binding.sentenceSearchResult.setVisibility(View.GONE);
//                    Log.d("DATA WORDS",noOfSentences+"");
//                    //if(binding.noResult.getVisibility()==View.VISIBLE)
//                    //Toast.makeText(WordSearchActivity.this, "Visible", Toast.LENGTH_SHORT).show();
//                }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        //end of else
    }

    private void storeSentenceInRoom(final String sentenceId, final Sentence newSentence) {
        class StoreInRoom extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                StoredSentenceDao storedSentenceDao=db.storedSentenceDao();
                StoredSentence storedSentence = new StoredSentence(sentenceId,fromLang,newSentence.getSentence(),newSentence.getUserId(),newSentence.getAudioUrl(),newSentence.getReported(),newSentence.getTimestamp(),newSentence.getTranslatedToKhasi(),newSentence.getNoOfVotes(),newSentence.getUserName(),newSentence.getCategory(),newSentence.isTranslatedToEnglish(),newSentence.isTranslatedToHindi(),newSentence.isTranslatedToGaro());
                storedSentenceDao.insert(storedSentence);
                return null;
            }
        }

        StoreInRoom storeInRoom=new StoreInRoom();
        storeInRoom.execute();
    }
}