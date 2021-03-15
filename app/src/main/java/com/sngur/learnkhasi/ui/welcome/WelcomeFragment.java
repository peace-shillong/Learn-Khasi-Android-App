package com.sngur.learnkhasi.ui.welcome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sngur.learnkhasi.R;
import com.sngur.learnkhasi.databinding.FragmentWelcomeBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WelcomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentWelcomeBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String mParam1;
    private String mParam2;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WelcomeFragment.
     */

    public static WelcomeFragment newInstance(String param1, String param2) {
        WelcomeFragment fragment = new WelcomeFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_welcome, container, false);
        binding=FragmentWelcomeBinding.inflate(getLayoutInflater(),container,false);
        View view=binding.getRoot();
        binding.yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WelcomeFragment2 fragment2=new WelcomeFragment2();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.layout_for_fragment, fragment2).addToBackStack("Welcome Page: 2").commit();
            }
        });

        binding.noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginFragment fragment=new LoginFragment();
                Bundle bundle=new Bundle();
                bundle.putInt("badge",-1);
                bundle.putInt("level",-1);
                bundle.putInt("english",-1);
                bundle.putInt("hindi",-1);
                bundle.putInt("garo",-1);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.layout_for_fragment, fragment).addToBackStack("Welcome Page: Login").commit();
            }
        });
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        return view;
    }
}