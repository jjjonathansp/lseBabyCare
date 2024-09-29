package com.example.lsbabycare.ui.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lsbabycare.models.UserData;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> dangerState = new MutableLiveData<>();
    private final MutableLiveData<UserData> userData = new MutableLiveData<>();

    public void setUserData(UserData data) {
        userData.setValue(data);
    }

    public LiveData<UserData> getUserData() {
        return userData;
    }
    public void setDangerState(boolean state) {
        dangerState.setValue(state);
    }

    public LiveData<Boolean> getDangerState() {
        return dangerState;
    }
}