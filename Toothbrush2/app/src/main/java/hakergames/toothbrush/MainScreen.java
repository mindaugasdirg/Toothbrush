package hakergames.toothbrush;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainScreen extends AppCompatActivity {
    public static final String USER_INFO = "ToothBrush.UserInformation";
    public static final String UNLCOKED_ACHIEVMENTS = "ToothBrush.UnlockedAchviements";

    private User user;
    private List<Achievment> Achievments;
    private BluetoothService btService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        btService = new BluetoothService(this);

        SharedPreferences achievmentsInfo = getSharedPreferences(UNLCOKED_ACHIEVMENTS, 0);
        Achievments = new ArrayList<Achievment>();
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
            Achievment achievment = new Achievment(i, Names[i], Descriptions[i]);

            String key = Integer.toString(i);
            if(achievmentsInfo.getBoolean(key, false)){
                achievment.unlock();
            }

            Achievments.add(achievment);
        }

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

        Button bluetoothButton = (Button)findViewById(R.id.bluetooth);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PRESS", "Pressed Bluetooth button");
                btService.sendMessage("1");
            }
        });
    }

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
        editor.commit();
    }

    private void clearUser() {
        Log.d("ENTER", "Enterring clearUser()");
        SharedPreferences userInfo = getSharedPreferences(USER_INFO, 0);

        SharedPreferences.Editor editor = userInfo.edit();
        editor.clear();
        editor.commit();
    }
}
