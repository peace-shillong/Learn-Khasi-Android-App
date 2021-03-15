package com.sngur.learnkhasi.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.SentenceSearchResultAdapter;
import com.sngur.learnkhasi.adapters.WordSearchResultAdapter;
import com.sngur.learnkhasi.databinding.ActivityUserWordsListBinding;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserWordsListActivity extends AppCompatActivity {

    ActivityUserWordsListBinding binding;
    //Similar to UserSentenceListActivity
    private ViewPagerWordsAdapter viewPagerWordsAdapter;
    private static String userId,type;
    private static DatabaseReference myRef;
    private static FirebaseDatabase database;
    private static int noOfWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_words_list);
        binding=ActivityUserWordsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewPagerWordsAdapter=new ViewPagerWordsAdapter(getSupportFragmentManager());
        binding.tabLayoutWords.setupWithViewPager(binding.viewPagerWords);
        binding.viewPagerWords.setAdapter(viewPagerWordsAdapter);
        userId=getIntent().getExtras().getString("userId");
        type=getIntent().getExtras().getString("type");
        database = FirebaseDatabase.getInstance();
        noOfWords=-1;
    }

    private class ViewPagerWordsAdapter extends FragmentStatePagerAdapter {

        public ViewPagerWordsAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager,FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new WordsListFragment();
            String fromLang="English";
            switch (position){
                case 0:
                    fromLang="English";
                    break;
                case 1:
                    fromLang="Khasi";
                    break;
                case 2:
                    fromLang="Garo";
                    break;
//                case 3:
//                    fromLang="Hindi";
//                    break;
            }
            Bundle args = new Bundle();
            args.putString("fromLang",fromLang);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            String title="";
            switch (position){
                case 0:
                    title="English";
                    break;
                case 1:
                    title="Khasi";
                    break;
                case 2:
                    title="Garo";
                    break;
//                case 3:
//                    title="Hindi";
//                    break;
            }
            return title;
        }
    }

    public static class WordsListFragment extends Fragment {
        public static final String ARG_OBJECT = "object";
        private RecyclerView recyclerView,khasiWordsRecyclerView;
        private WordSearchResultAdapter wordSearchResultAdapter;
        private List<Word> wordsList,khasiWordsList;//Garo List also
        //private List<String> wordId.khasiWordId;
        private TextView noItems;
        private LinearLayout shimmer;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View view= inflater.inflate(R.layout.activity_word_search, container, false);
            recyclerView=view.findViewById(R.id.wordSearchResult);
            khasiWordsRecyclerView=view.findViewById(R.id.wordSearchResult);
            noItems=view.findViewById(R.id.noItems);
            shimmer=view.findViewById(R.id.layout);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            String path;
            Bundle args = getArguments();
            String fromLang=args.getString("fromLang");
            switch (fromLang){
                case "English":
                    path="english_word";
                    break;
                case "Khasi":
                    path="khasi_word";
                    break;
//                case "Hindi":
//                    path="hindi_word";
//                    break;
                default:
                    path="garo_word";
                    break;
            }
            wordsList=new ArrayList<>();
            //wordId=new ArrayList<>();
            khasiWordsList=new ArrayList<>();
            //khasiWordId=new ArrayList<>();
            loadUserWords(path,fromLang);
        }

        //inner class
        private void loadUserWords(String path,final String fromLang) {
            myRef=database.getReference(path);
            myRef.orderByChild("userId").startAt(userId).endAt(userId+"\uf8ff").limitToLast(10).addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //add data to list
                    wordsList.clear();
                    //wordId.clear();
                    khasiWordsList.clear();
                    //khasiWordId.clear();
                    if(wordSearchResultAdapter!=null)
                        wordSearchResultAdapter.notifyDataSetChanged();
                    noOfWords = (int) snapshot.getChildrenCount();
                    //if(fromLang.equals("Khasi") || fromLang.equals("English"))//Load only English and Khasi Languages for now
                    if (noOfWords > 0) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setVisibility(View.VISIBLE);
                        khasiWordsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        khasiWordsRecyclerView.setHasFixedSize(true);

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Word newWord = postSnapshot.getValue(Word.class);
                            //Log.d("DATA", " LANG KEY DATA: " + postSnapshot.getKey());
                            //noSearch - view all
                            //assert newSentence != null;//got error here with Khasi sentence
                            //if(temp.contains(tempSentence) ) {
                            //Log.d("Khasi", newSentence.getSentence() + " - ");
                            if(fromLang.equals("Khasi"))
                            {

                                if(type.equals("reported")) {
                                    //list of reported sentences
                                    if (newWord.getReported() > 0){
                                        khasiWordsList.add(newWord);
                                        //khasiSentenceId.add(postSnapshot.getKey() + "");
                                    }
                                }
                                else {
                                    khasiWordsList.add(newWord);
                                    //khasiSentenceId.add(postSnapshot.getKey() + "");
                                }

                            }
                            else {
                                if(type.equals("reported")) {
                                    //list of reported sentences
                                    if (newWord.getReported() > 0){
                                        wordsList.add(newWord);
                                        //sentenceId.add(postSnapshot.getKey() + "");
                                    }
                                }
                                else {
                                    wordsList.add(newWord);
                                    //sentenceId.add(postSnapshot.getKey() + "");
                                }
                            }
                            // }
                        }
                    }
                    //load data
                    //Log.d(":DATA",sentenceList.size()+" - "+khasiSentenceList.size());
                    if(wordsList.size()>0){
                        // else { //This can be used for ENG, GARO and HINDI
                        Collections.reverse(wordsList);
                        wordSearchResultAdapter = new WordSearchResultAdapter(getContext(), wordsList, fromLang);
                        recyclerView.setAdapter(wordSearchResultAdapter);
                        recyclerView.getRecycledViewPool().clear();
                        wordSearchResultAdapter.notifyDataSetChanged();
                        noItems.setVisibility(View.GONE);
                        // }
                    }
                    if(khasiWordsList.size()>0){
                        Collections.reverse(khasiWordsList);
                        //if(fromLang.equals("Khasi")) {
                        wordSearchResultAdapter = new WordSearchResultAdapter(getContext(), khasiWordsList, fromLang);
                        khasiWordsRecyclerView.setAdapter(wordSearchResultAdapter);
                        khasiWordsRecyclerView.getRecycledViewPool().clear();
                        wordSearchResultAdapter.notifyDataSetChanged();
                        noItems.setVisibility(View.GONE);
                        //}
                    }
                    else {
                        //khasiSentenceList.size();
                        //noItems.setVisibility(View.VISIBLE);
                    }
                    shimmer.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}