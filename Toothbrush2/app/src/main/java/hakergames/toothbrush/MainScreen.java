package hakergames.toothbrush;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends AppCompatActivity {
    public static final String USER_INFO = "ToothBrush.UserInformation";
    private static final String UNLOCKED_ACHIEVEMENTS = "ToothBrush.UnlockedAchviements";

    private User user;
    private List<Achievement> Achievements;
    private BluetoothService btService;
    private Event event;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // bluetooth service setup
        Chronometer stopwatch = (Chronometer)findViewById(R.id.eventTimer);
        TextView noEvent = (TextView)findViewById(R.id.noEvent);
        btService = new BluetoothService(this, new EventCommand());
        Button bluetoothButton = (Button)findViewById(R.id.bluetooth);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PRESS", "Pressed Bluetooth button");
                btService.sendMessage("1");
            }
        });

        // setups user
        loadUser();
        updateUserInfo();

        Button addButton = (Button)findViewById(R.id.button2);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PRESS", "Pressed Test button");
                user.addExp(1);
                updateUserInfo();
                saveUser();
            }
        });

        // setup achievements
        SharedPreferences achievementsInfo = getSharedPreferences(UNLOCKED_ACHIEVEMENTS, 0);
        Achievements = new ArrayList<Achievement>();
        String[] Names = {
                "30 min",
                "Rytas ir vakaras",
                "7 dienos"
        };
        String[] Descriptions = {
                "Iš viso valeisi 30 min.",
                "Dantis švarūs ir ryte ir vakare.",
                "Dantys švarūs ir ryte ir vakare visą savaitę."
        };

        for(int i = 0; i < Names.length; i++){
            Achievement achievement = new Achievement(i, Names[i], Descriptions[i]);

            String key = Integer.toString(i);
            if(achievementsInfo.getBoolean(key, false)){
                achievement.unlock();
            }

            Achievements.add(achievement);
        }
    }

    //------------------- User
    private void updateUserInfo() {
        if(user != null) {
            TextView username = (TextView) findViewById(R.id.username);
            username.setText(user.getUsername());
            TextView level = (TextView) findViewById(R.id.level);
            level.setText(getString(R.string.level) + " " + user.getLevel());
            ProgressBar progress = (ProgressBar)findViewById(R.id.levelProgress);
            progress.setProgress(user.getProgress());
        }
    }

    private void loadUser() {
        Log.d("ENTER", "Enterring loadUser()");
        SharedPreferences userInfo = getSharedPreferences(USER_INFO, 0);

        String username = userInfo.getString("username", "Nėra vardo");
        int exp = userInfo.getInt("exp", 0);

        user = new User(username, exp);
    }

    private void saveUser() {
        Log.d("ENTER", "Enterring saveUser()");
        SharedPreferences userInfo = getSharedPreferences(USER_INFO, 0);

        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("username", user.getUsername());
        editor.putInt("exp", user.getExp());
        editor.apply();
    }

    private void clearUser() {
        Log.d("ENTER", "Entering clearUser()");
        SharedPreferences userInfo = getSharedPreferences(USER_INFO, 0);

        SharedPreferences.Editor editor = userInfo.edit();
        editor.clear();
        editor.apply();
    }
    //------------------- User

    //------------------- Events
    public class EventCommand implements Command{
        public void start(){
            if(event == null || !event.isInProgress()){
                Chronometer stopwatch = (Chronometer)findViewById(R.id.eventTimer);
                TextView noEvent = (TextView)findViewById(R.id.noEvent);

                event = new Event();
                stopwatch.setBase(SystemClock.elapsedRealtime());

                stopwatch.setVisibility(View.VISIBLE);
                noEvent.setVisibility(View.INVISIBLE);
                stopwatch.start();
            }
        }

        public void end(){
            if(event.isInProgress()){
                Chronometer stopwatch = (Chronometer)findViewById(R.id.eventTimer);
                TextView noEvent = (TextView)findViewById(R.id.noEvent);

                event.endEvent();

                stopwatch.setVisibility(View.INVISIBLE);
                noEvent.setVisibility(View.VISIBLE);
                stopwatch.stop();
            }
        }
    }
    //------------------- Events
}
