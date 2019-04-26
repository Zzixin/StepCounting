package sjtu.iiot.pedometer_iiot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
public class MainActivity extends Activity implements SensorEventListener,
        OnClickListener {
    /** Called when the activity is first created. */
    //Create a LOG label
    private Button mWriteButton, mStopButton;
    private boolean doWrite = false;
    private SensorManager sm;
    private float lowX = 0, lowY = 0, lowZ = 0;
    private final float FILTERING_VALAUE = 0.1f;
    private TextView AT,ACT;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AT = (TextView)findViewById(R.id.AT);
        ACT = (TextView)findViewById(R.id.onAccuracyChanged);
        verifyStoragePermissions(this);
        //Create a SensorManager to get the system’s sensor service
        sm =
                (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        /* **********************************************************************
        * Using the most common method to register an event
        * Parameter1 ：SensorEventListener detectophone
        * Parameter2 ：Sensor one service could have several Sensor
        realizations. Here,We use getDefaultSensor to get the defaulted Sensor
        * Parameter3 ：Mode We can choose the refresh frequency of the
        data change
        * **********************************************************************/
        // Register the acceleration sensor
        sm.registerListener(this,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);//High sampling rate；.SENSOR_DELAY_NORMAL means a lower sampling rate
        try {
            FileOutputStream fout = openFileOutput("acc.txt",
                    Context.MODE_PRIVATE);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mWriteButton = (Button) findViewById(R.id.Button_Write);
        mWriteButton.setOnClickListener(this);
        mStopButton = (Button) findViewById(R.id.Button_Stop);
        mStopButton.setOnClickListener(this);
    }
    public void onPause(){
        super.onPause();
    }
    public void onClick(View v) {
        if (v.getId() == R.id.Button_Write) {
            doWrite = true;
        }
        if (v.getId() == R.id.Button_Stop) {
            doWrite = false;
        }
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        ACT.setText("onAccuracyChanged is detonated");
    }
    public void onSensorChanged(SensorEvent event) {
        String message = new String();
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float X = event.values[0];
            float Y = event.values[1];
            float Z = event.values[2];
            //Low-Pass Filter
            lowX = X * FILTERING_VALAUE + lowX * (1.0f -
                    FILTERING_VALAUE);
            lowY = Y * FILTERING_VALAUE + lowY * (1.0f -
                    FILTERING_VALAUE);
            lowZ = Z * FILTERING_VALAUE + lowZ * (1.0f -
                    FILTERING_VALAUE);
            //High-pass filter
            float highX = X - lowX;
            float highY = Y - lowY;
            float highZ = Z - lowZ;
            double highA = Math.sqrt(highX * highX + highY * highY + highZ
                    * highZ);
            DecimalFormat df = new DecimalFormat("#,##0.000");
            message = df.format(highX) + " ";
            message += df.format(highY) + " ";
            message += df.format(highZ) + " ";
            message += df.format(highA) + "\n";
            AT.setText(message + "\n");
            if (doWrite) {
                write2file(message);
            }
        }
    }
    private void write2file(String a){
        try {
            File file = new File("/sdcard/acc.txt");//write the result into/sdcard/acc.txt
            if (!file.exists()){
                file.createNewFile();}
            // Open a random access file stream for reading and writing
            RandomAccessFile randomFile = new RandomAccessFile("/sdcard/acc.txt", "rw");
            // The length of the file (the number of bytes)
            long fileLength = randomFile.length();
            // Move the file pointer to the end of the file
            randomFile.seek(fileLength);
            randomFile.writeBytes(a);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
// Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
// We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}