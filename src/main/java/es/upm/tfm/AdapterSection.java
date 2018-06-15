package es.upm.tfm;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterSection extends ArrayAdapter<String> {

    private HashMap<String, String> lvUrl = new HashMap<String, String>();
    private HashMap<String, String> lvDescription = new HashMap<String, String>();

    AdapterSection(Context context, ArrayList<String> list, HashMap<String, String> mapDescription) {
        super(context, R.layout.item, list);
        this.lvDescription = mapDescription;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater buckysInflater = LayoutInflater.from(getContext());
        View customView = buckysInflater.inflate(R.layout.section, parent, false);

        String selectedItem = getItem(position);
        String selectedDescription = lvDescription.get(selectedItem);

        TextView tvTitle = (TextView) customView.findViewById(R.id.tvTitle);
        tvTitle.setText(selectedItem);

        TextView tvDescription = (TextView) customView.findViewById(R.id.tvDescription);
        if (selectedDescription != null && !selectedDescription.equals("")) {
            tvDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(selectedDescription);
        } else {
            tvDescription.setVisibility(View.GONE);
            LayoutParams lp = (RelativeLayout.LayoutParams) tvTitle.getLayoutParams();
            Resources r = getContext().getResources();
            lp.setMargins((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    15,
                    r.getDisplayMetrics()
            ), (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    12,
                    r.getDisplayMetrics()
            ), (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    25,
                    r.getDisplayMetrics()
            ), (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    12,
                    r.getDisplayMetrics()
            ));
            tvTitle.setLayoutParams(lp);
        }

        return customView;
    }
}
