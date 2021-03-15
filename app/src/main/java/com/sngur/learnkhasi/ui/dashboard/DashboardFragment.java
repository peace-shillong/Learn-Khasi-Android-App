package com.sngur.learnkhasi.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.FragmentDashboardBinding;
import com.sngur.learnkhasi.ui.activity.LessonLettersActivity;
import com.sngur.learnkhasi.ui.activity.LessonSentencesActivity;
import com.sngur.learnkhasi.ui.activity.LessonWordsActivity;

//Lessons
public class DashboardFragment extends Fragment {

    FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        binding=FragmentDashboardBinding.inflate(getLayoutInflater(),container,false);
//        binding.textDashboard.setText(R.string.hello_blank_fragment);
        //Letters Lessons
        binding.viewLettersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), LessonLettersActivity.class);
                intent.putExtra("lesson","viewLetters");
                startActivity(intent);
            }
        });

        binding.listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), LessonLettersActivity.class);
                intent.putExtra("lesson","listenLetters");
                startActivity(intent);
            }
        });
        //Words Lessons
        binding.view3LettersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), LessonWordsActivity.class);
                intent.putExtra("lesson","3");
                startActivity(intent);
            }
        });
        binding.view4LettersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), LessonWordsActivity.class);
                intent.putExtra("lesson","4");
                startActivity(intent);
            }
        });
        //Sentences Lessons
        binding.conversations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), LessonSentencesActivity.class);
                intent.putExtra("lesson","Conversations");
                startActivity(intent);
            }
        });

        binding.greetings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), LessonSentencesActivity.class);
                intent.putExtra("lesson","Greetings");
                startActivity(intent);
            }
        });

        binding.directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), LessonSentencesActivity.class);
                intent.putExtra("lesson","Directions and Places");
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }
}