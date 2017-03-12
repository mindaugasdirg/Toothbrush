package hakergames.toothbrush;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainScreen extends AppCompatActivity {
    public static final String USER_INFO = "ToothBrush.UserInformation";
    private static final String UNLOCKED_ACHIEVEMENTS = "ToothBrush.UnlockedAchviements";
    private static final String LAST_EVENT = "ToothBrush.LastEvent";

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

        // setups user
        loadUser();
        updateUserInfo();

        Button addButton = (Button)findViewById(R.id.button2);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.addExp(1);
                updateUserInfo();
                saveUser();
            }
        });

        // setup achievements
        loadAchievements();

        GridView achievementGrid = (GridView)findViewById(R.id.achievementContainer);
        achievementGrid.setNumColumns(3);
        achievementGrid.setAdapter(new AchievementAdapter(this, Achievements));
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
            user.setTimes(user.getTimes() + 1);
            if(event != null) {
                user.setLastDate(event.getEventDate());
                user.setLastTime(event.getDeltaTime());
            }
        }

        saveUser();
    }

    private void loadUser() {
        SharedPreferences userInfo = getSharedPreferences(USER_INFO, 0);

        String username = userInfo.getString("username", "Nėra vardo");
        int exp = userInfo.getInt("exp", 0);
        long totalTime = userInfo.getLong("total", 0);
        String dateString = userInfo.getString("date", "");
        Date date = null;

        if(!dateString.isEmpty()){
            SimpleDateFormat dateFormat = new SimpleDateFormat();
            try {
                date = dateFormat.parse(dateString);
            }catch (Exception e) { }
        }

        user = new User(username, exp);
        user.addTotalTime(totalTime);
        if(date != null) {
            user.setLastDate(date);
        }
    }

    private void saveUser() {
        SharedPreferences userInfo = getSharedPreferences(USER_INFO, 0);

        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("username", user.getUsername());
        editor.putInt("exp", user.getExp());
        editor.putLong("total", user.getTotalTime());
        if(user.getLastDate() != null) {
            editor.putString("date", user.getLastDate().toString());
        }

        editor.putInt("times", user.getTimes());
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
    public void saveEvent(){
        SharedPreferences eventPref = getSharedPreferences(LAST_EVENT, 0);

        SharedPreferences.Editor editor = eventPref.edit();
        editor.putString("date", event.getEventDate().toString());
        editor.putLong("time", event.getDeltaTime());
        editor.apply();

        user.addTotalTime(event.getDeltaTime());
        if(event.getEventDate() != null) {
            user.setLastDate(event.getEventDate());
        }

        updateUserInfo();
    }

    public class EventCommand implements Command{
        public void start(){
            if(event == null || !event.isInProgress()){
                Chronometer stopwatch = (Chronometer)findViewById(R.id.eventTimer);
                LinearLayout stopwatchContainer = (LinearLayout)findViewById(R.id.stopwatchContainer);
                TextView noEvent = (TextView)findViewById(R.id.noEvent);

                event = new Event();
                stopwatch.setBase(SystemClock.elapsedRealtime());

                stopwatchContainer.setVisibility(View.VISIBLE);
                noEvent.setVisibility(View.INVISIBLE);
                stopwatch.start();
            }
        }

        public void end(){
            if(event.isInProgress()){
                Chronometer stopwatch = (Chronometer)findViewById(R.id.eventTimer);
                LinearLayout stopwatchContainer = (LinearLayout)findViewById(R.id.stopwatchContainer);
                TextView noEvent = (TextView)findViewById(R.id.noEvent);

                event.endEvent();
                saveEvent();
                updateUserInfo();
                checkAchievements();

                stopwatchContainer.setVisibility(View.INVISIBLE);
                noEvent.setVisibility(View.VISIBLE);
                stopwatch.stop();
            }
        }
    }
    //------------------- Events

    //------------------- Achievements
    private void loadAchievements() {
        SharedPreferences achievementsInfo = getSharedPreferences(UNLOCKED_ACHIEVEMENTS, 0);
        Achievements = new ArrayList<Achievement>();
        String[] Names = {
                "30 min",
                "2 kartai",
                "2 min"
        };
        String[] Descriptions = {
                "Iš viso valeisi 30 min.",
                "Dantys išvalyti 2 kartus.",
                "Dantys buvo valomi 2 min nesustojus."
        };
        int[] Exps = {
                10, 10, 10
        };
        Achievement.Requirement[] Requirements = {
                new Achievement.Requirement() {
                    @Override
                    public boolean check() {
                        Log.d("CMP", Float.toString(user.getTotalTime() / 6000f));
                        Log.d("ACHV", Boolean.toString((user.getTotalTime() / 6000f) >= 30));
                        return (user.getTotalTime() / 6000f) >= 30;
                    }
                },
                new Achievement.Requirement() {
                    @Override
                    public boolean check() {
                        Log.d("CMP", Integer.toString(user.getTimes()));
                        Log.d("ACHV", Boolean.toString(user.getTimes() >= 2));
                        return user.getTimes() >= 2;
                    }
                },
                new Achievement.Requirement() {
                    @Override
                    public boolean check() {
                        Log.d("CMP", Float.toString(user.getLastTime() / 6000f));
                        Log.d("ACHV", Boolean.toString((user.getLastTime() / 6000f) >= 2));
                        return (user.getLastTime() / 6000f) >= 2;
                    }
                }
        };

        for(int i = 0; i < Names.length; i++){
            Achievement achievement = new Achievement(i, Names[i], Descriptions[i], Exps[i], Requirements[i]);

            String key = Integer.toString(i);
            if(achievementsInfo.getBoolean(key, false)){
                achievement.unlockNoXp();
            }

            Achievements.add(achievement);
        }
    }

    public void checkAchievements(){
        for(int i = 0; i < Achievements.size(); i++){
            user.addExp(Achievements.get(i).check());
            Log.d("UNLOCKED", Boolean.toString(Achievements.get(i).isUnlocked()));
        }

        GridView achievementGrid = (GridView)findViewById(R.id.achievementContainer);
        achievementGrid.setNumColumns(3);
        achievementGrid.setAdapter(new AchievementAdapter(this, Achievements));
    }

    public void saveAchievements(){
        SharedPreferences achievementsInfo = getSharedPreferences(UNLOCKED_ACHIEVEMENTS, 0);
        SharedPreferences.Editor editor = achievementsInfo.edit();

        for(int i = 0; i < Achievements.size(); i++){
            editor.putBoolean(Integer.toString(i), Achievements.get(i).isUnlocked());
        }

        editor.apply();
    }
    //------------------- Achievements
}
