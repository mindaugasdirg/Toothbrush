package hakergames.toothbrush;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hakergames.toothbrush.Models.Achievement;
import hakergames.toothbrush.Models.Command;
import hakergames.toothbrush.Models.Event;
import hakergames.toothbrush.Models.User;

public class MainScreen extends AppCompatActivity {
    public static final String USER_INFO = "ToothBrush.UserInformation";
    public static final String UNLOCKED_ACHIEVEMENTS = "ToothBrush.UnlockedAchviements";
    public static final String LAST_EVENT = "ToothBrush.LastEvent";

    private User user;
    private List<Achievement> Achievements;
    private BluetoothService btService;
    private Event event;
    EventCommand command;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // bluetooth service setup
        btService = new BluetoothService(this, new EventCommand());
        Chronometer stopwatch = (Chronometer)findViewById(R.id.eventTimer);
        stopwatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(!event.isInProgress()){
                    Chronometer stopwatch = (Chronometer)findViewById(R.id.eventTimer);
                    LinearLayout stopwatchContainer = (LinearLayout)findViewById(R.id.stopwatchContainer);
                    TextView noEvent = (TextView)findViewById(R.id.noEvent);

                    user.setLastDate(event.getEventDate());
                    user.setLastTime(event.getDeltaTime());
                    saveEvent();
                    checkAchievements();
                    updateUserInfo();
                    saveUser();

                    stopwatchContainer.setVisibility(View.INVISIBLE);
                    noEvent.setVisibility(View.VISIBLE);
                    stopwatch.stop();
                }
            }
        });

        // setups user
        loadUser();
        updateUserInfo();

        // setup achievements
        Achievements = Achievement.loadAchievements(this, user);

        GridView achievementGrid = (GridView)findViewById(R.id.achievementContainer);
        achievementGrid.setNumColumns(3);
        achievementGrid.setAdapter(new AchievementAdapter(this, Achievements));

        setupStats();
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

            if(event != null && !event.isInProgress()) {
                int times = user.getTimes();
                times++;
                user.setTimes(times);
            }
        }

        setupStats();
        saveUser();
    }

    private void loadUser() {
        SharedPreferences userInfo = getSharedPreferences(USER_INFO, 0);

        String username = userInfo.getString("username", "No name");
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
    }

    public void loadEvent(){
        SharedPreferences eventPref = getSharedPreferences(LAST_EVENT, 0);
        Date date = null;
        long time = 0;

        if(eventPref.contains("date")){
            String dateString = eventPref.getString("date", "");

            if(!dateString.isEmpty()){
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                try {
                    date = dateFormat.parse(dateString);
                }catch (Exception e) { }
            }
        }
        if(eventPref.contains("time")){
            time = eventPref.getLong("time", 0);
        }

        event = new Event(date, time);
    }

    public class EventCommand implements Command {
        public void start(){
            Log.d("SB_HACKERGAMES", "MainScreen - starting event");
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
            Log.d("SB_HACKERGAMES", "MainScreen - ending event");
            if(event.isInProgress() || event == null){
                event.endEvent();
            }
        }
    }
    //------------------- Events

    //------------------- Achievements
    public void checkAchievements(){
        for(int i = 0; i < Achievements.size(); i++){
            int xp = Achievements.get(i).check();
            Log.d("XP", Integer.toString(xp));
            user.addExp(xp);
            Log.d("UNLOCKED", Boolean.toString(Achievements.get(i).isUnlocked()));
        }

        saveAchievements();

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

    public void setupStats(){
        TextView times = (TextView)findViewById(R.id.time);
        times.setText(Integer.toString(user.getTimes()));

        TextView totalTimeText = (TextView)findViewById(R.id.totalTime);
        long totalTime = user.getTotalTime();
        long totalTimeH = totalTime / (60 * 60 * 1000);
        long totalTimeM = (totalTime - totalTimeH * 3600 * 1000) / (60 * 1000);
        long totalTimeS = (totalTime - totalTimeH * 3600 * 1000 - totalTimeM * 60000) / 1000;
        totalTimeText.setText(String.format("%1$02d:%2$02d:%3$02d", totalTimeH, totalTimeM, totalTimeS));

        TextView lastDate = (TextView)findViewById(R.id.lastDate);
        Date date = user.getLastDate();
        if(date == null){
            lastDate.setText("No information");
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");
            String formattedDate = dateFormat.format(calendar.getTime());
            lastDate.setText(formattedDate);
        }

        TextView lastTimeText = (TextView)findViewById(R.id.lastTime);
        long lastTime = user.getTotalTime();
        long lastTimeH = lastTime / (60 * 60 * 1000);
        long lastTimeM = (lastTime - lastTimeH * 3600 * 1000) / (60 * 1000);
        long lastTimeS = (lastTime - lastTimeH * 3600 * 1000 - lastTimeM * 60000) / 1000;
        String hours = String.format("%1$02d", lastTimeH);
        String minutes = String.format("%1$02d", lastTimeM);
        String seconds = String.format("%1$02d", lastTimeS);
        lastTimeText.setText(hours + ":" + minutes + ":" + seconds);
    }
}
