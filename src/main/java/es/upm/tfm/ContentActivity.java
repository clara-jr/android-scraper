package es.upm.tfm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.List;
import java.util.Locale;

public class ContentActivity extends AppCompatActivity {

    private String filterTitle, filterAuthor, filterBody, filterImage;

    private TextView tvTitle, tvAuthor, tvBody;
    private ImageView ivImg;

    private RelativeLayout rlProgress;

    private String title, author, body;

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
        if (getIntent().getBooleanExtra("texttospeech", false)) {
            if (text.length() >= 4000) { // TextToSpeech.getMaxSpeechInputLength()
                List<String> ret = new ArrayList<String>((text.length() + 3999 - 1) / 3999);
                for (int start = 0; start < text.length(); start += 3999) {
                    ret.add(text.substring(start, Math.min(text.length(), start + 3999)));
                }
                for (String t : ret) {
                    tts.speak(t, TextToSpeech.QUEUE_ADD, null);
                }
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
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
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            event.startTracking();
            if (longPress) {
                upPress = false;
            } else {
                upPress = true;
                longPress = false;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            event.startTracking();
            upPress = true;
            longPress = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void getContent(final String url, final String selectedUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        switch (jsonfilters.getString("type")) {
                            case "Title":
                                filterTitle = jsonfilters.getString("pattern");
                                break;
                            case "Author":
                                filterAuthor = jsonfilters.getString("pattern");
                                break;
                            case "Body":
                                filterBody = jsonfilters.getString("pattern");
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                final StringBuilder builder = new StringBuilder();
                try {
                    Document doc = Jsoup.connect(selectedUrl).get();
                    Elements elem_title = doc.select(filterTitle);
                    builder.append(elem_title.text());
                    title = builder.toString();
                    builder.setLength(0);
                    if (title == "") title = getIntent().getStringExtra("name");
                    Elements elem_author = doc.select(filterAuthor);
                    builder.append(elem_author.text());
                    author = builder.toString();
                    builder.setLength(0);
                    Elements elem_body = doc.select(filterBody);
                    for (Element elem : elem_body) {
                        builder.append(elem.text()).append("\n").append("\n");
                    }
                    body = builder.toString();
                    builder.setLength(0);
                } catch (Exception e) { e.printStackTrace(); }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTitle.setText(title);
                        tvBody.setText(body);
                        if (author != null && !author.equals("")) {
                            tvAuthor.setVisibility(View.VISIBLE);
                            tvAuthor.setText(author);
                        } else {
                            tvAuthor.setVisibility(View.GONE);
                        }
                        rlProgress.setVisibility(View.GONE);
                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status == TextToSpeech.SUCCESS){
                                    tts.setLanguage(new Locale("es", "",""));
                                    ConvertTextToSpeech(tvBody.getText().toString());
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void getImage(final String url, final String selectedUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        if(jsonfilters.getString("type").equals("Image"))
                            filterImage = jsonfilters.getString("pattern");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                Bitmap bitmap = null;
                try {
                    Document doc = Jsoup.connect(selectedUrl).get();
                    Elements img = doc.select(filterImage);
                    // Locate the src attribute
                    String imgSrc = img.attr("src");
                    if (!imgSrc.startsWith("http")) {
                        if (imgSrc.startsWith("//"))
                            imgSrc = "http:".concat(imgSrc);
                        else if (imgSrc.startsWith("/"))
                            imgSrc = "http://" + url.concat(imgSrc);
                        else imgSrc = selectedUrl.concat(imgSrc);
                    }
                    // Download image from URL
                    InputStream input = new URL(imgSrc).openStream();
                    // Decode Bitmap
                    bitmap = BitmapFactory.decodeStream(input);
                } catch (Exception e) { e.printStackTrace(); }
                final Bitmap finalBitmap = bitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalBitmap != null) {
                            ivImg.setVisibility(View.VISIBLE);
                            ivImg.setImageBitmap(finalBitmap);
                        } else {
                            ivImg.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvBody = findViewById(R.id.tvBody);
        ivImg = (ImageView) findViewById(R.id.ivImg);

        String url = getIntent().getStringExtra("url");
        String selectedUrl = getIntent().getStringExtra("selectedUrl");

        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.VISIBLE);

        setTitle(getIntent().getStringExtra("title"));

        getContent(url, selectedUrl);
        getImage(url, selectedUrl);
    }
}