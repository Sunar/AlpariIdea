package com.alpari.avia_kos.alpidea.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alpari.avia_kos.alpidea.DB;
import com.alpari.avia_kos.alpidea.models.Message;
import com.alpari.avia_kos.alpidea.MySwipeRefreshLayout;
import com.alpari.avia_kos.alpidea.R;
import com.alpari.avia_kos.alpidea.models.User;
import com.alpari.avia_kos.alpidea.activities.ProfileActivity;
import com.alpari.avia_kos.alpidea.adapters.MessagesAdapter;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Avia-Kos on 16.05.16.
 */
public class MessagesFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    MessagesAdapter adapter;
    ProgressDialog pd;
    MySwipeRefreshLayout swipeLayout;
    FloatingActionButton fab;
    View sendMessageContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, null);
        swipeLayout = (MySwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        fab = (FloatingActionButton)v.findViewById(R.id.fab);
        fab.setOnClickListener(this);

        sendMessageContent = inflater.inflate(R.layout.dialog_send_message, null);
        (sendMessageContent.findViewById(R.id.etMessageDialogUser)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    CheckLoginTask task = new CheckLoginTask((EditText)sendMessageContent.findViewById(R.id.etMessageDialogUser));
                    task.execute();
                }
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeLayout.setListView(getListView());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pd = new ProgressDialog(getActivity());
        pd.setTitle(getString(R.string.wait));
        pd.setMessage(getString(R.string.get_messages_process));
        pd.show();
        GetMessagesTask task = new GetMessagesTask();
        task.execute();
        adapter = new MessagesAdapter(getActivity(), R.layout.messages_item, new ArrayList<Message>());
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(((Message)l.getItemAtPosition(position)).getFrom().equals(User.getInstance().getLogin()))
            return;
        ReadMessageTask task = new ReadMessageTask();
        task.execute((Message)l.getItemAtPosition(position));
        v.findViewById(R.id.llMessage).setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(sendMessageContent)
                // Add action buttons
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SendMessageTask task = new SendMessageTask(((EditText)sendMessageContent.findViewById(R.id.etMessageDialogUser)).getText().toString(),
                                ((EditText)sendMessageContent.findViewById(R.id.etMessageDialogText)).getText().toString());
                        task.execute();
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setTitle(getString(R.string.message_title));
        builder.show();
    }

    private class GetMessagesTask extends AsyncTask<Void, Void, ArrayList<Message>> {

        @Override
        protected ArrayList<Message> doInBackground(Void... params) {
            User user = User.getInstance();
            if (user.getMessages().size() > 0)
                return user.getMessages();
            DB db = DB.getInstance();
            try {
                user.setMessages(db.getMessages());
                return user.getMessages();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Message> messages) {
            super.onPostExecute(messages);
            pd.dismiss();
            swipeLayout.setRefreshing(false);
            if(messages != null){
                adapter.setSource(messages);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pd.dismiss();
        }
    }

    private class ReadMessageTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... params) {
            DB db = DB.getInstance();
            try {
                db.readMessage(params[0]);
            } catch (SQLException e) {
                Toast.makeText(getActivity(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((ProfileActivity)getActivity()).new GetPointsStatusesMessagesIdeas().execute();

        }
    }

    private class SendMessageTask extends AsyncTask<Void, Void, Boolean> {

        private String login;
        private String message;

        public SendMessageTask(String login, String message) {
            this.login = login;
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                return db.sendMessage(User.getInstance().getId(), login, message);
            } catch (SQLException e) {
                Toast.makeText(getActivity(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            try {
                if(success) {
                    Toast.makeText(getActivity(), getString(R.string.message_delivered), Toast.LENGTH_SHORT).show();
                    onRefresh();
                }
                else
                    Toast.makeText(getActivity(), getString(R.string.message_not_delivered), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class CheckLoginTask extends AsyncTask<Void, Void, Boolean> {

        EditText et;
        String etText;

        public CheckLoginTask(EditText et) {
            this.et = et;
            this.etText = et.getText().toString();
            this.et.setError(null);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                return !db.loginIsVacant(etText);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(!success)
                et.setError(getString(R.string.user_is_not_exist));
        }
    }

    @Override
    public void onRefresh() {
        User.getInstance().getMessages().clear();
        new GetMessagesTask().execute();
        ((ProfileActivity)getActivity()).new GetPointsStatusesMessagesIdeas().execute();
    }
}
