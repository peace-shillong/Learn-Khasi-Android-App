package com.sngur.learnkhasi.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.media.AsyncPlayer;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.Editors;
import com.sngur.learnkhasi.model.room.UserSentenceVotes;
import com.sngur.learnkhasi.model.room.UserWordVoted;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.EditorsDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceVotesDao;
import com.sngur.learnkhasi.roomdb.dao.UserWordVotesDao;
import com.sngur.learnkhasi.ui.activity.WordViewActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.RecyclerView;

import static com.sngur.learnkhasi.MainActivity.minVotes;
import static com.sngur.learnkhasi.ui.activity.WordViewActivity.getUserVoteDataWordView;

public class WordSearchResultTranslatedWordsAdapter extends RecyclerView.Adapter<WordSearchResultTranslatedWordsAdapter.MyViewHolder> {

    List<Word> translatedWordList;
    Context context;
    String currentUserId, toLang, fromLang, originalWordId;
    int noOfVotes, orignalNoOfVotes, maxVote;
    DatabaseReference rootRef, userPointsRef;
    MediaPlayer mediaPlayer;
    private TextInputEditText translatedWordTxt, englishMeaningTxt, khasiMeaningTxt;
    private ImageButton record, play;
    private Button answerBtn;
    private AlertDialog alertDialog;
    private Word updatedWord;
    private LearnKhasiDatabase db;
    private List<String> votedWordId;
    private List<UserWordVoted> votedWords;
    private Integer userPoints;
    private String updateUserId, msg;
    private String pathFromLangWord, pathToLangWord, pathFromToLangWord, pathToFromLangWord, pathWordVote;//for dynamic word ref
    private int localNoOfVotes;
    private List<String> wordTypeList;
    private Spinner wordTypeSpinner;
    private ArrayAdapter<String> arrayAdapter;
    private TextToSpeech tts;
    //for the resulting translated words we just like/unlike no reporting them, for words then we need
    private List<Editors> editorsList;

    public WordSearchResultTranslatedWordsAdapter(List<Word> translatedWordList, Context context, String currentUserId, int noOfVotes, String fromLang, String toLang, String originalWordId, List<String> votedWordId, List<UserWordVoted> votedWords,List<Editors> editorsList) {
        this.translatedWordList = translatedWordList;
        this.context = context;
        this.currentUserId = currentUserId;
        this.toLang = toLang;
        this.noOfVotes = noOfVotes;
        this.fromLang = fromLang;
        this.orignalNoOfVotes = noOfVotes;
        this.votedWordId = votedWordId;
        this.originalWordId = originalWordId;
        this.votedWords = votedWords;
        maxVote = 0;

        this.editorsList=new ArrayList<>();
        this.editorsList=editorsList;
        db=LearnKhasiDatabase.getDatabase(context);
        //loadEditors();
        rootRef = FirebaseDatabase.getInstance().getReference();
        db = LearnKhasiDatabase.getDatabase(context);
        userPoints = 0;
        wordTypeList = new ArrayList<>();
        wordTypeList.add("Noun");
        wordTypeList.add("Verb");
        wordTypeList.add("Pronoun");
        wordTypeList.add("Preposition");
        wordTypeList.add("Adjective");
        wordTypeList.add("Adverb");
        wordTypeList.add("Conjunction");
        wordTypeList.add("Interjection");
        arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, wordTypeList);

