package es.upm.tfm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ListView lvList;

    private RelativeLayout rlProgress;

    private HashMap<String, String> lvMap = new HashMap<String, String>();
    private HashMap<String, String> lvDescription = new HashMap<String, String>();
    private HashMap<String, Bitmap> lvLogo = new HashMap<String, Bitmap>();

    private int size;
    private int pos = -1;

    private TextToSpeech tts;

    @Override
    protected void onPause() {
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    private void ConvertTextToSpeech(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (pos < size) pos++;
            lvList.setItemChecked(pos, true);
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS){
                        int result = tts.setLanguage(new Locale("es", "",""));
                        String selectedItem = (String) lvList.getItemAtPosition(pos);
                        ConvertTextToSpeech(selectedItem);
                    }
                }
            });
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (pos > 0) pos--;
            lvList.setItemChecked(pos, true);
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS){
                        int result = tts.setLanguage(new Locale("es", "",""));
                        String selectedItem = (String) lvList.getItemAtPosition(pos);
                        ConvertTextToSpeech(selectedItem);
                    }
                }
            });
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            if (pos >= 0 && pos <= size) {
                Intent intent = new Intent(getApplicationContext(), MiddleActivity.class);
                String selectedItem = (String) lvList.getItemAtPosition(pos);
                lvList.setItemChecked(pos, false);
                pos = -1;
                String selectedUrl = lvMap.get(selectedItem);
                intent.putExtra("title", selectedItem);
                intent.putExtra("url", selectedUrl);
                intent.putExtra("level", 1);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection urlConnection = null;
                final ArrayList<String> lvArray = new ArrayList<String>();
                try {
                    url = new URL("http://138.4.140.84:1607/webs/parser");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader buffered = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    StringBuilder builder = new StringBuilder();
                    while((line = buffered.readLine()) != null)
                        builder.append(line);
                    in.close();
                    JSONArray jsonArray = new JSONArray(builder.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        lvArray.add(json.getString("name"));
                        lvMap.put(json.getString("name"), json.getString("url"));
                        lvDescription.put(json.getString("name"), json.getString("genre"));
                        JSONArray objectJsonArray = json.getJSONArray("filters");
                        for (int j = 0; j < objectJsonArray.length(); j++) {
                            JSONObject jsonfilters = objectJsonArray.getJSONObject(j);
                            if(jsonfilters.getString("type").equals("Logo")) {
                                Bitmap bitmap;
                                try {
                                    String imgSrc = jsonfilters.getString("pattern");
                                    // Download image from URL
                                    InputStream input = new URL(imgSrc).openStream();
                                    // Decode Bitmap
                                    bitmap = BitmapFactory.decodeStream(input);
                                    lvLogo.put(json.getString("name"), bitmap);
                                } catch (Exception e) { e.printStackTrace(); }
                            }
                        }
                    }
                    size = lvArray.size() - 1;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvList.setAdapter(new AdapterItem(getApplicationContext(), lvArray, lvDescription, lvLogo));
                        lvList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        rlProgress.setVisibility(View.GONE);
                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status == TextToSpeech.SUCCESS){
                                    tts.setLanguage(new Locale("es", "",""));
                                    String selectedItem = (String) lvList.getItemAtPosition(pos);
                                    ConvertTextToSpeech(selectedItem);
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private AdapterView.OnItemClickListener ocllvList = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), MiddleActivity.class);
            // Get the selected item text from ListView
            String selectedItem = (String) adapterView.getItemAtPosition(position);
            lvList.setItemChecked(position, false);
            pos = -1;
            String selectedUrl = lvMap.get(selectedItem);
            intent.putExtra("title", selectedItem);
            intent.putExtra("url", selectedUrl);
            intent.putExtra("level", 1);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvList = (ListView) findViewById(R.id.lvList);

        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.VISIBLE);

        getList();

        lvList.setClickable(true);
        lvList.setOnItemClickListener(ocllvList);
    }

}

