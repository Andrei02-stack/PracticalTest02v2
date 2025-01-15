package ro.pub.cs.systems.eim.practicaltest02v2;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TimeActivity extends AppCompatActivity {

    private TextView timeOutput;
    private Button connectButton;
    private Handler handler;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_activity);

        timeOutput = findViewById(R.id.timeOutput);
        connectButton = findViewById(R.id.connectButton);
        handler = new Handler();

        // Set button listener to start connection
        connectButton.setOnClickListener(view -> {
            if (!isRunning) {
                isRunning = true;
                startFetchingTime();
            }
        });
    }

    private void startFetchingTime() {
        new Thread(() -> {
            try {
                Log.d("TimeActivity", "Attempting to connect to server...");
                Socket socket = new Socket("10.41.29.51", 12345); // Update IP and port
                Log.d("TimeActivity", "Connected to server!");

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                while (isRunning) {
                    Log.d("TimeActivity", "Waiting for server data...");
                    String serverTime = reader.readLine();
                    if (serverTime != null) {
                        Log.d("TimeActivity", "Received: " + serverTime);
                        handler.post(() -> timeOutput.setText(serverTime));
                        writer.println("ACK"); // Send acknowledgment
                    }
                }

                socket.close();
            } catch (Exception e) {
                Log.e("TimeActivity", "Error: " + e.getMessage(), e);
                handler.post(() -> timeOutput.setText("Error connecting to server."));
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false; // Stop the thread
    }
}