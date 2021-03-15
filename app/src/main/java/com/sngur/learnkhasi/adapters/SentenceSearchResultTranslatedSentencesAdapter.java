package com.sngur.learnkhasi.adapters;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.model.Sentence;
import com.sngur.learnkhasi.model.room.Editors;
import com.sngur.learnkhasi.model.room.UserSentenceVotes;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.EditorsDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceVotesDao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import static com.sngur.learnkhasi.MainActivity.minVotes;

public class SentenceSearchResultTranslatedSentencesAdapter extends RecyclerView.Adapter<SentenceSearchResultTranslatedSentencesAdapter.MyViewHolder> {

    List<Sentence> translatedSentencesList;
    List<String> translatedSentencesKey;
    Context context;
    String currentUserId,originalSentenceId;
    int originalSentenceNoOfVotes;
    DatabaseReference rootRef,userPointsRef;
    MediaPlayer mediaPlayer;
    private int lastHighlightedPosition,maxVote;
    //edit sentence
    private TextInputEditText translatedSentence;
    private ImageButton record,play;
    private Button answerBtn;
    private AlertDialog alertDialog;
    private Sentence updatedSentence;
    private int updatedItemPosition;
    private static LearnKhasiDatabase db;
    private static List<String> votedSentencesId;
    private static List<UserSentenceVotes> votedSentences;
    private Integer userPoints;
    private String updateUserId,msg,fromLang,toLang;
    private String pathFromLangSentence,pathtoLangSentence,pathfromToLangSentence,pathtoFromLangSentence,pathSentenceVote;//for dynamic sentences
    private List<Editors> editorsList;

