package com.sngur.learnkhasi.ui.welcome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sngur.learnkhasi.MainActivity;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.FragmentLoginBinding;
import com.sngur.learnkhasi.model.User;
import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.Editors;
import com.sngur.learnkhasi.model.room.StoredEnglishWord;
import com.sngur.learnkhasi.model.room.StoredKhasiWord;
import com.sngur.learnkhasi.model.room.UserSentenceReported;
import com.sngur.learnkhasi.model.room.UserSentenceVotes;
import com.sngur.learnkhasi.model.room.UserWordReported;
import com.sngur.learnkhasi.model.room.UserWordVoted;
import com.sngur.learnkhasi.roomdb.LearnKhasiDatabase;
import com.sngur.learnkhasi.roomdb.dao.EditorsDao;
import com.sngur.learnkhasi.roomdb.dao.StoredEnglishWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredKhasiWordDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceReportedDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceVotesDao;
import com.sngur.learnkhasi.roomdb.dao.UserWordReportedDao;
import com.sngur.learnkhasi.roomdb.dao.UserWordVotesDao;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "badge";
    private static final String ARG_PARAM2 = "level";
    private int badge,level,english,hindi,garo,points;
    private FragmentLoginBinding binding;
    private static final int RC_SIGN_IN = 9001;
    String name, email,survey1,survey2;
    private boolean exist;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mAuth;
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;
    private AlertDialog alertDialog;
    private DatabaseReference myRef,voteKhasiSentenceRef,voteEnglishSentenceRef,reportedSentenceRef,votedKhasiWordRef,votedEngWordRef,votedGaroWordRef,editorsRef;
    private static LearnKhasiDatabase db;
    private FragmentActivity activity;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.     *
     * @return A new instance of fragment LoginFragment.
     */

    public static LoginFragment newInstance(String param1) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            badge = getArguments().getInt(ARG_PARAM1);
            level = getArguments().getInt(ARG_PARAM2);
            english = getArguments().getInt("english");
            hindi = getArguments().getInt("hindi");
            garo = getArguments().getInt("garo");
            points=0;
            //Toast.makeText(getActivity(), "DATA "+badge, Toast.LENGTH_SHORT).show();
            if(badge==-1)
            {
                badge=level=hindi=english=garo=0;
            }
            switch (badge)
            {
                case 0:
                    survey1="&#127891; Learner";
                    break;
                case 1:
                    survey1="&#128483; Speaker";
                    break;
                case 2:
                    survey1="&#128214; Reader";
                    break;
                case 3:
                    survey1="&#9997; Writer";
                    break;
            }
            survey2="";
            if(english==1)
                survey2=survey2+" English ";
            if(hindi==1)
                survey2=survey2+" Hindi ";
            if(garo==1)
                survey2=survey2+" Garo ";
            if(level==1)
                points=10;
        }
        db=LearnKhasiDatabase.getDatabase(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_login, container, false);
        binding = FragmentLoginBinding.inflate(getLayoutInflater(),container,false);

        binding.signIn.setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //skip login
        binding.skipSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });

        binding.terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://learnkhasi.in/terms")));
            }
        });

        binding.privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://learnkhasi.in/privacy")));
            }
        });

        return binding.getRoot();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }
    private void updateUI(final FirebaseUser user) {
        if (user != null) {
            //Log.d("Khasi","LOGIN" +"");
            //Toast.makeText(getActivity(), "Logged In : "+user.getEmail()+" "+user.getUid(), Toast.LENGTH_SHORT).show();
            //Goto MainActivity
            //put Data in SharedPref
            pref = getActivity().getSharedPreferences("LearnKhasi", 0);
            editor = pref.edit();
            editor.putString("name",user.getDisplayName()+"");
            editor.putString("email",user.getEmail()+"");
            editor.putString("gid",user.getUid()+"");
            editor.putString("badge",survey1);
            editor.putInt("english",english);//knows english
            editor.putInt("hindi",hindi);//knows hindi
            editor.putInt("garo",garo);//knows garo
            editor.putInt("khasi",level);//knows khasi
            //Log.d("DATA  UID",user.getUid());
            Calendar cal2 = Calendar.getInstance();
            String joinedIn = "" + cal2.get(Calendar.DATE) + "/" + (cal2.get(Calendar.MONTH) + 1) + "/" + cal2.get(Calendar.YEAR);
            editor.putString("joined",joinedIn);
            if(user.getPhotoUrl()!=null)
                editor.putString("photo",user.getPhotoUrl().toString()+"");
            editor.commit();
            editor.apply();

            //Log.d("Khasi","LOGIN" +"NEW ");

            final User newUser = new User(user.getUid(), user.getDisplayName(), user.getEmail(), user.getPhotoUrl() + "", survey1 + "", survey2 + "", joinedIn, points, level, badge,english,hindi,garo,level);

            //Get Data from Firebase using Uid
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference();
            binding.skipSignin.setEnabled(false);
            binding.signIn.setEnabled(false);
            myRef.child("users/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Log.d("Khasi","LOGIN" +"");
                    if(snapshot.exists()){
                        User existing=snapshot.getValue(User.class);
                        editor.putString("badge",existing.getSurvey1()+"");
                        editor.putInt("english",existing.getEnglish());
                        editor.putInt("hindi",existing.getHindi());
                        editor.putInt("garo",existing.getGaro());
                        editor.putInt("khasi",existing.getKhasi());
                        editor.putInt("level",existing.getLevelId());
                        editor.putString("joined",existing.getJoinedIn());
                        //Log.d("Khasi",existing.getKhasi()+"-"+existing.getJoinedIn());
                        editor.commit();
                        editor.apply();
                        exist=true;
                        userLogin(existing);
                    }
                    else{
                        exist=false;
                        userLogin(newUser);
                    }
                    //Log.d("Khasi","LOGIN" +"");
                    hideProgressBar(true);
                    //Snackbar.make(binding.getRoot(),"Successfully Logged in",Snackbar.LENGTH_LONG).show();
                    Toast.makeText(getContext(), "Successfully Logged in", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //Log.d("Khasi","ERROR" +"");
                }
            });

            //save khasi_sentence_vote
            voteKhasiSentenceRef=database.getReference();
            voteKhasiSentenceRef.child("khasi_sentence_vote/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Boolean userVote = postSnapshot.getValue(Boolean.class);
                        //Log.e("DATA",userVote+" - "+postSnapshot.getKey()+" = ");
                        storeUserVoteData(postSnapshot.getKey(),"khasi_sentence",userVote);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //save english sentence vote
            voteEnglishSentenceRef=database.getReference();
            voteEnglishSentenceRef.child("english_sentence_vote/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Boolean userVote = postSnapshot.getValue(Boolean.class);
                        //Log.e("DATA",userVote+" - "+postSnapshot.getKey()+" = ");
                        storeUserVoteData(postSnapshot.getKey(),"english_sentence",userVote);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //save reported sentences
            reportedSentenceRef=database.getReference();
            reportedSentenceRef.child("sentence_reported/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        //Boolean userVote = postSnapshot.getValue(Boolean.class);
                        //Log.e("DATA",userVote+" - "+postSnapshot.getKey()+" = ");
                        storeUserReportedData(postSnapshot.getKey(),"sentence_reported",true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //save reported Words
            reportedSentenceRef.child("word_reported/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        //Boolean userVote = postSnapshot.getValue(Boolean.class);
                        //Log.e("DATA",userVote+" - "+postSnapshot.getKey()+" = ");
                        storeUserReportedWordData(postSnapshot.getKey(),"word_reported",true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //save user voted words
            votedKhasiWordRef=database.getReference();
            votedKhasiWordRef.child("khasi_word_vote/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Boolean userVote = postSnapshot.getValue(Boolean.class);
                        //Log.e("DATA",userVote+" - "+postSnapshot.getKey()+" = ");
                        storeUserVotedWord(postSnapshot.getKey(),"khasi_word",userVote);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            votedEngWordRef=database.getReference();
            votedEngWordRef.child("english_word_vote/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Boolean userVote = postSnapshot.getValue(Boolean.class);
                        //Log.e("DATA",userVote+" - "+postSnapshot.getKey()+" = ");
                        storeUserVotedWord(postSnapshot.getKey(),"english_word",userVote);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            votedGaroWordRef=database.getReference();
            votedGaroWordRef.child("garo_word_vote/"+user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Boolean userVote = postSnapshot.getValue(Boolean.class);
                        //Log.e("DATA",userVote+" - "+postSnapshot.getKey()+" = ");
                        storeUserVotedWord(postSnapshot.getKey(),"word_reported",userVote);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Editors
            editorsRef=database.getReference("status");
            editorsRef.child("editorList").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //UserSentenceVotes userSentenceVotes=new UserSentenceVotes(snapshot.getKey(),"khasi_sentence",);
                    //snapshot.getKey();
                    //Object value=snapshot.getValue(Object.class);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Editors editor = new Editors(postSnapshot.getKey());
//                        Log.e("DATA"," - "+postSnapshot.getKey()+" = "+editor.getUserId());
                        storeEditors(editor.getUserId());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            Toast.makeText(getActivity(), "User not Sign In", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeEditors(final String uId) {
        class storeEditors extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                EditorsDao editorsDao=db.editorsDao();
                Editors editors=new Editors(uId);
                editorsDao.insert(editors);
                return null;
            }

        }
        storeEditors storeEditors=new storeEditors();
        storeEditors.execute();
    }

    private void storeUserVotedWord(final String localWordId, final String fromLanguage, final Boolean userVote) {
        class storeUserVoteData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                UserWordVotesDao userWordVotesDao=db.userWordVotesDao();
                UserWordVoted userWordVoted=new UserWordVoted(localWordId,fromLanguage,userVote);
                userWordVotesDao.insert(userWordVoted);
                //Log.d("DATA","Stored Data - "+localWordId+"-"+likeData);
                return null;
            }

        }
        storeUserVoteData storeUserVoteData=new storeUserVoteData();
        storeUserVoteData.execute();
    }

    private void storeUserReportedWordData(final String wordId, final String word_reported, final boolean value) {
        class storeUserReportedData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                UserWordReportedDao userWordReportedDao=db.userWordReportedDao();
                UserWordReported userWordReported=new UserWordReported(wordId,word_reported,value);
                userWordReportedDao.insert(userWordReported);
                //insert vote in db
                //Log.d("DATA","Stored Data - "+sentenceId+"-"+value);
                return null;
            }
        }
        storeUserReportedData storeUserReportedData=new storeUserReportedData();
        storeUserReportedData.execute();
    }

    private void storeUserReportedData(final String sentenceId,final String substring,final boolean value) {
        class storeUserReportedData extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                UserSentenceReportedDao userSentenceReportedDao=db.userSentenceReportedDao();
                UserSentenceReported userSentenceReported=new UserSentenceReported(sentenceId,substring,value);
                userSentenceReportedDao.insert(userSentenceReported);
                //insert vote in db
                //Log.d("DATA","Stored Data - "+sentenceId+"-"+value);
                return null;
            }
        }
        storeUserReportedData storeUserReportedData=new storeUserReportedData();
        storeUserReportedData.execute();
    }

    private static void storeUserVoteData(final String sentenceId, final String fromLanguage, final boolean vote) {

        class storeUserVoteData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                UserSentenceVotesDao userSentenceVotesDao=db.userSentenceVotesDao();
                UserSentenceVotes userSentenceVotes=new UserSentenceVotes(sentenceId,fromLanguage,vote);
                userSentenceVotesDao.insert(userSentenceVotes);
                //insert vote in db
                //Log.d("DATA","Stored Data");
                return null;
            }
        }
        storeUserVoteData storeUserVoteData=new storeUserVoteData();
        storeUserVoteData.execute();
    }

    private void userLogin(User newUser){
        //Log.d("Khasi","IS EXIST "+exist);
        if(!exist) {
            //Store Data online only if user is new and there is no exiting data online
            myRef.child("users").child(newUser.getuId()).setValue(newUser);//child(user.getUid())
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.signIn) {
            signIn();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d("DATA", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("DATA", "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        // [START_EXCLUDE silent]
        showProgressBar();
        // [END_EXCLUDE]
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("DATA", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("DATA", "signInWithCredential:failure", task.getException());
                            Snackbar.make(binding.getRoot(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressBar(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void hideProgressBar(boolean openApp) {
        //Toast.makeText(getActivity(), "Hide Progress, Done", Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
        if(openApp)
        {
            try {
                startActivity(new Intent(activity, MainActivity.class)); //fixes #1
                getActivity().finish();
            }catch (NullPointerException e){}
        }
    }

    private void showProgressBar() {
        //Toast.makeText(getActivity(), "Show Progress, Please wait", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Signing In");
        builder.setMessage("Please wait...");
        builder.setCancelable(false);
        alertDialog=builder.create();
        alertDialog.show();

    }
}