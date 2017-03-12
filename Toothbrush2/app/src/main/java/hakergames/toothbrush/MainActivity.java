package hakergames.toothbrush;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        final SharedPreferences userInfo = getSharedPreferences(MainScreen.USER_INFO, 0);
        if(userInfo.contains("username")){
            Intent intent = new Intent(context, MainScreen.class);
            startActivity(intent);
        }

        Button saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((EditText) findViewById(R.id.nameInput)).getText().toString();

                if(username.isEmpty()){
                    Toast.makeText(context, "Vardas negali būti tuščias", Toast.LENGTH_LONG).show();
                    return;
                }

                clearData();

                SharedPreferences.Editor editor = userInfo.edit();

                editor.putString("username", username);
                editor.putInt("exp", 0);
                editor.apply();

                Intent intent = new Intent(context, MainScreen.class);
                startActivity(intent);
            }
        });

        final Button clearButton = (Button)findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearData();
                Toast.makeText(context, "Duomenys ištrinti", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void clearData(){
        SharedPreferences userPref = getSharedPreferences(MainScreen.USER_INFO, 0);
        SharedPreferences.Editor userEditor = userPref.edit();
        userEditor.clear();
        userEditor.apply();

        SharedPreferences eventPref = getSharedPreferences(MainScreen.LAST_EVENT, 0);
        SharedPreferences.Editor eventEditor = eventPref.edit();
        eventEditor.clear();
        eventEditor.apply();

        SharedPreferences achievementPref = getSharedPreferences(MainScreen.UNLOCKED_ACHIEVEMENTS, 0);
        SharedPreferences.Editor achievementEditor = achievementPref.edit();
        achievementEditor.clear();
        achievementEditor.apply();
    }
}