    public SentenceSearchResultTranslatedSentencesAdapter(List<Sentence> translatedSentencesList, Context context,String currentUserId,String originalSentenceId,List<String> translatedSentencesKey,int originalSentenceNoOfVotes,String fromLang,String toLang,List<Editors> editorsList) {
        this.translatedSentencesList = translatedSentencesList;
        this.context = context;
        this.currentUserId=currentUserId;
        this.originalSentenceId=originalSentenceId;
        this.translatedSentencesKey=translatedSentencesKey;
        this.originalSentenceNoOfVotes=originalSentenceNoOfVotes;
        this.fromLang=fromLang;
        this.toLang=toLang;
        lastHighlightedPosition=-1;
        maxVote=0;

        this.editorsList=new ArrayList<>();
        this.editorsList=editorsList;

        db=LearnKhasiDatabase.getDatabase(context);
        rootRef= FirebaseDatabase.getInstance().getReference();
        //Highlight the Sentence Card with most votes
        for(int i=0;i<translatedSentencesList.size();i++) {
            if (translatedSentencesList.get(i).getNoOfVotes() > maxVote) {
                maxVote = translatedSentencesList.get(i).getNoOfVotes();
                lastHighlightedPosition = i;
            }
        }
        updatedItemPosition=-1;
        db=LearnKhasiDatabase.getDatabase(context);
        votedSentencesId=new ArrayList<>();
        votedSentences=new ArrayList<>();
        getUserVoteData();
        userPoints=0;
        if(fromLang.equals("KHA") && toLang.equals("ENG")){
            pathFromLangSentence="khasi_sentence/";
            pathtoLangSentence="english_sentence/";
            pathfromToLangSentence="khasi_english_sentence/";
            pathtoFromLangSentence="english_khasi_sentence/";
            pathSentenceVote="english_sentence_vote/";
        }
        else if(fromLang.equals("ENG") && toLang.equals("KHA")){ //Original ENG -KHA
            pathFromLangSentence="english_sentence/";
            pathtoLangSentence="khasi_sentence/";
            pathfromToLangSentence="english_khasi_sentence/";
            pathtoFromLangSentence="khasi_english_sentence/";
            pathSentenceVote="khasi_sentence_vote/";
        }
        else if(fromLang.equals("ENG") && toLang.equals("GARO")){ //Original ENG -KHA
            pathFromLangSentence="english_sentence/";
            pathtoLangSentence="garo_sentence/";
            pathfromToLangSentence="english_garo_sentence/";
            pathtoFromLangSentence="garo_english_sentence/";
            pathSentenceVote="garo_sentence_vote/";
        }
        else if(fromLang.equals("ENG") && toLang.equals("HIN")){ //Original ENG -KHA
            pathFromLangSentence="english_sentence/";
            pathtoLangSentence="hindi_sentence/";
            pathfromToLangSentence="english_hindi_sentence/";
            pathtoFromLangSentence="hindi_english_sentence/";
            pathSentenceVote="hindi_sentence_vote/";
        }
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_translated_sentence, parent, false);
        SentenceSearchResultTranslatedSentencesAdapter.MyViewHolder vh= new SentenceSearchResultTranslatedSentencesAdapter.MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.translatedSentence.setText(translatedSentencesList.get(position).getSentence());
        holder.userName.setText("Added by: "+translatedSentencesList.get(position).getUserName());
        if(translatedSentencesList.get(position).getAudioUrl().equals(""))
        {
            holder.playAudio.setVisibility(View.GONE);
        }
        //DONE test update only one field in English Sentence
        holder.playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //play audio
                //Toast.makeText(context, "DONE Play Audio", Toast.LENGTH_SHORT).show();
                if(mediaPlayer!=null){
                    mediaPlayer.release();
                    mediaPlayer=null;
                    mediaPlayer=new MediaPlayer();
                }
                else {
                    mediaPlayer=new MediaPlayer();
                }
                try {
                    mediaPlayer.setDataSource(translatedSentencesList.get(position).getAudioUrl());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mediaPlayer.setAudioAttributes(
                                new AudioAttributes
                                        .Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build());
                    }else
                    {
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
//                            Toast.makeText(context, "Playing Audio", Toast.LENGTH_LONG).show();
                        }
                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Toast.makeText(context, "Audio Completed", Toast.LENGTH_LONG).show();
                        }
                    });
                    Toast.makeText(context, "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    // make something
                    //Log.d("ERROR",e.getMessage()+"");
//                    e.printStackTrace();
                }

            }
        });

        holder.noOfVotes.setText(translatedSentencesList.get(position).getNoOfVotes()+"");
        holder.voteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "add +ve vote from this user", Toast.LENGTH_SHORT).show();
                //originalSentenceNoOfVotes++;

                boolean vote=false;
                for (int i = 0; i < translatedSentencesKey.size(); i++) {
                    if (votedSentencesId.contains(translatedSentencesKey.get(i))) {
                        for (int j = 0; j < votedSentences.size(); j++) {
                            if (votedSentences.get(j).getSentenceId().equals(translatedSentencesKey.get(i))) {
                                vote = votedSentences.get(j).isVote();
                                //Log.d("VOTED UP", vote + "-"+votedSentences.size());
                                break;
                            }
                        }
                        break;
                    }
                }
                if (!vote) {
                    //Test on Like/Unlike Does user gets points updated or not
                    //Update points
                    updateUserId=translatedSentencesList.get(position).getUserId();
                    userPointsRef=FirebaseDatabase.getInstance().getReference();
                    userPointsRef.child("user_points/"+updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pointsExist = (int) snapshot.getChildrenCount();
                            if(pointsExist>0)
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    userPoints = postSnapshot.getValue(Integer.class);
                                    //update Like Data
                                    userPoints+=10;
                                    updateUserVoteData(translatedSentencesList.get(position).getNoOfVotes(),true,translatedSentencesKey.get(position),userPoints);
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteData(translatedSentencesList.get(position).getNoOfVotes(),true,translatedSentencesKey.get(position),userPoints);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
                else {
                    Toast.makeText(context, "You have already voted", Toast.LENGTH_SHORT).show();
                }


            }
        });

        holder.voteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, " DONE add -ve vote from this user", Toast.LENGTH_SHORT).show();
                //It works: if not in likes table : allow like/unlike
                //just use loop here
                boolean vote=true;
                for (int i = 0; i < translatedSentencesKey.size(); i++) {
                    if (votedSentencesId.contains(translatedSentencesKey.get(i))) {
                        for (int j = 0; j < votedSentences.size(); j++) {
                            if (votedSentences.get(j).getSentenceId().equals(translatedSentencesKey.get(i))) {
                                vote = votedSentences.get(j).isVote();
                                //Log.d("VOTED DOWN", vote + "-"+votedSentences.size());
                                break;
                            }
                        }
                        break;
                    }
                }
                if (vote) {
                    //Update points
                    updateUserId=translatedSentencesList.get(position).getUserId();
                    userPointsRef=FirebaseDatabase.getInstance().getReference();
                    userPointsRef.child("user_points/"+updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pointsExist = (int) snapshot.getChildrenCount();
                            if(pointsExist>0)
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    userPoints = postSnapshot.getValue(Integer.class);
                                    //update Like Data
                                    if(userPoints > 0)
                                        userPoints-=5;
                                    updateUserVoteData(translatedSentencesList.get(position).getNoOfVotes(),false,translatedSentencesKey.get(position),userPoints);
                                    break;
                                }
                            else{
                                //new data for user
                                //Update Like data
                                updateUserVoteData(translatedSentencesList.get(position).getNoOfVotes(),false,translatedSentencesKey.get(position),userPoints);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else{
                    Toast.makeText(context, "You have already voted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Highlight the sentence with maxVotes
        if(lastHighlightedPosition==position) {
            holder.translatedCardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            holder.translatedSentence.setTextColor(context.getResources().getColor(R.color.colorWhite));
            holder.voteUp.setColorFilter(context.getResources().getColor(R.color.colorWhite));
            holder.voteDown.setColorFilter(context.getResources().getColor(R.color.colorWhite));
            holder.playAudio.setColorFilter(context.getResources().getColor(R.color.colorWhite));
            holder.editItem.setColorFilter(context.getResources().getColor(R.color.colorWhite));
            holder.noOfVotes.setTextColor(context.getResources().getColor(R.color.colorWhite));
            holder.userName.setTextColor(context.getResources().getColor(R.color.colorWhite));
        }
        //editItem visible to the user who added the sentence
        //Editors editor=new Editors(currentUserId);
        Boolean found=false;
        for(Editors user: editorsList)
        {
            if(user.getUserId().equals(currentUserId)) {
                found = true;
                break;
            }
        }
        if(currentUserId.equals(translatedSentencesList.get(position).getUserId())  || found)
        {
//            Log.d("SIZE",editorsList.size()+"");
            if(translatedSentencesList.get(position).getNoOfVotes()<minVotes)
                holder.editItem.setVisibility(View.VISIBLE);
            holder.editItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //open Dialog to Edit the Translation
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    ViewGroup viewGroup = view.findViewById(android.R.id.content);
                    final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_answer, viewGroup, false);
                    builder.setView(dialogView);
                    LinearLayout bigLevel=dialogView.findViewById(R.id.bigLevel);
                    bigLevel.setVisibility(View.GONE);
                    translatedSentence=dialogView.findViewById(R.id.translatedSentence);
                    record=dialogView.findViewById(R.id.record);
                    play=dialogView.findViewById(R.id.play);
                    answerBtn=dialogView.findViewById(R.id.buttonAnswer);
                    translatedSentence.setText(holder.translatedSentence.getText());
                    record.setVisibility(View.GONE);
                    play.setVisibility(View.GONE);
                    answerBtn.setText(R.string.edit_translation);
                    answerBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(translatedSentencesList.get(position).getSentence().equals(translatedSentence.getText().toString()))
                            {
                                Toast.makeText(context, "Sentence has not been changed", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                            else if(translatedSentence.getText().toString().contains(" ")){
                                //translatedSentencesList.get(position).getUserId change to currentId later
                                String temp=translatedSentence.getText().toString().toLowerCase()+"";
                                String editedSentence=temp.substring(0,1).toUpperCase()+temp.substring(1);
                                updatedSentence=new Sentence(editedSentence,translatedSentencesList.get(position).getUserId(),translatedSentencesList.get(position).getAudioUrl(),translatedSentencesList.get(position).getReported(),translatedSentencesList.get(position).getTimestamp(),translatedSentencesList.get(position).getTranslatedToKhasi(),translatedSentencesList.get(position).getNoOfVotes(),translatedSentencesList.get(position).getUserName(),translatedSentencesList.get(position).getCategory(),translatedSentencesList.get(position).isTranslatedToEnglish(),translatedSentencesList.get(position).isTranslatedToHindi(),translatedSentencesList.get(position).isTranslatedToGaro());
                                //pushData for Update
                                HashMap<String, Object> updatedUserSentence = new HashMap<>();
                                //Log.d("DATA",translatedSentencesKey.get(position));
                                //khasi_sentence original - had a problem set to pathtoLangSentence
                                updatedUserSentence.put(pathtoLangSentence+translatedSentencesKey.get(position)+"/", updatedSentence);

                                rootRef.updateChildren(updatedUserSentence, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if(error!=null){
                                            Toast.makeText(context, "Unable to update sentence", Toast.LENGTH_SHORT).show();
                                        }
                                        Toast.makeText(context, "Sentence Updated", Toast.LENGTH_SHORT).show();
                                        //holder.translatedSentence.setText(translatedSentence.getText().toString());
                                        //translatedSentencesList.get(position).setSentence(translatedSentence.getText().toString());
                                        notifyItemChanged(position);
                                        notifyDataSetChanged();
                                        alertDialog.dismiss();
                                    }
                                });
                            }
                            else {
                                translatedSentence.setError("Enter a sentence");
                            }
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }

    private void updateUserVoteData(int votes,final boolean likeData,final String translatedSentenceKey,Integer userPoints) {
        int noOfVotes = votes;
        msg="Unlike";
        //simplify the like and unlike post
        if(likeData)
        {
            msg="Liked";
            noOfVotes += 1;
        }else{
            if (noOfVotes != 0)
                noOfVotes -= 1;
        }
        //TODO data to update make this dynamic for ENG - KHA and KHA - ENG : more test to do
        HashMap<String, Object> updatedUserData = new HashMap<>();
        //updatedUserData.put("english_sentence/"+originalSentenceId+"/noOfVotes", originalSentenceNoOfVotes);
        updatedUserData.put(pathSentenceVote + currentUserId + "/" + translatedSentenceKey, likeData); //pathSentenceVote khasi_sentence_vote  original
        updatedUserData.put(pathtoLangSentence + translatedSentenceKey + "/noOfVotes", noOfVotes); //Currently this causes a lot of problem in this line : problem solved with : pathtoLangSentence //khasi_sentence original
        updatedUserData.put("user_points/"+updateUserId+"/points",userPoints);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(context, "Unable to vote", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                storeUserVoteData(translatedSentenceKey, toLang, likeData);//todo check this fromLang - khasi_sentence
            }
        });
    }

    private static void storeUserVoteData(final String sentenceId, final String fromLanguage, final boolean vote) {

        class storeUserVoteData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                UserSentenceVotesDao userSentenceVotesDao=db.userSentenceVotesDao();
                UserSentenceVotes userSentenceVotes=new UserSentenceVotes(sentenceId,fromLanguage,vote);
                userSentenceVotesDao.insert(userSentenceVotes);
                //insert vote in db
                //Log.d("DATA","Stored Data - "+sentenceId+"-"+vote);
                getUserVoteData();
                return null;
            }
        }
        storeUserVoteData storeUserVoteData=new storeUserVoteData();
        storeUserVoteData.execute();
    }

    private static void getUserVoteData(){
        class getUserVoteData extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                votedSentencesId.clear();
                votedSentences.clear();
                UserSentenceVotesDao userSentenceVotesDao=db.userSentenceVotesDao();
                votedSentences=userSentenceVotesDao.getUserVotes();
                votedSentencesId=userSentenceVotesDao.getUserVotedSentenceId();
                //get data from db
                return null;
            }
        }
        getUserVoteData getUserVoteData=new getUserVoteData();
        getUserVoteData.execute();
    }

    @Override
    public int getItemCount() {
        return translatedSentencesList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView translatedSentence,noOfVotes,userName;
        ImageView voteUp,voteDown,playAudio,editItem;
        CardView translatedCardView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            translatedSentence=itemView.findViewById(R.id.translatedSentence);
            noOfVotes=itemView.findViewById(R.id.noOfVotes);
            voteUp=itemView.findViewById(R.id.voteUp);
            voteDown=itemView.findViewById(R.id.voteDown);
            playAudio=itemView.findViewById(R.id.playAudio);
            translatedCardView=itemView.findViewById(R.id.translatedCardView);
            editItem=itemView.findViewById(R.id.editItem);
            userName=itemView.findViewById(R.id.userName);
        }
    }
}
