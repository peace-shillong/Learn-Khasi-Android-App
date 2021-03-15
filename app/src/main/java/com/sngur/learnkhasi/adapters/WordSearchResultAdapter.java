package com.sngur.learnkhasi.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.ui.activity.WordViewActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class WordSearchResultAdapter extends RecyclerView.Adapter<WordSearchResultAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private static List<Word> wordList,wordListFiltered;
    private static String fromLang,activity;
    private static boolean toasted;

    public WordSearchResultAdapter() {

    }

    public WordSearchResultAdapter(Context context, List<Word> wordList, String fromLang) {
        this.context = context;
        this.wordList = wordList;
        this.fromLang = fromLang;
        this.wordListFiltered=wordList;
        activity="none";
        toasted=false;
    }

    public WordSearchResultAdapter(Context context, List<Word> wordList, String fromLang, String activity) {
        this.context = context;
        this.wordList = wordList;
        this.fromLang = fromLang;
        this.wordListFiltered=wordList;
        this.activity=activity;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    wordListFiltered = wordList;
                    //sentenceIdFiltered=sentenceId;
                } else {
                    List<Word> filteredList = new ArrayList<>();
                    //List<String> filteredIdList=new ArrayList<>();
                    for (int i=0;i<wordList.size();i++) {
                        Word row = wordList.get(i);
                        String sentenceIdRow=wordList.get(i).getWordId();
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for sentence that matches
                        if (row.getWord().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                            //filteredIdList.add(sentenceIdRow);
                            //Log.d("DATA","Result");
                        }
                    }

                    wordListFiltered = filteredList;
                    //sentenceIdFiltered=filteredIdList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = wordListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                wordListFiltered = (ArrayList<Word>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Reuse the same UI for sentence and words
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_sentence_list, parent, false);
        WordSearchResultAdapter.MyViewHolder vh= new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.englishWord.setText(wordListFiltered.get(position).getWord());

        if(wordListFiltered.get(position).isTranslatedToKhasi() || wordListFiltered.get(position).isTranslatedToEnglish()) {
            //Log.d("DATA",wordListFiltered.get(position).isTranslatedToEnglish()+" - "+wordListFiltered.get(position).getWord());
            holder.isTranslated.setText("Translated");
            holder.isTranslated.setTextColor(context.getResources().getColor(R.color.colorWhite));
            holder.isTranslated.setBackground(context.getResources().getDrawable(R.drawable.greenbg));
        }
        holder.category.setText(""+wordListFiltered.get(position).getWordType());
        holder.category.setTypeface(holder.category.getTypeface(), Typeface.ITALIC);
        holder.userName.setText("Added by: "+wordListFiltered.get(position).getUserName());
        holder.wordItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(context, WordViewActivity.class);
//                    Log.d("DATA",wordListFiltered.size()+" IS ");
                    i.putExtra("wordId", wordListFiltered.get(position).getWordId());
                    i.putExtra("userId", wordListFiltered.get(position).getUserId());//userId of the one who asked the word
                    i.putExtra("word", wordListFiltered.get(position).getWord());
                    i.putExtra("timestamp", wordListFiltered.get(position).getTimestamp());
                    i.putExtra("audioUrl", wordListFiltered.get(position).getAudioUrl());
                    i.putExtra("reported", wordListFiltered.get(position).getReported());
                    i.putExtra("translatedKhasi", wordListFiltered.get(position).isTranslatedToKhasi());
                    i.putExtra("noOfVotes", wordListFiltered.get(position).getNoOfVotes());
                    i.putExtra("userName", wordListFiltered.get(position).getUserName());
                    i.putExtra("wordType", wordListFiltered.get(position).getWordType());
                    i.putExtra("translatedEnglish", wordListFiltered.get(position).isTranslatedToEnglish());
                    i.putExtra("translatedHindi", wordListFiltered.get(position).isTranslatedToHindi());
                    i.putExtra("translatedGaro", wordListFiltered.get(position).isTranslatedToGaro());
                    i.putExtra("englishMeaning", wordListFiltered.get(position).getEnglishMeaning());
                    i.putExtra("khasiMeaning", wordListFiltered.get(position).getKhasiMeaning());
                    i.putExtra("activity",activity);

                    if (fromLang.equals("Khasi")) {
                        i.putExtra("fromLanguage", "KHA");
                        i.putExtra("toLanguage", "ENG");
                    } else if (fromLang.equals("English")) {
                        i.putExtra("fromLanguage", "ENG");
                        i.putExtra("toLanguage", "KHA");
                    }//Added these for later ENG-GARO GARO-ENG
                    else if (fromLang.equals("Garo")) {
                        i.putExtra("fromLanguage", "GARO");
                        i.putExtra("toLanguage", "ENG");
                    }
//                else if(fromLang.equals("Hindi")){
//                    i.putExtra("fromLanguage", "HIN");
//                    i.putExtra("toLanguage", "ENG");
//
//                }
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }catch (Exception e)
                {
                    Toast.makeText(context, "Fatal Error Occurred "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    //e.printStackTrace();
                }
            }
        });
        holder.fromSentenceLanguage.setTextColor(context.getResources().getColor(R.color.colorWhite));
        if(fromLang.equals("Khasi")) {
            holder.fromSentenceLanguage.setText(R.string.kha);
            holder.fromSentenceLanguage.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        }
        else {
            holder.fromSentenceLanguage.setText(R.string.eng);
            holder.fromSentenceLanguage.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public int getItemCount() {
        if(!toasted)
        if(wordListFiltered.size()==0)
        {
            Toast.makeText(context, "Try search online to find the word you are looking for", Toast.LENGTH_SHORT).show();
            toasted=true;
        }
        else{toasted=false;}
        return wordListFiltered.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView wordItem;
        TextView englishWord,isTranslated,fromSentenceLanguage,userName,category;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            englishWord=itemView.findViewById(R.id.fromSentence);
            wordItem=itemView.findViewById(R.id.sentenceItem);
            isTranslated=itemView.findViewById(R.id.isTranslated);
            fromSentenceLanguage=itemView.findViewById(R.id.fromSentenceLanguage);
            userName=itemView.findViewById(R.id.userName);
            category=itemView.findViewById(R.id.category);
        }
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
