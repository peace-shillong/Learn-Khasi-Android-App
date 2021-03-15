package com.sngur.learnkhasi.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.sngur.learnkhasi.databinding.FragmentSentencesBinding;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.ui.activity.SentenceSearchActivity;
import com.sngur.learnkhasi.ui.activity.WordSearchActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SentencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SentencesFragment extends Fragment {

    private String safe;
    private FragmentSentencesBinding binding;
    private DatabaseReference myRef;
    private List<Sentence> sentenceList;
    private List<String> sentenceId;
    private Animation slide_in_left, slide_out_right;
    private FirebaseDatabase database;
    private String newFromLang,sentenceTip;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int displayLimit;

    public SentencesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SentencesFragment.
     */
    // Rename and change types and number of parameters
    public static SentencesFragment newInstance(String param1, String param2) {
        SentencesFragment fragment = new SentencesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        newFromLang="English";
        myRef = database.getReference("english_sentence");
        sentenceList=new ArrayList<>();
        sentenceId=new ArrayList<>();
        //create Animation objects
        slide_in_left = AnimationUtils.loadAnimation(getContext(),
                android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(getContext(),
                android.R.anim.slide_out_right);
        preferences =getActivity().getSharedPreferences("LearnKhasi", 0);
        editor=preferences.edit();
        sentenceTip=preferences.getString("sentenceTip","none");
        safe=preferences.getString("safe","on");
        displayLimit=preferences.getInt("displayLimit",15);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_sentences, container, false);
        //getActivity().setTitle(R.string.title_sentences);
        binding=FragmentSentencesBinding.inflate(getLayoutInflater(),container,false);
        binding.sentenceSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    if(binding.sentenceSearch.getText()!=null && binding.sentenceSearch.getText().toString().contains(" "))
                        performSearch();
                    else {
                        Toast.makeText(getContext(), "Please enter a sentence", Toast.LENGTH_SHORT).show();
                        binding.sentenceSearch.setError("Please enter a sentence");
                    }
                    return true;
                }
                return false;
            }
        });
        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.sentenceSearch.getText()!=null  && binding.sentenceSearch.getText().toString().contains(" "))
                    performSearch();
                else {
                    Toast.makeText(getContext(), "Please enter a sentence", Toast.LENGTH_SHORT).show();
                    binding.sentenceSearch.setError("Please enter a sentence");
                }
            }
        });
        loadLatestSentences();
        binding.viewAllSentences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),SentenceSearchActivity.class);
                intent.putExtra("sentence","");
                intent.putExtra("fromLang",newFromLang);//specify which language to load
                startActivity(intent);
            }
        });
        //switch Language and reload all data
        binding.switchLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newFromLang=binding.toLang.getText().toString();
                String newToLang=binding.fromLang.getText().toString();

                binding.fromLang.setText(newFromLang);
                binding.toLang.setText(newToLang);

                binding.fromLang.startAnimation(slide_out_right);
                binding.toLang.startAnimation(slide_in_left);
                sentenceId.clear();
                sentenceList.clear();
                binding.latestSentences.setAdapter(null);
                binding.layout.shimmer.startShimmer();
                binding.layout.shimmer.setVisibility(View.VISIBLE);
                if(newFromLang.equals("Khasi"))
                {
                    myRef = database.getReference("khasi_sentence");
                    binding.sentenceHint.setHint("Enter Khasi Sentence");
                    binding.sentenceSearch.setText("");
                    binding.sentenceSearchTitle.setText("Kumno ban ong ha ka Ktien English?");
                    binding.searchBtn.setText("Wad");
                }else
                {
                    myRef = database.getReference("english_sentence");
                    binding.sentenceHint.setHint("Enter English Sentence");
                    binding.sentenceSearch.setText("");
                    binding.sentenceSearchTitle.setText("How do you say it in Khasi?");
                    binding.searchBtn.setText("Search Online");
                }
                loadLatestSentences();
            }
        });

        if(sentenceTip.equals("Ok"))
        {
            binding.quickWordTip.setVisibility(View.GONE);
        }
        binding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("sentenceTip","Ok");
                editor.commit();
                editor.apply();
                binding.okBtn.setVisibility(View.GONE);
            }
        });

        return binding.getRoot();
    }

    private void loadLatestSentences() {
        //hide the latest Sentences Untranslated Text by default
        binding.latestSentencesText.setVisibility(View.GONE);
        String child="translatedToEnglish";
        if(newFromLang.equalsIgnoreCase("ENGLISH"))
        {
            child="translatedToKhasi"; //has to be change later if we want ENG-GARO,ENG-HINDI
        }
        //Log.e("ID",child+" child");
        myRef.orderByChild(child).equalTo(false).limitToLast(displayLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sentenceList.clear();//onReload
                sentenceId.clear();//
//                Log.e("DATA","LOAD ONLINE 1");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                    Log.d("DATA","GOT SOME DATA");
                    Sentence sentence=postSnapshot.getValue(Sentence.class);
                    if(safe.equals("on"))
                    {
                        if(sentence.getReported()<=0)//to change this later
                        {
                            sentenceList.add(sentence);
                            sentenceId.add(postSnapshot.getKey() + "");
                        }
                    }
                    else {
                        sentenceList.add(sentence);
                        sentenceId.add(postSnapshot.getKey() + "");
                    }
                }
                if(sentenceList.size()>0) {
                    binding.latestSentencesText.setVisibility(View.VISIBLE);
                    Collections.reverse(sentenceList);
                    Collections.reverse(sentenceId);
                    SentenceSearchResultAdapter sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getContext(), sentenceList,sentenceId,newFromLang);
                    binding.latestSentences.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.latestSentences.setHasFixedSize(true);
                    binding.latestSentences.setAdapter(sentenceSearchResultAdapter);
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
                }
                else{
                    loadLatestTranslatedSentences();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadLatestTranslatedSentences() {
        myRef.orderByChild("timestamp").limitToLast(displayLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sentenceList.clear();//onReload
                sentenceId.clear();//
                //Log.e("DATA","LOAD ONLINE 2");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                    Log.d("DATA","GOT SOME DATA");
                    Sentence sentence=postSnapshot.getValue(Sentence.class);
                    if(safe.equals("on"))
                    {
                        Log.e("WHAT",sentence.getReported()+"");
                        if(sentence.getReported()<=0)//to change this later
                        {
                            sentenceList.add(sentence);
                            sentenceId.add(postSnapshot.getKey() + "");
                        }
                    }
                    else {
                        sentenceList.add(sentence);
                        sentenceId.add(postSnapshot.getKey() + "");
                    }
                }
                if(sentenceList.size()>0) {
                    binding.latestSentencesText.setVisibility(View.VISIBLE);
                    Collections.reverse(sentenceList);
                    Collections.reverse(sentenceId);
                    SentenceSearchResultAdapter sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getContext(), sentenceList,sentenceId,newFromLang);
                    binding.latestSentences.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.latestSentences.setHasFixedSize(true);
                    binding.latestSentences.setAdapter(sentenceSearchResultAdapter);
                    binding.layout.shimmer.stopShimmer();
                    binding.layout.shimmer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void performSearch() {
        //Toast.makeText(getContext(), "Searching for "+binding.wordSearch.getText(), Toast.LENGTH_SHORT).show();
        //perform a search on khasi_word node
        Intent intent=new Intent(getActivity(), SentenceSearchActivity.class);
        //improvement Capitalize the First Letter in search and translated text - 28th July
        String temp=binding.sentenceSearch.getText().toString().toLowerCase()+"";
        String search=temp.substring(0,1).toUpperCase()+temp.substring(1);
        intent.putExtra("sentence",search);
        intent.putExtra("fromLang",newFromLang);//specify which language to load
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadLatestSentences();
        String toLang="Khasi";
        if(newFromLang.equalsIgnoreCase("Khasi")) {
            toLang = "English";
        }
        else{
            //if it's English
        }
        Toast.makeText(getActivity(), "From "+newFromLang+" to "+toLang+" Sentence translation selected", Toast.LENGTH_SHORT).show();
    }
}