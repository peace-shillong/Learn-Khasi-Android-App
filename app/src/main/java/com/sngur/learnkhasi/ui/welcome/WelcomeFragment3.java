package com.sngur.learnkhasi.ui.welcome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.FragmentWelcome3Binding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WelcomeFragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeFragment3 extends Fragment {

    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "badge";
    private static final String ARG_PARAM2 = "level";

    // Rename and change types of parameters
    private int mParam1;
    private int mParam2;
    private FragmentWelcome3Binding binding;
    private int english, hindi, garo;

    public WelcomeFragment3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WelcomeFragment3.
     */
    // Rename and change types and number of parameters
    public static WelcomeFragment3 newInstance(String param1, String param2) {
        WelcomeFragment3 fragment = new WelcomeFragment3();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getInt(ARG_PARAM2);
            //Toast.makeText(getActivity(), "Badge "+mParam1, Toast.LENGTH_SHORT).show();
        }
        english=hindi=garo=0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_welcome3, container, false);
        binding=FragmentWelcome3Binding.inflate(getLayoutInflater(),container,false);

        binding.english.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    binding.english.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else{
                    binding.english.setTextColor(getResources().getColor(R.color.colorBlack));
                }

            }
        });

        binding.hindi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    binding.hindi.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else{
                    binding.hindi.setTextColor(getResources().getColor(R.color.colorBlack));
                }

            }
        });

        binding.garo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    binding.garo.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else{
                    binding.garo.setTextColor(getResources().getColor(R.color.colorBlack));
                }

            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.english.isChecked())
                    english=1;
                else
                    english=0;
                if(binding.hindi.isChecked())
                    hindi=1;
                else
                    hindi=0;
                if(binding.garo.isChecked())
                    garo=1;
                else
                    garo=0;
                //Toast.makeText(getContext(), "Selected "+english+hindi+garo, Toast.LENGTH_SHORT).show();
                LoginFragment fragment=new LoginFragment();
                Bundle bundle=new Bundle();
                bundle.putInt("badge",mParam1);
                bundle.putInt("level",mParam2);
                bundle.putInt("english",english);
                bundle.putInt("hindi",hindi);
                bundle.putInt("garo",garo);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.layout_for_fragment, fragment).addToBackStack("Welcome Page: Login").commit();
            }
        });
        return binding.getRoot();
    }
}