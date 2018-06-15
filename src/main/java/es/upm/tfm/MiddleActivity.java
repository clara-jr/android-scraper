package es.upm.tfm;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MiddleActivity extends AppCompatActivity {

    private ListView lvList;

    private RelativeLayout rlProgress;

    private ArrayList<String> filtersArray = new ArrayList<String>();

    private HashMap<String, String> lvMap = new HashMap<String, String>();
    private ArrayList<String> lvArray = new ArrayList<String>();
    private HashMap<String, String> lvDescription = new HashMap<String, String>();
    private String filterDate;

    private String title;
    private int level;
    private int nextLevel;
    private int maxLevel;

    private int size;
    private int pos = -1;

    // Flags
    boolean upPress = false;
    boolean longPress = false;

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
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            upPress = false;
            longPress = true;
            onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
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
            event.startTracking();
            if (longPress) {
                upPress = false;
            } else {
                upPress = true;
                longPress = false;
            }
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            if (pos >= 0 && pos <= size) {
                Intent intent;
                nextLevel = level + 1;
                if (nextLevel == maxLevel) {
                    intent = new Intent(getApplicationContext(), ContentActivity.class);
                    intent.putExtra("texttospeech", true);
                } else {
                    intent = new Intent(getApplicationContext(), MiddleActivity.class);
                }
                // Get the selected item text from ListView
                String selectedItem = (String) lvList.getItemAtPosition(pos);
                lvList.setItemChecked(pos, false);
                pos = -1;
                String selectedUrl = lvMap.get(selectedItem);
                intent.putExtra("title", title);
                intent.putExtra("name", selectedItem);
                intent.putExtra("url", getIntent().getStringExtra("url"));
                intent.putExtra("selectedUrl", selectedUrl);
                intent.putExtra("level", nextLevel);
                intent.putExtra("maxLevel", maxLevel);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            event.startTracking();
            if (upPress) {
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
            }
            upPress = true;
            longPress = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private AdapterView.OnItemClickListener ocllvList = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent;
            nextLevel = level + 1;
            if (nextLevel == maxLevel) {
                intent = new Intent(getApplicationContext(), ContentActivity.class);
                intent.putExtra("texttospeech", false);
            } else {
                intent = new Intent(getApplicationContext(), MiddleActivity.class);
            }
            // Get the selected item text from ListView
            String selectedItem = (String) adapterView.getItemAtPosition(position);
            lvList.setItemChecked(position, false);
            pos = -1;
            String selectedUrl = lvMap.get(selectedItem);
            intent.putExtra("title", title);
            intent.putExtra("name", selectedItem);
            intent.putExtra("url", getIntent().getStringExtra("url"));
            intent.putExtra("selectedUrl", selectedUrl);
            intent.putExtra("level", nextLevel);
            intent.putExtra("maxLevel", maxLevel);
            startActivity(intent);
        }
    };

    private void getMaxLevel(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL uri;
                HttpURLConnection urlConnection = null;
                final ArrayList<String> lvArray = new ArrayList<String>();
                try {
                    uri = new URL("http://138.4.140.84:1607/webs/url/" + url);
                    urlConnection = (HttpURLConnection) uri.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader buffered = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    StringBuilder builder = new StringBuilder();
                    while((line = buffered.readLine()) != null)
                        builder.append(line);
                    in.close();
                    JSONArray jsonArray = new JSONArray(builder.toString());
                    JSONObject json = jsonArray.getJSONObject(0);
                    JSONArray objectJsonArray = json.getJSONArray("filters");
                    for (int i = 0; i < objectJsonArray.length(); i++) {
                        JSONObject jsonfilters = objectJsonArray.getJSONObject(i);
                        if (Integer.parseInt(jsonfilters.getString("level")) > maxLevel)
                            maxLevel = Integer.parseInt(jsonfilters.getString("level"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void getContent(final String url, final String selectedUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL uri;
                    HttpURLConnection urlConnection = null;
                    try {
                        uri = new URL("http://138.4.140.84:1607/webs/url/" + url);
                        urlConnection = (HttpURLConnection) uri.openConnection();
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader buffered = new BufferedReader(new InputStreamReader(in));
                        String line = "";
                        StringBuilder builder = new StringBuilder();
                        while((line = buffered.readLine()) != null)
                            builder.append(line);
                        in.close();
                        JSONArray jsonArray = new JSONArray(builder.toString());
                        JSONObject json = jsonArray.getJSONObject(0);
                        JSONArray objectJsonArray = json.getJSONArray("filters");
                        for (int i = 0; i < objectJsonArray.length(); i++) {
                            JSONObject jsonfilters = objectJsonArray.getJSONObject(i);
                            if (Integer.parseInt(jsonfilters.getString("level")) == level)
                                filtersArray.add(jsonfilters.getString("pattern"));
                            else if (jsonfilters.getString("type").equals("Date"))
                                filterDate = jsonfilters.getString("pattern");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                    for (String filter : filtersArray) {
                        Document doc = Jsoup.connect(selectedUrl).get();
                        Elements links = doc.select(filter);
                        for (Element link : links) {
                            String href = link.attr("href");
                            if (!href.equals("#")) {
                                if (!href.startsWith("http")) {
                                    if (href.startsWith("//"))
                                        href = "http:".concat(href);
                                    else if (href.startsWith("/"))
                                        href = "http://" + url.concat(href);
                                    else href = selectedUrl.concat(href);
                                }
                                if (url.equals("as.com") && level == 1) {
                                    if (lvArray.size() < 7) {
                                        lvMap.put(link.text(), href);
                                        lvArray.add(link.text());
                                        String date = "";
                                        StringBuilder builder = new StringBuilder();
                                        try {
                                            doc = Jsoup.connect(href).get();
                                            Elements elem_date = doc.select(filterDate);
                                            builder.append(elem_date.text());
                                            lvDescription.put(link.text(), builder.toString());
                                            builder.setLength(0);
                                        } catch (Exception e) { e.printStackTrace(); }
                                    }
                                } else {
                                    lvMap.put(link.text(), href);
                                    lvArray.add(link.text());
                                    String date = "";
                                    StringBuilder builder = new StringBuilder();
                                    try {
                                        doc = Jsoup.connect(href).get();
                                        Elements elem_date = doc.select(filterDate);
                                        builder.append(elem_date.text());
                                        lvDescription.put(link.text(), builder.toString());
                                        builder.setLength(0);
                                    } catch (Exception e) { e.printStackTrace(); }
                                }

                            }
                        }
                    }
                    size = lvArray.size() - 1;
                } catch (Exception e) { e.printStackTrace(); }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvList.setAdapter(new AdapterSection(getApplicationContext(), lvArray, lvDescription));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_middle);

        title = getIntent().getStringExtra("title");
        String name = getIntent().hasExtra("name") ? ": " + getIntent().getStringExtra("name") : "";
        String selected = getIntent().hasExtra("selectedUrl") ? getIntent().getStringExtra("selectedUrl") : "http://" + getIntent().getStringExtra("url");
        level = getIntent().getIntExtra("level", 0);
        String url = getIntent().getStringExtra("url");
        getMaxLevel(url);

        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.VISIBLE);

        setTitle(title);
        getContent(url, selected);

        lvList = (ListView) findViewById(R.id.lvList);

        lvList.setClickable(true);
        lvList.setOnItemClickListener(ocllvList);
    }
}