        if (fromLang.equals("KHA") && toLang.equals("ENG")) {
            pathFromLangWord = "khasi_word/";
            pathToLangWord = "english_word/";
            pathFromToLangWord = "khasi_english_word/";
            pathToFromLangWord = "english_khasi_word/";
            pathWordVote = "english_word_vote/";
        } else if (fromLang.equals("ENG") && toLang.equals("KHA")) { //Original ENG -KHA
            pathFromLangWord = "english_word/";
            pathToLangWord = "khasi_word/";
            pathFromToLangWord = "english_khasi_word/";
            pathToFromLangWord = "khasi_english_word/";
            pathWordVote = "khasi_word_vote/";
        } else if (fromLang.equals("ENG") && toLang.equals("GARO")) { //Original ENG -KHA
            pathFromLangWord = "english_word/";
            pathToLangWord = "garo_word/";
            pathFromToLangWord = "english_garo_word/";
            pathToFromLangWord = "garo_english_word/";
            pathWordVote = "garo_word_vote/";
        } else if (fromLang.equals("KHA") && toLang.equals("GARO")) { //Original ENG -KHA
            pathFromLangWord = "khasi_word/";
            pathToLangWord = "garo_word/";
            pathFromToLangWord = "khasi_garo_word/";
            pathToFromLangWord = "garo_khasi_word/";
            pathWordVote = "garo_word_vote/";
        } else if (fromLang.equals("GARO") && toLang.equals("ENG")) { //Original ENG -KHA
            pathFromLangWord = "garo_word/";
            pathToLangWord = "english_word/";
            pathFromToLangWord = "garo_english_word/";
            pathToFromLangWord = "english_garo_word/";
            pathWordVote = "english_word_vote/";
        }
        if (votedWordId == null || votedWordId.size() == 0 || votedWords == null || votedWords.size() == 0)
            getUserVoteData();
    }

    @NonNull
    @Override
    public WordSearchResultTranslatedWordsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_translated_word, parent, false);
        WordSearchResultTranslatedWordsAdapter.MyViewHolder vh = new WordSearchResultTranslatedWordsAdapter.MyViewHolder(v);
        return vh;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull final WordSearchResultTranslatedWordsAdapter.MyViewHolder holder, final int position) {
        holder.toWordLanguage.setText(toLang);
        holder.toWordLanguage.setTextColor(context.getResources().getColor(R.color.colorWhite));
        if (toLang.equalsIgnoreCase("KHA")) {
            holder.toWordLanguage.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.toWordLanguage.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
        holder.toWord.setText(translatedWordList.get(position).getWord());
        holder.wordTypeToWord.setText(translatedWordList.get(position).getWordType());
        holder.userNameToWord.setText(translatedWordList.get(position).getUserName());
        String meanings = "";
        if (translatedWordList.get(position).getKhasiMeaning() != null)
            if (!translatedWordList.get(position).getKhasiMeaning().equalsIgnoreCase(""))
                meanings = meanings + "Khasi Meaning:\n" + translatedWordList.get(position).getKhasiMeaning() + "\n\n";
        if (translatedWordList.get(position).getEnglishMeaning() != null)
            if (!translatedWordList.get(position).getEnglishMeaning().equalsIgnoreCase(""))
                meanings = meanings + "English Meaning:\n" + translatedWordList.get(position).getEnglishMeaning();
        holder.wordMeaningsIfAny.setText(meanings);

        if (!meanings.equals("")) {
            holder.wordMeaningsIfAny.setVisibility(View.VISIBLE);
            //Toast.makeText(context, "meanings "+meanings, Toast.LENGTH_SHORT).show();
            holder.wordMeaningsIfAnyView.setVisibility(View.VISIBLE);
        }
        //Log.e("VOTES",translatedWordList.get(position).getNoOfVotes()+"");
        holder.noOfVotes.setText(translatedWordList.get(position).getNoOfVotes() + "");
        //voting
        holder.voteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "add +ve vote from this user "+votedWords.size(), Toast.LENGTH_SHORT).show();
                //originalSentenceNoOfVotes++;

                if (votedWords.size() > 0) {
                    boolean vote = false;
                    for (int i = 0; i < votedWords.size(); i++) {
//                        Log.e("DATA", votedWords.get(i).getWordId() + " - " + translatedWordList.get(position).getWordId() + " LIST");
                        if (votedWords.get(i).getWordId().equals(translatedWordList.get(position).getWordId())) {
                            vote = votedWords.get(i).isVote();
                            break;
                        }
                    }
                    if (!vote) {
                        //Test on Like/Unlike Does user gets points updated or not
                        //Update points
                        updateUserId = translatedWordList.get(position).getUserId();
                        userPointsRef = FirebaseDatabase.getInstance().getReference();
                        userPointsRef.child("user_points/" + updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int pointsExist = (int) snapshot.getChildrenCount();
                                if (pointsExist > 0)
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        userPoints = postSnapshot.getValue(Integer.class);
                                        //update Like Data
                                        userPoints += 10;
                                        updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), true, translatedWordList.get(position).getWordId(), userPoints);
                                        break;
                                    }


                                else {
                                    //new data for user
                                    //Update Like data
                                    updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), true, translatedWordList.get(position).getWordId(), userPoints);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        Toast.makeText(context, "You have already voted", Toast.LENGTH_SHORT).show();
                    }
                    //hide voteUp
                    //holder.voteUp.setVisibility(View.GONE);
                    //holder.voteDown.setVisibility(View.VISIBLE);
                } else {
                    //get by wordId
                    getUserVoteByWordId(translatedWordList.get(position).getWordId(), position);
                }


            }
        });

        holder.voteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, " DONE add -ve vote from this user", Toast.LENGTH_SHORT).show();
                //It works: if not in likes table : allow like/unlike
                //just use loop here
                if (votedWords.size() > 0) {
                    boolean vote = true;
                    for (int i = 0; i < votedWordId.size(); i++) {
                        //Log.e("DATA",votedWords.get(i).getWordId()+" - "+translatedWordList.get(position).getWordId()+" "+votedWordId.size());
                        if (votedWords.get(i).getWordId().equals(translatedWordList.get(position).getWordId())) {
                            vote = votedWords.get(i).isVote();
                            break;
                        }
                    }
                    //                for (int i = 0; i < votedWordId.size(); i++) {
                    //                    if (votedWords.contains(votedWordId.get(i))) {
                    //                        for (int j = 0; j < votedSentences.size(); j++) {
                    //                            if (votedSentences.get(j).getSentenceId().equals(votedWordId.get(i))) {
                    //                                vote = votedSentences.get(j).isVote();
                    //                                Log.d("VOTED DOWN", vote + "-"+votedSentences.size());
                    //                                break;
                    //                            }
                    //                        }
                    //                        break;
                    //                    }
                    //                }
                    if (vote) {
                        //Update points
                        updateUserId = translatedWordList.get(position).getUserId();
                        userPointsRef = FirebaseDatabase.getInstance().getReference();
                        userPointsRef.child("user_points/" + updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int pointsExist = (int) snapshot.getChildrenCount();
                                if (pointsExist > 0)
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        userPoints = postSnapshot.getValue(Integer.class);
                                        //update Like Data
                                        if (userPoints > 0)
                                            userPoints -= 5;
                                        updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), false, translatedWordList.get(position).getWordId(), userPoints);
                                        break;
                                    }
                                else {
                                    //new data for user
                                    //Update Like data
                                    updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), false, translatedWordList.get(position).getWordId(), userPoints);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //hide voteDown
                        holder.voteUp.setVisibility(View.VISIBLE);
                        holder.voteDown.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(context, "You have already voted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();
                    getUserVoteDownByWordId(translatedWordList.get(position).getWordId(), position);
                }
            }
        });

        //edit word item
        //Editors editor=new Editors(currentUserId);
        //trying to find an editor
        Boolean found=false;
        for(Editors user: editorsList)
        {
            if(user.getUserId().equals(currentUserId)) {
                found = true;
                break;
            }
        }

        //Log.e("DATA",found+ " Editor");
        if (currentUserId.equals(translatedWordList.get(position).getUserId()) || found) {
//            Log.d("SIZE",editorsList.size()+"");
            if(translatedWordList.get(position).getNoOfVotes()<minVotes)
                holder.editItem.setVisibility(View.VISIBLE);
            holder.editItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //open Dialog to Edit the Translation
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    ViewGroup viewGroup = view.findViewById(android.R.id.content);
                    final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_translated_word, viewGroup, false);
                    builder.setView(dialogView);
                    LinearLayout bigLevel = dialogView.findViewById(R.id.bigLevel);
                    bigLevel.setVisibility(View.GONE);
                    translatedWordTxt = dialogView.findViewById(R.id.translatedWord);
                    englishMeaningTxt = dialogView.findViewById(R.id.englishMeaning);
                    khasiMeaningTxt = dialogView.findViewById(R.id.khasiMeaning);
                    record = dialogView.findViewById(R.id.record);
                    play = dialogView.findViewById(R.id.play);
                    answerBtn = dialogView.findViewById(R.id.buttonAnswer);
                    translatedWordTxt.setText(holder.toWord.getText());
                    englishMeaningTxt.setText(translatedWordList.get(position).getEnglishMeaning());
                    khasiMeaningTxt.setText(translatedWordList.get(position).getKhasiMeaning());
                    wordTypeSpinner = dialogView.findViewById(R.id.translatedWordType);
                    wordTypeSpinner.setAdapter(arrayAdapter);
                    //set the spinner view selected Id
                    wordTypeSpinner.setSelection(wordTypeList.indexOf(translatedWordList.get(position).getWordType()));
                    //Log.e("DATA",englishMeaningTxt.getText()+"");
                    //add filter for no space
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
                    translatedWordTxt.setFilters(new InputFilter[]{filter});
                    record.setVisibility(View.GONE);
                    play.setVisibility(View.GONE);
                    answerBtn.setText(R.string.edit_translation);
                    answerBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (translatedWordList.get(position).getWord().equals(translatedWordTxt.getText().toString()) && translatedWordList.get(position).getEnglishMeaning().equals(englishMeaningTxt.getText().toString()) && translatedWordList.get(position).getKhasiMeaning().equals(khasiMeaningTxt.getText().toString())) {
                                Toast.makeText(context, "Word and meanings have not been changed", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            } else if (translatedWordTxt.getText().toString().length() >= 2) {
                                //translatedWordList.get(position).getUserId change to currentId later
                                String word = translatedWordTxt.getText().toString();
                                word = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                                updatedWord = new Word(translatedWordList.get(position).getWordId() + "", word, translatedWordList.get(position).getUserId(), translatedWordList.get(position).getAudioUrl(), translatedWordList.get(position).getReported(), translatedWordList.get(position).getTimestamp(), translatedWordList.get(position).getNoOfVotes(), translatedWordList.get(position).getUserName(), wordTypeSpinner.getSelectedItem().toString(), englishMeaningTxt.getText().toString(), khasiMeaningTxt.getText().toString(), translatedWordList.get(position).isTranslatedToKhasi(), translatedWordList.get(position).isTranslatedToEnglish(), translatedWordList.get(position).isTranslatedToHindi(), translatedWordList.get(position).isTranslatedToGaro());
                                //pushData for Update
                                HashMap<String, Object> updatedUserWord = new HashMap<>();
                                //Log.d("DATA",votedWordId.get(position));
                                //khasi_sentence original - had a problem set to pathtoLangSentence
                                updatedUserWord.put(pathToLangWord + translatedWordList.get(position).getWordId() + "/", updatedWord);

                                rootRef.updateChildren(updatedUserWord, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error != null) {
                                            Toast.makeText(context, "Unable to update word", Toast.LENGTH_SHORT).show();
                                        }
                                        Toast.makeText(context, "Word Updated", Toast.LENGTH_SHORT).show();
                                        //holder.translatedSentence.setText(translatedSentence.getText().toString());
                                        //translatedWordList.get(position).setSentence(translatedSentence.getText().toString());
                                        notifyItemChanged(position);
                                        notifyDataSetChanged();
                                        alertDialog.dismiss();
                                    }
                                });
                            } else {
                                //display error
                                translatedWordTxt.setError("Please enter more than 2 characters for the word");
                            }
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }

        //Using TTS
        if (fromLang != null && toLang != null)
            if (fromLang.equals("KHA") && toLang.equals("ENG")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.playAudio.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimaryDark)));
                    //holder.playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_volume_up_24));
                    holder.playAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
                }
                holder.playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onInit(int status) {
                                    if (status == TextToSpeech.SUCCESS) {
                                        int result = tts.setLanguage(Locale.US);
                                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Toast.makeText(context, "This Language is not supported in your phone", Toast.LENGTH_SHORT).show();
                                        } else {
                                            tts.setLanguage(Locale.US);
                                            tts.speak(holder.toWord.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                                        }
                                    } else
                                        Toast.makeText(context, "Unable to play audio", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                });
            } else if (fromLang.equals("ENG") && (toLang.equals("KHA") || toLang.equals("GARO"))) {
                //play audio from url
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.playAudio.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorAccent)));
                }
                if (translatedWordList.get(position).getAudioUrl() != null && !translatedWordList.get(position).getAudioUrl().equals("null") && !translatedWordList.get(position).getAudioUrl().equals("") && !translatedWordList.get(position).getAudioUrl().equals("")) {
                    holder.playAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
                    holder.playAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //play audio
                            if (mediaPlayer != null) {
                                mediaPlayer.release();
                                mediaPlayer = null;
                                mediaPlayer = new MediaPlayer();
                            } else {
                                mediaPlayer = new MediaPlayer();
                            }
                            try {
                                mediaPlayer.setDataSource(translatedWordList.get(position).getAudioUrl());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mediaPlayer.setAudioAttributes(
                                            new AudioAttributes
                                                    .Builder()
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                    .build());
                                } else {
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
//                            e.printStackTrace();
                            }
                        }
                    });
                }
            }
    }

    //Added these fixes for the words in list since list is modified
    private void getUserVoteDownByWordId(final String wordId, final int position) {
        class getUserVote extends AsyncTask<Void,Void,Void>
        {
            Boolean vote;
            @Override
            protected Void doInBackground(Void... voids) {
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                vote=userWordVotesDao.getUserVoteById(wordId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                for (int i = 0; i < votedWordId.size(); i++) {
                    //Log.e("DATA",votedWords.get(i).getWordId()+" - "+translatedWordList.get(position).getWordId()+" "+votedWordId.size());
                    if (votedWords.get(i).getWordId().equals(translatedWordList.get(position).getWordId())) {
                        vote = votedWords.get(i).isVote();
                        break;
                    }
                }
                //                for (int i = 0; i < votedWordId.size(); i++) {
                //                    if (votedWords.contains(votedWordId.get(i))) {
                //                        for (int j = 0; j < votedSentences.size(); j++) {
                //                            if (votedSentences.get(j).getSentenceId().equals(votedWordId.get(i))) {
                //                                vote = votedSentences.get(j).isVote();
                //                                Log.d("VOTED DOWN", vote + "-"+votedSentences.size());
                //                                break;
                //                            }
                //                        }
                //                        break;
                //                    }
                //                }
                if (vote) {
                    //Update points
                    updateUserId = translatedWordList.get(position).getUserId();
                    userPointsRef = FirebaseDatabase.getInstance().getReference();
                    userPointsRef.child("user_points/" + updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pointsExist = (int) snapshot.getChildrenCount();
                            if (pointsExist > 0)
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    userPoints = postSnapshot.getValue(Integer.class);
                                    //update Like Data
                                    if (userPoints > 0)
                                        userPoints -= 5;
                                    updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), false, translatedWordList.get(position).getWordId(), userPoints);
                                    break;
                                }
                            else {
                                //new data for user
                                //Update Like data
                                updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), false, translatedWordList.get(position).getWordId(), userPoints);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(context, "You have already voted", Toast.LENGTH_SHORT).show();
                }
            }
        }
        new getUserVote().execute();
    }

    private void getUserVoteByWordId(final String wordId, final int position) {
        class getUserVote extends AsyncTask<Void,Void,Void>{
            Boolean vote;
            @Override
            protected Void doInBackground(Void... voids) {
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                vote=userWordVotesDao.getUserVoteById(wordId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //checking here now for the fix of wordId and vote
                //Log.e("VOTED",vote+"");
                if (!vote) {
                    //Test on Like/Unlike Does user gets points updated or not
                    //Update points
                    updateUserId = translatedWordList.get(position).getUserId();
                    userPointsRef = FirebaseDatabase.getInstance().getReference();
                    userPointsRef.child("user_points/" + updateUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pointsExist = (int) snapshot.getChildrenCount();
                            if (pointsExist > 0)
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    userPoints = postSnapshot.getValue(Integer.class);
                                    //update Like Data
                                    userPoints += 10;
                                    updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), true, translatedWordList.get(position).getWordId(), userPoints);
                                    break;
                                }


                            else {
                                //new data for user
                                //Update Like data
                                updateUserVoteData(translatedWordList.get(position).getNoOfVotes(), true, translatedWordList.get(position).getWordId(), userPoints);
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
        }
        new getUserVote().execute();
    }


    @Override
    public int getItemCount() {
        return translatedWordList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView toWordLanguage,toWord,wordTypeToWord,userNameToWord,wordMeaningsIfAny,noOfVotes;
        ImageView voteUp,voteDown,playAudio,editItem;
        LinearLayout wordMeaningsIfAnyView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            toWordLanguage=itemView.findViewById(R.id.toWordLanguage);
            toWord=itemView.findViewById(R.id.toWord);
            wordTypeToWord=itemView.findViewById(R.id.wordTypeToWord);
            userNameToWord=itemView.findViewById(R.id.userNameToWord);
            wordMeaningsIfAny=itemView.findViewById(R.id.wordMeanings);
            wordMeaningsIfAnyView=itemView.findViewById(R.id.wordMeaningsIfAny);
            noOfVotes=itemView.findViewById(R.id.noOfVotesToWord);
            voteUp=itemView.findViewById(R.id.voteUpToWord);
            voteDown=itemView.findViewById(R.id.voteDownToWord);
            playAudio=itemView.findViewById(R.id.playAudioToWord);
            editItem=itemView.findViewById(R.id.editToWordItem);
        }
    }

    //my functions
    private void updateUserVoteData(int noOfVotes,  final boolean likeData,final String localWordId, Integer userPoints) {
        localNoOfVotes = noOfVotes;
        msg="Unlike";
        //simplify the like and unlike post
        if(likeData)
        {
            msg="Liked";
            localNoOfVotes += 1;
        }else{
            if (localNoOfVotes != 0)
                localNoOfVotes -= 1;
        }
        //TODO data to update make this dynamic for ENG - KHA and KHA - ENG : more test to do
        HashMap<String, Object> updatedUserData = new HashMap<>();
        //updatedUserData.put("english_sentence/"+originalSentenceId+"/noOfVotes", originalSentenceNoOfVotes);
        updatedUserData.put(pathWordVote + currentUserId + "/" + localWordId, likeData); //pathSentenceVote khasi_sentence_vote  original
        updatedUserData.put(pathToLangWord + localWordId + "/noOfVotes", localNoOfVotes); //Currently this causes a lot of problem in this line : problem solved with : pathtoLangSentence //khasi_sentence original
        updatedUserData.put("user_points/"+updateUserId+"/points",userPoints);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(context, "Unable to vote", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    storeUserVoteData(localWordId, toLang, likeData, localNoOfVotes);//"khasi_word"
                }
            }
        });

    }

    private void storeUserVoteData(final String localWordId, final String fromLanguage, final boolean likeData,int localNoOfVotes) {
        class storeUserVoteData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                //Log.e("LIKE",likeData+" - ");
                UserWordVoted userWordVoted=new UserWordVoted(localWordId,fromLanguage,likeData);
                userWordVotesDao.insert(userWordVoted);
                //insert vote in db
//                Log.e("DATA","Stored Data - "+localWordId+"-"+likeData);
                votedWords=userWordVotesDao.getUserVotes();
                votedWordId=userWordVotesDao.getUserVotedWordId();
                //getUserVoteData();
                //getUserVoteDataWordView();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getUserVoteData();
//                Log.e("DATA","CALLED");
                getUserVoteDataWordView();
            }
        }
        storeUserVoteData storeUserVoteData=new storeUserVoteData();
        storeUserVoteData.execute();
    }
    //update Data
    private void getUserVoteData() {
        class getUserVoteData extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                //votedWordId.clear();
                //votedWords.clear();
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                votedWords=userWordVotesDao.getUserVotes();
                votedWordId=userWordVotesDao.getUserVotedWordId();
                //get data from db
                return null;
            }
        }
        getUserVoteData getUserVoteData=new getUserVoteData();
        getUserVoteData.execute();
    }

}
