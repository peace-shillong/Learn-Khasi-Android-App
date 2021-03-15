package com.sngur.learnkhasi.ui.notifications;

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
import com.sngur.learnkhasi.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //getActivity().setTitle(R.string.title_notifications);
        //View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        binding=FragmentNotificationsBinding.inflate(getLayoutInflater(),container,false);
        binding.textNotifications.setText(R.string.blank_fragment_2);
        return binding.getRoot();
    }
}