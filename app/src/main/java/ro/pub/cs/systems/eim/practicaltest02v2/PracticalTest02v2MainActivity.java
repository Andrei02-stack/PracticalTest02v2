package ro.pub.cs.systems.eim.practicaltest02v2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PracticalTest02v2MainActivity extends AppCompatActivity {

    private EditText wordInput;
    private Button defineButton;
    private TextView definitionOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02v2_main);

        wordInput = findViewById(R.id.wordInput);
        defineButton = findViewById(R.id.defineButton);
        definitionOutput = findViewById(R.id.definitionOutput);

        // Register receiver
        IntentFilter intentFilter = new IntentFilter("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION");
        ContextCompat.registerReceiver(this, new DefinitionReceiver(), intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // Set button click listener
        defineButton.setOnClickListener(view -> {
            String word = wordInput.getText().toString();
            if (!word.isEmpty()) {
                new FetchDefinitionTask().execute(word);
            } else {
                definitionOutput.setText("Please enter a word.");
            }
        });
    }

    private class FetchDefinitionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String word = params[0];
            String apiUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Log the raw response for debugging purposes
                Log.d("ServerResponse", response.toString());

                // Parse JSON and extract the first definition
                JSONArray jsonArray = new JSONArray(response.toString());
                JSONArray meanings = jsonArray.getJSONObject(0).getJSONArray("meanings");
                JSONArray definitions = meanings.getJSONObject(0).getJSONArray("definitions");
                return definitions.getJSONObject(0).getString("definition");

            } catch (Exception e) {
                Log.e("Error", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                // Broadcast the definition
                Intent intent = new Intent("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION").setPackage(getPackageName());
                intent.putExtra("definition", result);
                sendBroadcast(intent);
                definitionOutput.setText(result);
            } else {
                definitionOutput.setText("Error fetching definition.");
            }
        }
    }

    private class DefinitionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION")) {
                String definition = intent.getStringExtra("definition");
                if (definition != null) {
                    definitionOutput.setText(definition);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister receiver
        unregisterReceiver(new DefinitionReceiver());
    }
}