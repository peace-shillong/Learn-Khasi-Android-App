package com.sngur.learnkhasi.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.SentenceSearchResultAdapter;
import com.sngur.learnkhasi.adapters.WordSearchResultAdapter;
import com.sngur.learnkhasi.databinding.FragmentWordsBinding;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.ui.activity.SentenceSearchActivity;
import com.sngur.learnkhasi.ui.activity.WordSearchActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WordsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordsFragment extends Fragment {

    private FragmentWordsBinding binding;
    private DatabaseReference myRef;
    private List<Word> wordList,khasiWordList;
//    private List<String> wordId;
    private Animation slide_in_left, slide_out_right;
    private FirebaseDatabase database;
    private static String newFromLang;
    private String wordTip,safe;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private int displayLimit;
//    private WordSearchResultAdapter wordSearchResultAdapter;

    public WordsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordsFragment.
     */
    // Rename and change types and number of parameters
    public static WordsFragment newInstance(String param1, String param2) {
        WordsFragment fragment = new WordsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        newFromLang="English";//Default from ENG-KHA
        myRef = database.getReference("english_word");
        wordList=new ArrayList<>();
        khasiWordList=new ArrayList<>();
        //create Animation objects
        slide_in_left = AnimationUtils.loadAnimation(getContext(),
                android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(getContext(),
                android.R.anim.slide_out_right);
        preferences =getActivity().getSharedPreferences("LearnKhasi", 0);
        editor=preferences.edit();
        wordTip=preferences.getString("wordTip","none");
        safe=preferences.getString("safe","on");
        displayLimit=preferences.getInt("displayLimit",15);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_words, container, false);
        binding=FragmentWordsBinding.inflate(getLayoutInflater(),container,false);
        //getActivity().setTitle(R.string.title_dictionary);
        binding.wordSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    if(binding.wordSearch.getText().length()>=2)
                        performSearch();
                    else {
                        Toast.makeText(getContext(), "Enter more than two characters", Toast.LENGTH_SHORT).show();
                        binding.wordSearch.setError("Please enter more than two character");
                    }
                    return true;
                }
                return false;
            }
        });
        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.wordSearch.getText().length()>=2)
                    performSearch();
                else {
                    Toast.makeText(getContext(), "Enter more than two characters", Toast.LENGTH_SHORT).show();
                    binding.wordSearch.setError("Please enter more than two character");
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
        binding.wordSearch.setFilters(new InputFilter[] { filter });

        binding.viewAllWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), WordSearchActivity.class);
                intent.putExtra("word","");
                intent.putExtra("fromLang",newFromLang);//specify which language to load
                startActivity(intent);
            }
        });

        //load words when switched
        binding.switchLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newFromLang=binding.toLang.getText().toString();
                String newToLang=binding.fromLang.getText().toString();

                binding.fromLang.setText(newFromLang);
                binding.toLang.setText(newToLang);

                binding.fromLang.startAnimation(slide_out_right);
                binding.toLang.startAnimation(slide_in_left);
                //wordList.clear();
                binding.latestWords.setAdapter(null);
                binding.layout.shimmer.startShimmer();
                binding.layout.shimmer.setVisibility(View.VISIBLE);
                if(newFromLang.equals("Khasi"))
                {
                    myRef = database.getReference("khasi_word");
                    binding.wordInput.setHint("Enter Khasi Word");
                    binding.wordSearch.setText("");
                    binding.wordSearchTitle.setText("Thoh ka kyntien ha Khasi");
                    binding.searchBtn.setText("Wad");
                    binding.latestWords.setVisibility(View.GONE);
                }else
                {
                    myRef = database.getReference("english_word");
                    binding.wordInput.setHint("Enter English Word");
                    binding.wordSearch.setText("");
                    binding.wordSearchTitle.setText("Enter English Word");
                    binding.searchBtn.setText("Search Online");
                    binding.latestKhasiWords.setVisibility(View.GONE);
                }
                loadLatestWords();
            }
        });
        loadLatestWords();

        if(wordTip.equals("Ok"))
        {
            binding.quickWordTip.setVisibility(View.GONE);
        }
        binding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("wordTip","Ok");
                editor.commit();
                editor.apply();
                binding.okBtn.setVisibility(View.GONE);
            }
        });

        return binding.getRoot();
    }


    private void performSearch() {
        //Toast.makeText(getContext(), "Searching for "+binding.wordSearch.getText(), Toast.LENGTH_SHORT).show();
        //perform a search on khasi_word node
        Intent intent=new Intent(getActivity(), WordSearchActivity.class);
        intent.putExtra("word",binding.wordSearch.getText().toString());
        intent.putExtra("fromLang",newFromLang);
        startActivity(intent);
    }

    private void loadLatestWords() {
        binding.latestWordsText.setVisibility(View.GONE);
        //load latest words from DB online
        String child="translatedToEnglish";
        if(newFromLang.equalsIgnoreCase("ENGLISH"))
        {
            child="translatedToKhasi";
        }
        //load Words that have not been translated to KHA or ENG
        //load latest words from local memory first then load from online if not found
        myRef.orderByChild(child).equalTo(false).limitToLast(displayLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wordList.clear();
                khasiWordList.clear();
//                Log.e("DATA","CALLED ONLINE 1");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                    Log.e("DATA","GOT SOME DATA");
                    Word word=postSnapshot.getValue(Word.class);
                    if(newFromLang.equalsIgnoreCase("ENGLISH")) {
                        //found bug in the list
                        if (wordList.size() == 0) {
                            if(safe.equals("on"))
                            {
                                if(word.getReported()<=0)//to change this later
                                {
                                    wordList.add(word);
                                }
                            }
                            else {
                                wordList.add(word);
                            }
                        } else if (wordList.size() > 0) {
                            //search in list
                            List<Word> tempList =wordList;
                            boolean wordNotFound=true;
                            for (Word wordItem : tempList) {
                                if (wordItem.getWord().equals(word.getWord())) {
                                    wordNotFound=false;
                                    break;
                                }
                            }
                            if(wordNotFound)
                            {
                                if(safe.equals("on"))
                                {
                                    if(word.getReported()<=0)//to change this later
                                    {
                                        wordList.add(word);
                                    }
                                }
                                else {
                                    wordList.add(word);
                                }
                            }
                        }
                    }
                    else{
                        if(khasiWordList.size()==0) {
                            if(safe.equals("on"))
                            {
                                if(word.getReported()<=0)//to change this later
                                {
                                    khasiWordList.add(word);
                                }
                            }
                            else {
                                khasiWordList.add(word);
                            }
                        }
                        else if(khasiWordList.size()>0)
                        {
                            List<Word> tempList =khasiWordList;
                            boolean wordNotFound=true;
                            for (Word wordItem : tempList) {
                                if (wordItem.getWord().equals(word.getWord())) {
                                    wordNotFound=false;
                                    break;
                                }
                            }
                            if(wordNotFound) {
                                if(safe.equals("on"))
                                {
                                    if(word.getReported()<=0)//to change this later
                                    {
                                        khasiWordList.add(word);
                                    }
                                }
                                else {
                                    khasiWordList.add(word);
                                }
                            }
                        }
                        }
                }
//                Log.e("SIZE",wordList.size()+" - "+khasiWordList.size());
                if(khasiWordList.size()==0)
                    loadLatestTranslatedWords();
                else if(wordList.size()>0) {
                    binding.latestWordsText.setVisibility(View.VISIBLE);

                    binding.latestWords.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.latestWords.setHasFixedSize(true);

                    binding.latestKhasiWords.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.latestKhasiWords.setHasFixedSize(true);

                    Collections.reverse(wordList);
                    Collections.reverse(khasiWordList);

                    WordSearchResultAdapter wordSearchResultAdapter;
                    if(newFromLang.equalsIgnoreCase("ENGLISH")) {
                        wordSearchResultAdapter = new WordSearchResultAdapter(getContext(), wordList, newFromLang);
                        binding.latestWords.setAdapter(wordSearchResultAdapter);
                        binding.latestKhasiWords.setVisibility(View.GONE);
                        binding.latestWords.setVisibility(View.VISIBLE);
                    }
                    else {
                        wordSearchResultAdapter = new WordSearchResultAdapter(getContext(), khasiWordList, newFromLang);
                        binding.latestKhasiWords.setAdapter(wordSearchResultAdapter);
                        binding.latestWords.setVisibility(View.GONE);
                        binding.latestKhasiWords.setVisibility(View.VISIBLE);
                    }


                }
                else{
                    loadLatestTranslatedWords();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadLatestTranslatedWords() {
        //load words that have been translated
        myRef.orderByChild("timestamp").limitToLast(displayLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wordList.clear();
                khasiWordList.clear();
//                Log.e("DATA","CALLED ONLINE 2");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                    Log.d("DATA","GOT SOME DATA");
                    Word word=postSnapshot.getValue(Word.class);
                    if(newFromLang.equalsIgnoreCase("ENGLISH")) {
                        if (wordList.size() == 0) {
                            if(safe.equals("on"))
                            {
                                if(word.getReported()<=0)//to change this later
                                {
                                    wordList.add(word);
                                }
                            }
                            else {
                                wordList.add(word);
                            }

                        } else if (wordList.size() > 0) {
                            //search in list
                            List<Word> tempList = wordList;
                            boolean wordNotFound = true;
                            for (Word wordItem : tempList) {
                                if (wordItem.getWord().equals(word.getWord())) {
                                    wordNotFound = false;
                                    break;
                                }
                            }
                            if (wordNotFound) {
                                if(safe.equals("on"))
                                {
                                    if(word.getReported()<=0)//to change this later
                                    {
                                        wordList.add(word);
                                    }
                                }
                                else {
                                    wordList.add(word);
                                }
                            }
                        }
                    }
                    else {
                            if (khasiWordList.size() == 0) {
                                if(safe.equals("on"))
                                {
                                    if(word.getReported()<=0)//to change this later
                                    {
                                        khasiWordList.add(word);
                                    }
                                }
                                else {
                                    khasiWordList.add(word);
                                }
                            } else if (khasiWordList.size() > 0) {
                                List<Word> tempList = khasiWordList;
                                boolean wordNotFound = true;
                                for (Word wordItem : tempList) {
                                    if (wordItem.getWord().equals(word.getWord())) {
                                        wordNotFound = false;
                                        break;
                                    }
                                }
                                if (wordNotFound) {
                                    if(safe.equals("on"))
                                    {
                                        if(word.getReported()<=0)//to change this later
                                        {
                                            khasiWordList.add(word);
                                        }
                                    }
                                    else {
                                        khasiWordList.add(word);
                                    }
                                }
                            }
                        }
                }
                if(wordList.size()>0 || khasiWordList.size()>0) {
                    binding.latestWordsText.setVisibility(View.VISIBLE);

                    binding.latestWords.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.latestWords.setHasFixedSize(true);

                    binding.latestKhasiWords.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.latestKhasiWords.setHasFixedSize(true);

                    Collections.reverse(wordList);
                    Collections.reverse(khasiWordList);
                    WordSearchResultAdapter wordSearchResultAdapter;
                    if(newFromLang.equalsIgnoreCase("ENGLISH")) {
//                        Log.e("SIZE",wordList.size()+"");
                        wordSearchResultAdapter = new WordSearchResultAdapter(getContext(), wordList, newFromLang);
                        binding.latestWords.setAdapter(wordSearchResultAdapter);
                        binding.latestKhasiWords.setVisibility(View.GONE);
                        binding.latestWords.setVisibility(View.VISIBLE);
                    }
                    else {
                        wordSearchResultAdapter = new WordSearchResultAdapter(getContext(), khasiWordList, newFromLang);
                        binding.latestKhasiWords.setAdapter(wordSearchResultAdapter);
                        binding.latestWords.setVisibility(View.GONE);
                        binding.latestKhasiWords.setVisibility(View.VISIBLE);
                    }
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //wordList.clear(); fixed Fatal Error Occurred
        String toLang="Khasi";
//        if(newFromLang.equalsIgnoreCase("Khasi")) {
//            toLang = "English";
//            binding.latestWords.setVisibility(View.GONE);
//            binding.latestKhasiWords.setVisibility(View.VISIBLE);
//        }
//        else{
//            //if it's English
//            binding.latestKhasiWords.setVisibility(View.GONE);
//            binding.latestWords.setVisibility(View.VISIBLE);
//        }
        //loadLatestTranslatedWords();
        Toast.makeText(getActivity(), "From "+newFromLang+" to "+toLang+" Word translation selected", Toast.LENGTH_SHORT).show();
    }
}