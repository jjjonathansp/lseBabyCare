package com.example.lsbabycare.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.lsbabycare.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lsbabycare.MainActivity;
import com.example.lsbabycare.dao.UserDataDao;
import com.example.lsbabycare.databinding.FragmentSettingsBinding;
import com.example.lsbabycare.models.UserData;
import com.example.lsbabycare.ui.shared.SharedViewModel;

public class SettingsFragment extends Fragment {

    private SharedViewModel sharedViewModel;

    private FragmentSettingsBinding binding;

    private UserDataDao userDataDao;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        userDataDao = ((MainActivity) requireActivity()).getUserDataDao();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final SeekBar soundRangeBar = binding.soundRangeBar;
        final TextView soundRangeValue = binding.soundRangeValue;
        if(sharedViewModel.getUserData()!=null && sharedViewModel.getUserData().getValue()!=null) {
            if(sharedViewModel.getUserData().getValue().getRange()>0) {
                soundRangeBar.setProgress(sharedViewModel.getUserData().getValue().getRange());
            }
            if (sharedViewModel.getUserData().getValue().getSecsBeforeVibrate()>0) {
                EditText secsNumberField = binding.secsNumberField;
                secsNumberField.setText(String.valueOf(sharedViewModel.getUserData().getValue().getSecsBeforeVibrate()));
                binding.soundRangeValue.setText(String.valueOf(sharedViewModel.getUserData().getValue().getRange()));
            }
        }
        soundRangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                soundRangeValue.setText(String.valueOf(progress));
                checkFieldsForValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed
            }
        });

        final TextView textView = binding.textSettings;
        settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        final EditText secsNumberField = binding.secsNumberField;
        // Añadir TextWatcher al EditText
        secsNumberField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForValues();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
        final Button saveBtn = binding.saveBtn;
        saveBtn.setEnabled(false);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData(v);
            }
        });

        return root;

    }

    private void checkFieldsForValues() {
        final SeekBar soundRangeBar = binding.soundRangeBar;
        final EditText secsNumberField = binding.secsNumberField;
        int soundRange = soundRangeBar.getProgress();
        String secsText = secsNumberField.getText().toString();
        int secsNumber = secsText.isEmpty() ? 0 : Integer.parseInt(secsText);
        final Button saveBtn = binding.saveBtn;
        saveBtn.setEnabled(soundRange > 0 && secsNumber > 0);
    }

    public void saveData(View view) {
        // Do something in response to button click
        SeekBar secsRangeBar = binding.soundRangeBar;
        EditText secsBeforeVibrateTxt = binding.secsNumberField;
        UserData userData = new UserData();
        userData.setRange(secsRangeBar.getProgress());
        userData.setSecsBeforeVibrate(Integer.parseInt(secsBeforeVibrateTxt.getText().toString()));
        saveUserData(userData);
        sharedViewModel.setUserData(userData);


        // Redirigir a MonitorFragment
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.nav_monitor);
    }

    private void saveUserData(UserData userData) {
        new Thread(() -> {
            try{
                userDataDao.insert(userData);
            } catch (Exception e) {
                userDataDao.update(userData);
            }

        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}