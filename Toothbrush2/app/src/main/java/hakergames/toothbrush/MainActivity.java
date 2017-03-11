package hakergames.toothbrush;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends FragmentActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences userInfo = getSharedPreferences(MainScreen.USER_INFO, 0);
        context = getApplicationContext();

        if(userInfo.contains("username")){
            Intent intent = new Intent(context, MainScreen.class);
            startActivity(intent);
        }

        Button saveButton = (Button)findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((EditText) findViewById(R.id.nameInput)).getText().toString();

                SharedPreferences.Editor editor = userInfo.edit();
                editor.putString("username", username);
                editor.putInt("exp", 0);
                editor.commit();

                Intent intent = new Intent(context, MainScreen.class);
                startActivity(intent);
            }
        });
    }
}
