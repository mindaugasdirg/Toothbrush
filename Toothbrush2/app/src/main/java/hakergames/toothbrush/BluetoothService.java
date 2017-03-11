package hakergames.toothbrush;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private BluetoothAdapter adapterBT;
    private Set<BluetoothDevice> pairedDevices;
    private Handler handler;
    private ConnectedThread connectedThread;
    private BluetoothDevice device;
    private BluetoothSocket socketBT = null;

    private Context context;

    public BluetoothService(final Context context, final Command eventCommand){
        this.context = context;

        adapterBT = BluetoothAdapter.getDefaultAdapter();

        handler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    byte[] bytes = (byte[]) msg.obj;

                    eventCommand.start();
                    if(bytes[0] == 48 && bytes[1] == 48){
                        eventCommand.end();
                    }

                    //Log.d("READ_BT", readMessage);
                    Log.d("READ_BT", Character.toString((char)bytes[0]));
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        Toast.makeText(context, "Prisijungė prie: " + (String)(msg.obj), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(context, "Nepavyko prisijungti", Toast.LENGTH_LONG).show();;
                }
            }
        };

        if (adapterBT == null) {
            // Device does not support Bluetooth
            Toast.makeText(context, "Nerastas Bluetooth", Toast.LENGTH_LONG).show();;
        }

        findDevice();
        if(device != null) {
            connect();
        }
    }

    public void sendMessage(String message){
        if(device != null) {
            connectedThread.write(message);
        }
    }

    public void findDevice(){
        pairedDevices = adapterBT.getBondedDevices();
        if(adapterBT.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                if (name.equals("SB")) {
                    this.device = device;
                    break;
                }
            }
        }
    }

    public void connect(){
        if(!adapterBT.isEnabled()) {
            Toast.makeText(context, "Bluetooth išjungtas", Toast.LENGTH_LONG).show();
            return;
        }

        final String address = device.getAddress();
        final String name = device.getName();

        new Thread()
        {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = adapterBT.getRemoteDevice(address);

                try {
                    socketBT = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(context, "Nepavyko užmegzti ryšio", Toast.LENGTH_LONG).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    socketBT.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        socketBT.close();
                        handler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(context, "Nepavyko užmegzti ryšio", Toast.LENGTH_SHORT).show();
                    }
                }
                if(fail == false) {
                    connectedThread = new ConnectedThread(socketBT);
                    connectedThread.start();

                    handler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BT_UUID);
        //creates secure outgoing connection with BT device using UUID
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[16];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(bytes != 0) {
                        SystemClock.sleep(100);
                        mmInStream.read(buffer);
                    }

                    Log.d("SEND", Integer.toString(bytes));
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
