package es.upm.tfm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AdapterItem extends ArrayAdapter<String> {

    private HashMap<String, String> lvDescription = new HashMap<String, String>();
    private HashMap<String, Bitmap> lvLogo = new HashMap<String, Bitmap>();

    AdapterItem(Context context, ArrayList<String> list, HashMap<String, String> mapDescription, HashMap<String, Bitmap> mapLogo) {
        super(context, R.layout.item, list);
        this.lvDescription = mapDescription;
        this.lvLogo = mapLogo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater buckysInflater = LayoutInflater.from(getContext());
        View customView = buckysInflater.inflate(R.layout.item, parent, false);

        String selectedItem = getItem(position);
        String selectedDescription = lvDescription.get(selectedItem);
        Bitmap selectedLogo = lvLogo.get(selectedItem);

        TextView tvTitle = (TextView) customView.findViewById(R.id.tvTitle);
        tvTitle.setText(selectedItem);

        TextView tvDescription = (TextView) customView.findViewById(R.id.tvDescription);
        tvDescription.setText(selectedDescription);

        ImageView ivLogo = (ImageView) customView.findViewById(R.id.ivLogo);
        ivLogo.setImageResource(R.drawable.ic_launcher_background);
        ivLogo.setImageBitmap(selectedLogo);

        return customView;
    }
}
