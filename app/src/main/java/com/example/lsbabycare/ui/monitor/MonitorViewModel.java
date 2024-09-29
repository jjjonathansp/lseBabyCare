package com.example.lsbabycare.ui.monitor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MonitorViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MonitorViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Vibrar a partir de (dB):");
    }

    public LiveData<String> getText() {
        return mText;
    }
}