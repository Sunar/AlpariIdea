package com.alpari.avia_kos.alpidea.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alpari.avia_kos.alpidea.models.Prize;
import com.alpari.avia_kos.alpidea.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avia-Kos on 10.05.16.
 */
public class PrizesAdapter extends ArrayAdapter<Prize>{

    ArrayList<Prize> mPrizes;

    public PrizesAdapter(Context context, int resource, List<Prize> objects) {
        super(context, resource, objects);
        mPrizes = (ArrayList)objects;
    }

    @Override
    public int getCount() {
        return mPrizes.size();
    }

    @Override
    public Prize getItem(int position) {
        return mPrizes.get(position);
    }

    public void setSource(ArrayList<Prize> prizes){
        mPrizes = prizes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.prizes_item, null);
        }

        Prize p = mPrizes.get(position);

        if (p != null) {
            TextView tvPrize = (TextView) v.findViewById(R.id.tvPrize);
            TextView tvPrizeDescr = (TextView) v.findViewById(R.id.tvPrizeDescr);
            ImageView ivPrize = (ImageView) v.findViewById(R.id.ivPrize);
            tvPrize.setText(p.getName());
            tvPrizeDescr.setText(p.getDescription());
            if(p.getImageUrl() != null)
                new DownloadImageTask(ivPrize).execute(p.getImageUrl());
        }

        return v;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
