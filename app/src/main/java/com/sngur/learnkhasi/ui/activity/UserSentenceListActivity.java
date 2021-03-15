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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.adapters.SentenceSearchResultAdapter;
import com.sngur.learnkhasi.databinding.ActivityUserSentenceListBinding;
import com.sngur.learnkhasi.model.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserSentenceListActivity extends AppCompatActivity {

    private ActivityUserSentenceListBinding binding;
    private ViewPagerAdapter viewPagerAdapter;
    private static String userId,type;
    private static DatabaseReference myRef;
    private static FirebaseDatabase database;
    private static int noOfSentences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_sentence_list);
        binding=ActivityUserSentenceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.viewPager.setAdapter(viewPagerAdapter);
        userId=getIntent().getExtras().getString("userId");
        type=getIntent().getExtras().getString("type");
        database = FirebaseDatabase.getInstance();
        noOfSentences=-1;

    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    public ViewPagerAdapter(FragmentManager fm) {
            super(fm,FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new SentenceListFragment();
            String fromLang="English";
            switch (i){
                case 0:
                    fromLang="English";
                    break;
                case 1:
                    fromLang="Khasi";
                    break;
                case 2:
                    fromLang="Garo";
                    break;
                case 3:
                    fromLang="Hindi";
                    break;
            }
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            //args.putInt(SentenceListFragment.ARG_OBJECT, i + 1);
            args.putString("fromLang",fromLang);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
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
            case 3:
                title="Hindi";
                break;
        }
        return title;
        }
    }

    public static class SentenceListFragment extends Fragment {
        public static final String ARG_OBJECT = "object";
        private RecyclerView recyclerView,khasiSentenceRecyclerView;
        private SentenceSearchResultAdapter sentenceSearchResultAdapter;
        private List<Sentence> sentenceList,khasiSentenceList;
        private List<String> sentenceId,khasiSentenceId;
        private TextView noItems;
        private LinearLayout shimmer;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View view= inflater.inflate(R.layout.activity_sentence_search, container, false);
            recyclerView=view.findViewById(R.id.sentenceSearchResult);
            khasiSentenceRecyclerView=view.findViewById(R.id.sentenceSearchResult);
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
                    path="english_sentence";
                    break;
                case "Khasi":
                    path="khasi_sentence";
                    break;
                case "Hindi":
                    path="hindi_sentence";
                    break;
                default:
                    path="garo_sentence";
                    break;
            }
            sentenceList=new ArrayList<>();
            sentenceId=new ArrayList<>();
            khasiSentenceList=new ArrayList<>();
            khasiSentenceId=new ArrayList<>();
            loadUserSentences(path,fromLang);
//            ((TextView) view.findViewById(R.id.text_notifications))
//                    .setText(Integer.toString();
        }

        private void loadUserSentences(String path,final String fromLang) {
            myRef=database.getReference(path);
            myRef.orderByChild("userId").startAt(userId).endAt(userId+"\uf8ff").limitToLast(10).addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //add data to list
                    sentenceList.clear();
                    sentenceId.clear();
                    khasiSentenceList.clear();
                    khasiSentenceId.clear();
                    if(sentenceSearchResultAdapter!=null)
                        sentenceSearchResultAdapter.notifyDataSetChanged();
                    noOfSentences = (int) snapshot.getChildrenCount();
                    //if(fromLang.equals("Khasi") || fromLang.equals("English"))//Load only English and Khasi Languages for now
                    if (noOfSentences > 0) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setVisibility(View.VISIBLE);
                        khasiSentenceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        khasiSentenceRecyclerView.setHasFixedSize(true);

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Sentence newSentence = postSnapshot.getValue(Sentence.class);
                            //Log.d("DATA", " LANG KEY DATA: " + postSnapshot.getKey());
                            //noSearch - view all
                            //assert newSentence != null;//got error here with Khasi sentence
                            //if(temp.contains(tempSentence) ) {
                            //Log.d("Khasi", newSentence.getSentence() + " - ");
                            if(fromLang.equals("Khasi"))
                            {

                                    if(type.equals("reported")) {
                                        //list of reported sentences
                                        if (newSentence.getReported() > 0){
                                            khasiSentenceList.add(newSentence);
                                            khasiSentenceId.add(postSnapshot.getKey() + "");
                                        }
                                    }
                                    else {
                                        khasiSentenceList.add(newSentence);
                                        khasiSentenceId.add(postSnapshot.getKey() + "");
                                    }

                            }
                            else {
                                if(type.equals("reported")) {
                                    //list of reported sentences
                                    if (newSentence.getReported() > 0){
                                        sentenceList.add(newSentence);
                                        sentenceId.add(postSnapshot.getKey() + "");
                                    }
                                }
                                else {
                                    sentenceList.add(newSentence);
                                    sentenceId.add(postSnapshot.getKey() + "");
                                }
                            }
                            // }
                        }
                    }
                    //load data
                    //Log.d(":DATA",sentenceList.size()+" - "+khasiSentenceList.size());
                    if(sentenceList.size()>0){
                       // else { //This can be used for ENG, GARO and HINDI
                            Collections.reverse(sentenceList);
                            Collections.reverse(sentenceId);
                            sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getContext(), sentenceList, sentenceId,fromLang);
                            recyclerView.setAdapter(sentenceSearchResultAdapter);
                            recyclerView.getRecycledViewPool().clear();
                            sentenceSearchResultAdapter.notifyDataSetChanged();
                            noItems.setVisibility(View.GONE);
                       // }
                    }
                    if(khasiSentenceList.size()>0){
                        Collections.reverse(khasiSentenceList);
                        Collections.reverse(khasiSentenceId);
                        //if(fromLang.equals("Khasi")) {
                            sentenceSearchResultAdapter = new SentenceSearchResultAdapter(getContext(), khasiSentenceList, khasiSentenceId,fromLang);
                            khasiSentenceRecyclerView.setAdapter(sentenceSearchResultAdapter);
                            khasiSentenceRecyclerView.getRecycledViewPool().clear();
                            sentenceSearchResultAdapter.notifyDataSetChanged();
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