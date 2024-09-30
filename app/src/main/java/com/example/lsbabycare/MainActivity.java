package com.example.lsbabycare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.example.lsbabycare.dao.UserDataDao;
import com.example.lsbabycare.database.AppDatabase;
import com.example.lsbabycare.models.UserData;
import com.example.lsbabycare.ui.settings.SettingsFragment;
import com.example.lsbabycare.ui.shared.SharedViewModel;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SETTINGS = 1;
    private ActivityResultLauncher<Intent> settingsActivityResultLauncher;

    private static final int REQUEST_MICROPHONE = 200;
    private MediaRecorder mediaRecorder;
    private Handler handler;
    private int soundThreshold = 66; // Umbral de sonido en dB
    private int checkInterval = 300; // Comprobar cada segundo
    private int durationThreshold = 1000; // Tiempo que debe mantenerse el ruido en ms
    private long soundStartTime = 0;
    private Vibrator vibrator;
    private boolean isRecording = false;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private AppBarConfiguration mAppBarConfiguration;
    private SharedViewModel sharedViewModel;
    private AppDatabase db;
    private UserDataDao userDataDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);
        obtainUserData();
        configureNavBarAndToggle();
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "user-database").build();
        userDataDao = db.userDataDao();
        // Cargar los datos del usuario
        new Thread(() -> {
            UserData userData = userDataDao.getUserData();
            if (userData != null) {
                runOnUiThread(() -> {
                    // Actualizar UI con los datos del usuario
                    soundThreshold = userData.getRange();
                    durationThreshold = userData.getSecsBeforeVibrate() * 1000;
                    sharedViewModel.setUserData(userData);
                });
            }
        }).start();

        //Para compartir el estado entre vistas.

        sharedViewModel.getUserData().observe(this, userData -> {
            if (userData != null) {
                soundThreshold = userData.getRange();
                durationThreshold = userData.getSecsBeforeVibrate() * 1000;
                Log.i("UserData", "Range: " + soundThreshold + ", Seconds: " + durationThreshold);
                restartMonitoring();
            }
        });

        handler = new Handler();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Pedir permiso para el micrófono si no lo tenemos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
        } else {
            startMonitoring();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == Activity.RESULT_OK && data != null) {
            UserData userData = (UserData) data.getSerializableExtra("userData");
            if (userData != null) {
                soundThreshold = userData.getRange();
                durationThreshold = userData.getSecsBeforeVibrate() * 1000;
                Log.i("UserData", "Range: " + soundThreshold + ", Seconds: " + durationThreshold);
                restartMonitoring();
            }
        }
    }

    public UserDataDao getUserDataDao() {
        return userDataDao;
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsFragment.class);
        settingsActivityResultLauncher.launch(intent);
    }

    private void restartMonitoring() {
        releaseMediaRecorder(); // Liberar el MediaRecorder antes de reiniciar
        soundStartTime = 0; // Reiniciar el tiempo de ruido
        startMonitoring();
    }


    private void configureNavBarAndToggle() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_monitor, R.id.nav_settings)
                .setDrawerLayout(drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void obtainUserData() {
        UserData userData = (UserData) getIntent().getSerializableExtra("userData");
        if(userData != null) {
            soundThreshold = userData.getRange();
            durationThreshold = userData.getSecsBeforeVibrate() * 1000;
            Log.i("UserData", "Range: " + soundThreshold + ", Seconds: " + durationThreshold);
        } else {
            Log.i("UserData", "No data received");
        }
    }

    private void startMonitoring() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // Cambiamos el archivo de salida a un archivo temporal
        String filePath = getExternalCacheDir().getAbsolutePath() + "/test.3gp";
        mediaRecorder.setOutputFile(filePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();  // Iniciar grabación
            isRecording = true; // Indicar que la grabación comenzó
            handler.postDelayed(monitorSoundLevel, checkInterval);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MediaRecorder", "prepare() failed");
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.e("MediaRecorder", "start() failed");
            releaseMediaRecorder(); // Liberar recursos si falla el inicio
        }
    }

    private Runnable monitorSoundLevel = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                int amplitude = mediaRecorder.getMaxAmplitude();

                // Verificar que la amplitud no sea 0
                if (amplitude > 0) {
                    double decibels = 20 * Math.log10(amplitude); // Quitar el valor de referencia

                    Log.d("SoundLevel", "dB: " + decibels);

                    if (decibels > soundThreshold) {
                        if (soundStartTime == 0) {
                            soundStartTime = System.currentTimeMillis(); // Empieza a contar el tiempo
                        } else if (System.currentTimeMillis() - soundStartTime >= durationThreshold) {
                            Log.i("SoundLevel", "Ruido constante detectado");
                            triggerVibration((int) decibels); // Vibrar si se ha superado el tiempo
                            sharedViewModel.setDangerState(true);
                        }
                    } else {
                        Log.i("SoundLevel", "Ha parado el ruido");
                        soundStartTime = 0; // Reiniciar si el nivel de ruido baja
                        sharedViewModel.setDangerState(false);
                    }
                } else {
                    Log.d("SoundLevel", "Amplitud insuficiente o silenciosa.");
                }

                handler.postDelayed(this, checkInterval); // Vuelve a comprobar en el siguiente intervalo
            }
        }
    };

    private void triggerVibration(int soundLevel) {
        // Intensidad de vibración en función del nivel de sonido
        long[] pattern = {0, 200, 100, 300}; // Patrón de vibración
        vibrator.vibrate(pattern, -1); // Vibrar
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                // Ignore exceptions if the MediaRecorder has already been stopped
            }
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording && mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        handler.removeCallbacks(monitorSoundLevel);
    }

    // Solicitar permiso al usuario
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MICROPHONE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMonitoring();
        } else {
            // El permiso fue denegado
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder(); // Liberar el MediaRecorder cuando la actividad se pausa
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaRecorder(); // Liberar el MediaRecorder cuando la actividad se detiene
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startMonitoring(); // Reiniciar el monitoreo si se tiene el permiso
        }
    }

}
