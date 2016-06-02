package com.alpari.avia_kos.alpidea.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alpari.avia_kos.alpidea.models.Idea;
import com.alpari.avia_kos.alpidea.R;
import com.alpari.avia_kos.alpidea.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avia-Kos on 10.05.16.
 */
public class MyIdeasAdapter extends ArrayAdapter<Idea>{

    ArrayList<Idea> mIdeas;

    public MyIdeasAdapter(Context context, int resource, List<Idea> objects) {
        super(context, resource, objects);
        mIdeas = (ArrayList)objects;
    }

    @Override
    public int getCount() {
        return mIdeas.size();
    }

    public void setSource(ArrayList<Idea> ideas){
        mIdeas = ideas;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.ideas_item, null);
        }

        Idea p = mIdeas.get(position);

        if (p != null) {
            TextView tvMyIdea = (TextView) v.findViewById(R.id.tvMyIdea);
            TextView tvMyIdeaStatus = (TextView) v.findViewById(R.id.tvMyIdeaStatus);
            String content = p.getContent();
            if(User.getInstance().isRuk() || User.getInstance().isExpert()){
                if(p.getStatus().isActualForExpert())
                    v.findViewById(R.id.llIdea).setBackgroundColor(Color.argb(120, 120, 120, 120));
                else
                    v.findViewById(R.id.llIdea).setBackgroundColor(0);
            }
            if(content.length() > 40)
                tvMyIdea.setText(p.getContent().substring(0, 40) + "...");
            else
                tvMyIdea.setText(p.getContent());
            tvMyIdeaStatus.setText(p.getStatus().getStatus());
        }

        return v;
    }

    @Override
    public Idea getItem(int position) {
        return mIdeas.get(position);
    }
}
