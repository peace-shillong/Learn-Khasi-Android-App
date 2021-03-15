package com.sngur.learnkhasi.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.FragmentProfileBinding;
import com.sngur.learnkhasi.model.User;
import com.sngur.learnkhasi.ui.activity.SettingsActivity;
import com.sngur.learnkhasi.ui.activity.UserSentenceListActivity;
import com.sngur.learnkhasi.ui.activity.UserWordsListActivity;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentProfileBinding binding;
    private String gid,name,email,badge,photo,knows;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int khasi,english,hindi,garo,level,points;
    private DatabaseReference myRef,pointsRef;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    //Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name="";
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        preferences =getActivity().getSharedPreferences("LearnKhasi", 0);
        gid=preferences.getString("gid","none");
        //Log.d("DATA GID",""+gid);
        name=preferences.getString("name","none");
        email=preferences.getString("email","none");
        badge=preferences.getString("badge","none");
        photo=preferences.getString("photo","none");
        khasi=preferences.getInt("khasi",0);
        english=preferences.getInt("english",0);
        hindi=preferences.getInt("hindi",0);
        garo=preferences.getInt("garo",0);
        knows="";
        if(english==1)
            knows=knows+""+" English ";
        if(khasi==1)
            knows=knows+""+" Khasi ";
        if(hindi==1)
            knows=knows+""+" Hindi ";
        if(garo==1)
            knows=knows+""+" Garo ";
        editor=preferences.edit();
        level=0;
        points=0;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getActivity().setTitle(R.string.title_profile);
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_profile, container, false);
        binding=FragmentProfileBinding.inflate(getLayoutInflater(),container,false);
        if(!gid.equals("none")) {
            binding.singedIn.setVisibility(View.VISIBLE);
            binding.name.setText(name);
            binding.email.setText(email);
            Picasso.get().load(photo).placeholder(R.color.colorPrimary).resize(600,200).centerCrop().into(binding.photo);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.badge.setText(Html.fromHtml("Badge: "+badge , Html.FROM_HTML_MODE_LEGACY));
            }
            else{
                binding.badge.setText(Html.fromHtml("Badge: "+badge));
            }
            binding.level.setText(binding.level.getText()+" "+knows);
            binding.signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("Sign Out");
                    builder.setMessage("Do you want to sign out?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseAuth.getInstance().signOut();
                            editor.clear();
                            editor.commit();
                            editor.apply();
                            binding.singedOut.setVisibility(View.VISIBLE);
                            binding.singedIn.setVisibility(View.GONE);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                }
            });

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference();
            //Get User Points and Level from online
            myRef.child("users").child(gid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Log.e("DATA","DATA");
                    if(snapshot.exists()) {
                        User userData = snapshot.getValue(User.class);
                        binding.userLevel.setText("Level "+userData.getLevelId());
                        level=userData.getLevelId();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //Points
            pointsRef=database.getReference();
            pointsRef.child("user_points").child(gid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Log.e("DATA","DATA");
                    if (snapshot.exists()) {
                        Long tempPoints = (Long) snapshot.child("points").getValue();
                        binding.userPoints.setText(tempPoints + " Points");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            points = Math.toIntExact(tempPoints);
                        }
                        checkUpgrade();
                        //Snackbar.make(binding.getRoot(), "Unlock this feature by becoming a sponsor", Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //binding for Words, Sentences and all
            binding.pointsInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Show AlertDialog
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("Points");
                    builder.setMessage("Points are earned when you add a new word/sentence or when a user up vote your word/sentence. You earn +10 Points when a user up votes your word/sentence and you get -5 points when a user down vote or report your word/sentence");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Learn more", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //openLink
                            openLink("https://github.com/sngur/Learn-Khasi-App");
                        }
                    });
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                }
            });

            binding.levelInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("Level");
                    builder.setMessage("Levels are upgraded up when you earn points. The higher the level the more features you unlock.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Learn more", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //openLink
                            openLink("https://github.com/sngur/Learn-Khasi-App");
                        }
                    });
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                }
            });

            //Load user sentences
            binding.userSentences.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   //show activity with user sentences in Tabs(Lang)
                    Intent intent=new Intent(getActivity(), UserSentenceListActivity.class);
                    intent.putExtra("userId",gid);
                    intent.putExtra("type","all");
                    getActivity().startActivity(intent);
                }
            });

            //user reported sentences
            binding.infoUserReportedSentences.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("Reported Sentences");
                    builder.setMessage("These are sentences that have been reported by users and which you have to edit and submit an update translation for correction.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Learn more", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //openLink
                            openLink("https://github.com/sngur/Learn-Khasi-App");
                        }
                    });
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                }
            });

            binding.userReportedSentences.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getActivity(), UserSentenceListActivity.class);
                    intent.putExtra("userId",gid);
                    intent.putExtra("type","reported");
                    getActivity().startActivity(intent);
                }
            });

            //words added by User
            binding.userWords.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //show activity with user sentences in Tabs(Lang)
                    Intent intent=new Intent(getActivity(), UserWordsListActivity.class);
                    intent.putExtra("userId",gid);
                    intent.putExtra("type","all");
                    getActivity().startActivity(intent);
                }
            });

            //words reported to user
            binding.userReportedWords.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getActivity(), UserWordsListActivity.class);
                    intent.putExtra("userId",gid);
                    intent.putExtra("type","reported");
                    getActivity().startActivity(intent);
                }
            });

            binding.infoUserReportedWords.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("Reported Words");
                    builder.setMessage("These are words that have been reported by users and which you have to edit and submit an update translation for correction.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Learn more", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //openLink
                            openLink("https://github.com/sngur/Learn-Khasi-App");
                        }
                    });
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                }
            });
            //open settings
            binding.settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
            });
        }
        else{
            //show Login Button
            binding.singedOut.setVisibility(View.VISIBLE);
            binding.singedIn.setVisibility(View.GONE);
        }
        binding.sngurLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink("http://sngur.com/");
            }
        });
        binding.fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink("https://facebook.com/sngur.shillong");
            }
        });
        binding.ig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink("https://instagram.com/sngur_shillong");
            }
        });
        binding.git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink("https://github.com/sngur");
            }
        });
        return binding.getRoot();
    }

    private void checkUpgrade() {
        //Log.e("DATA","Points");
        if(level==1 & points>=750)
        {
            binding.upgradeLevel.setVisibility(View.VISIBLE);
            //level 2
            binding.upgradeLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(binding.getRoot(), "Unlock this feature by becoming a sponsor", Snackbar.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), "Unlock this feature by becoming a sponsor", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(level>1 && points >12500)
        {
            //level 3
            binding.upgradeLevel.setVisibility(View.VISIBLE);
            binding.upgradeLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(binding.getRoot(), "Unlock this feature by becoming a sponsor", Snackbar.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), "Unlock this feature by becoming a sponsor", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openLink(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}