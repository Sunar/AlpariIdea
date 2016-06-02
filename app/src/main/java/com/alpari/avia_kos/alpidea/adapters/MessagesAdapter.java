package com.alpari.avia_kos.alpidea.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alpari.avia_kos.alpidea.models.Message;
import com.alpari.avia_kos.alpidea.R;
import com.alpari.avia_kos.alpidea.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avia-Kos on 10.05.16.
 */
public class MessagesAdapter extends ArrayAdapter<Message>{

    ArrayList<Message> mMessages;

    public MessagesAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        mMessages = (ArrayList)objects;
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    public void setSource(ArrayList<Message> messages){
        mMessages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.messages_item, null);
        }

        Message m = mMessages.get(position);

        if (m != null) {
            LinearLayout llMessage = (LinearLayout) v.findViewById(R.id.llMessage);
            if(!m.isRead())
                llMessage.setBackgroundColor(Color.argb(120, 120, 120, 120));
            else
                llMessage.setBackgroundColor(0);
            TextView tvMsgFrom = (TextView) v.findViewById(R.id.tvMsgFrom);
            TextView tvMsg = (TextView) v.findViewById(R.id.tvMsg);
            TextView tvMsgType = (TextView) v.findViewById(R.id.tvMsgType);
            if(User.getInstance().getLogin().equals(m.getFrom())){
                String s = getContext().getString(R.string.sent) + " " + m.getDate();
                tvMsgType.setText(s);
                tvMsgFrom.setText(m.getTo());
            }
            else {
                String s = getContext().getString(R.string.get) + " " + m.getDate();
                tvMsgType.setText(s);
                tvMsgFrom.setText(m.getFrom());
            }
            tvMsg.setText(m.getContent());
        }

        return v;
    }

    @Override
    public Message getItem(int position) {
        return mMessages.get(position);
    }
}
