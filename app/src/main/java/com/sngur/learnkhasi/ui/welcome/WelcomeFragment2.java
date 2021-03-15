package com.sngur.learnkhasi.ui.welcome;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.FragmentWelcome2Binding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WelcomeFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeFragment2 extends Fragment {

    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BADGE = "badge";

    // Rename and change types of parameters
    private int badge=0,level=1;
    private FragmentWelcome2Binding binding;

    public WelcomeFragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WelcomeFragment2.
     */
    // Rename and change types and number of parameters
    public static WelcomeFragment2 newInstance(String param1, String param2) {
        WelcomeFragment2 fragment = new WelcomeFragment2();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_welcome2, container, false);
        binding=FragmentWelcome2Binding.inflate(getLayoutInflater(),container,false);
        binding.nextBtn.setEnabled(false);
        //binding.nextBtn.setBackgroundColor(getResources().getColor(R.color.colorGray));
        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding.nextBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }

                binding.nextBtn.setEnabled(true);
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.learner:
                        //Toast.makeText(getContext(), "Learner", Toast.LENGTH_SHORT).show();
                        binding.learner.setTextColor(getResources().getColor(R.color.colorWhite));
                        binding.speaker.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.reader.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.writer.setTextColor(getResources().getColor(R.color.colorBlack));
                        badge=0;
                        level=1;
                        break;
                    case R.id.speaker:
                        //Toast.makeText(getContext(), "Speaker", Toast.LENGTH_SHORT).show();
                        binding.learner.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.speaker.setTextColor(getResources().getColor(R.color.colorWhite));
                        binding.reader.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.writer.setTextColor(getResources().getColor(R.color.colorBlack));
                        badge=1;
                        level=1;
                        break;
                    case R.id.reader:
                        //Toast.makeText(getContext(), "Reader", Toast.LENGTH_SHORT).show();
                        binding.learner.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.speaker.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.reader.setTextColor(getResources().getColor(R.color.colorWhite));
                        binding.writer.setTextColor(getResources().getColor(R.color.colorBlack));
                        badge=2;
                        level=1;
                        break;
                    case R.id.writer:
                        //Toast.makeText(getContext(), "Writer", Toast.LENGTH_SHORT).show();
                        binding.learner.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.speaker.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.reader.setTextColor(getResources().getColor(R.color.colorBlack));
                        binding.writer.setTextColor(getResources().getColor(R.color.colorWhite));
                        badge=3;
                        level=1;
                        break;
                }
            }
        });
        //enable only when option is selected
        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WelcomeFragment3 fragment=new WelcomeFragment3();
                Bundle bundle=new Bundle();
                bundle.putInt("badge",badge);
                bundle.putInt("level",level);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.layout_for_fragment, fragment).addToBackStack("Welcome Page: 3").commit();
            }
        });
        return binding.getRoot();
    }
}