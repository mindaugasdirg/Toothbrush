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
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static String EXTRA_DEVICE_ADDRESS = "SB";

    private String mConnectedDeviceName;
    private BluetoothAdapter adapter;
    private BluetoothService bluetoothService;
    private StringBuffer mOutStringBuffer;

    public static final String USER_INFO = "ToothBrush.UserInformation";
    public static final String UNLCOKED_ACHIEVMENTS = "ToothBrush.UnlockedAchviements";
    public static UUID MY_UUID = UUID.fromString("446118f0-8b1e-11f3-9e96-0800200c9a66");
    public static String DEVICE_NAME = "SB";

    private User user;
    private List<Achievment> Achievments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

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
                sendMessage("1254");
            }
        });

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth yra negalimas", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!adapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }else{
            bluetoothService = new BluetoothService(this, mHandler);
            mOutStringBuffer = new StringBuffer("");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null){
            bluetoothService.stop();
        }
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == MainScreen.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(MainScreen.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = adapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    bluetoothService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == MainScreen.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    bluetoothService = new BluetoothService(this, mHandler);
                    mOutStringBuffer = new StringBuffer("");
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Bluetooth yra išjungtas", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_LISTEN:
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d("Send", writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d("GOT", readMessage);
                    MainScreen.this.sendMessage("1254");
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void sendMessage(String message){
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "Neprisijungęs prie įrenginio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService.write(send);
        }
    }
}
