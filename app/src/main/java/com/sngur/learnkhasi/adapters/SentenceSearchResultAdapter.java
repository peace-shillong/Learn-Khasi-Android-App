package com.sngur.learnkhasi.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.ui.activity.SentenceViewActivity;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class SentenceSearchResultAdapter extends RecyclerView.Adapter<SentenceSearchResultAdapter.MyViewHolder> implements Filterable {

    Context context;
    List<Sentence> sentenceList,sentenceListFiltered;
    List<String> sentenceId,sentenceIdFiltered;
    String fromLang,activity;
    //Done Filter Search Result on View All

    public SentenceSearchResultAdapter() {
    }

    public SentenceSearchResultAdapter(Context context, List<Sentence> sentenceList,List<String> sentenceId,String fromLang) {
        this.context = context;
        this.sentenceList = sentenceList;
        this.sentenceId=sentenceId;
        this.fromLang=fromLang;
        this.sentenceListFiltered=sentenceList;
        sentenceIdFiltered=sentenceId;
        activity="none";
    }

    public SentenceSearchResultAdapter(Context context, List<Sentence> sentenceList, List<String> sentenceId, String fromLang, String activity) {
        this.context = context;
        this.sentenceList = sentenceList;
        this.sentenceId=sentenceId;
        this.fromLang=fromLang;
        this.sentenceListFiltered=sentenceList;
        sentenceIdFiltered=sentenceId;
        this.activity=activity;
    }

    @NonNull
    @Override
    public SentenceSearchResultAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_sentence_list, parent, false);
        MyViewHolder vh= new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final SentenceSearchResultAdapter.MyViewHolder holder, final int position) {
        holder.englishSentence.setText(sentenceListFiltered.get(position).getSentence());

        if(sentenceListFiltered.get(position).getTranslatedToKhasi() || sentenceListFiltered.get(position).isTranslatedToEnglish()) {
            //Log.d("DATA",sentenceListFiltered.get(position).getTranslatedToKhasi()+" - "+sentenceListFiltered.get(position).getSentence());
            holder.isTranslated.setText("Answered");
            holder.isTranslated.setTextColor(context.getResources().getColor(R.color.colorWhite));
            holder.isTranslated.setBackground(context.getResources().getDrawable(R.drawable.greenbg));
        }
        holder.category.setText("Category: "+sentenceListFiltered.get(position).getCategory());
        holder.userName.setText("Added by: "+sentenceListFiltered.get(position).getUserName());
        holder.sentenceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(context, SentenceViewActivity.class);
                //Log.d("DATA",sentenceIdFiltered.get(position)+" IS ");
                i.putExtra("sentenceId",sentenceIdFiltered.get(position));
                i.putExtra("userId",sentenceListFiltered.get(position).getUserId());
                i.putExtra("sentence",sentenceListFiltered.get(position).getSentence());
                i.putExtra("timestamp",sentenceListFiltered.get(position).getTimestamp());
                i.putExtra("audioUrl",sentenceListFiltered.get(position).getAudioUrl());
                i.putExtra("reported",sentenceListFiltered.get(position).getReported());
                i.putExtra("translatedKhasi",sentenceListFiltered.get(position).getTranslatedToKhasi());
                i.putExtra("noOfVotes",sentenceListFiltered.get(position).getNoOfVotes());
                i.putExtra("userName",sentenceListFiltered.get(position).getUserName());
                i.putExtra("category",sentenceListFiltered.get(position).getCategory());
                i.putExtra("translatedEnglish",sentenceListFiltered.get(position).isTranslatedToEnglish());
                i.putExtra("translatedHindi",sentenceListFiltered.get(position).isTranslatedToHindi());
                i.putExtra("translatedGaro",sentenceListFiltered.get(position).isTranslatedToGaro());
                i.putExtra("activity",activity);

                if(fromLang.equals("Khasi")) {
                    i.putExtra("fromLanguage", "KHA");
                    i.putExtra("toLanguage", "ENG");
                }
                else if(fromLang.equals("English")){
                    i.putExtra("fromLanguage", "ENG");
                    i.putExtra("toLanguage", "KHA");
                }//Added these for later ENG-GARO GARO-ENG
                else if(fromLang.equals("Garo")){
                    i.putExtra("fromLanguage", "GARO");
                    i.putExtra("toLanguage", "ENG");

                }
                else if(fromLang.equals("Hindi")){
                    i.putExtra("fromLanguage", "HIN");
                    i.putExtra("toLanguage", "ENG");

                }
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
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
        if(sentenceListFiltered.size()<1)
        {
            Toast.makeText(context, "Try Searching ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return sentenceListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    sentenceListFiltered = sentenceList;
                    sentenceIdFiltered=sentenceId;
                } else {
                    List<Sentence> filteredList = new ArrayList<>();
                    List<String> filteredIdList=new ArrayList<>();
                    for (int i=0;i<sentenceList.size();i++) {
                        Sentence row = sentenceList.get(i);
                        String sentenceIdRow=sentenceId.get(i);
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for sentence that matches
                        if (row.getSentence().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                            filteredIdList.add(sentenceIdRow);
                            //Log.d("DATA","Result");
                        }
                    }

                    sentenceListFiltered = filteredList;
                    sentenceIdFiltered=filteredIdList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = sentenceListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                sentenceListFiltered = (ArrayList<Sentence>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView sentenceItem;
        TextView englishSentence,isTranslated,fromSentenceLanguage,userName,category;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            englishSentence=itemView.findViewById(R.id.fromSentence);
            sentenceItem=itemView.findViewById(R.id.sentenceItem);
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
