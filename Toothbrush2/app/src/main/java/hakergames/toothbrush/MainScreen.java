package hakergames.toothbrush;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainScreen extends AppCompatActivity {
    public static final String USER_INFO = "ToothBrush.UserInformation";

    private User user;
    private PopupWindow popup;
    private SharedPreferences userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        userInfo = getSharedPreferences(USER_INFO, 0);

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
    }

    private void updateUserInfo() {
        if(user != null) {
            TextView username = (TextView) findViewById(R.id.username);
            username.setText(user.getUsername());
            TextView level = (TextView) findViewById(R.id.level);
            level.setText("Lygis " + user.getLevel());
            ProgressBar progress = (ProgressBar)findViewById(R.id.levelProgress);
            progress.setProgress(user.getProgress());
        }
    }

    private void loadUser() {
        String username = userInfo.getString("username", "Nėra vardo");
        int exp = userInfo.getInt("exp", 0);

        user = new User(username, exp);
    }

    private void saveUser() {
        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("username", user.getUsername());
        editor.putInt("exp", user.getExp());
        editor.commit();
    }

    private void clearUser() {
        SharedPreferences.Editor editor = userInfo.edit();
        editor.clear();
        editor.commit();
    }
}
