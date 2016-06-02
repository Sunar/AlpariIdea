package com.alpari.avia_kos.alpidea.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avia-Kos on 10.05.16.
 */
public class SpinnerAdapter extends ArrayAdapter<String>{

    ArrayList<String> mTypes;

    public SpinnerAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mTypes = (ArrayList)objects;
    }

    public void setSource(ArrayList<String> types){
        this.mTypes = types;
    }

    @Override
    public int getCount() {
        return mTypes.size();
    }

    @Override
    public String getItem(int position) {
        return mTypes.get(position);
    }

}
