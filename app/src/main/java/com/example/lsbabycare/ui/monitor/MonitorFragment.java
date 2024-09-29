package com.example.lsbabycare.ui.monitor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lsbabycare.databinding.FragmentMonitorBinding;
import com.example.lsbabycare.ui.shared.SharedViewModel;

public class MonitorFragment extends Fragment {

    private FragmentMonitorBinding binding;
    private SharedViewModel sharedViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MonitorViewModel monitorViewModel =
                new ViewModelProvider(this).get(MonitorViewModel.class);

        binding = FragmentMonitorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final ConstraintLayout monitorLayout = binding.layoutPrincipal;
        monitorLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getDangerState().observe(getViewLifecycleOwner(), dangerState -> {
            if (dangerState != null && dangerState) {
                changeColor(true);
            } else {
                changeColor(false);
            }
        });
        return root;
    }

    public void changeColor(boolean dangerState) {
        final ConstraintLayout monitorLayout = binding.layoutPrincipal;
        if(dangerState) {
            monitorLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            changeText(true);
        } else {
            monitorLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            changeText(false);
        }
    }

    public void changeText(boolean alertTxt) {
        TextView secsText = binding.secsText;
        if(alertTxt) {
            secsText.setText("¡Alerta!");
        } else {
            secsText.setText("Monitorizando...");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}