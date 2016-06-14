package com.alpari.avia_kos.alpidea.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.alpari.avia_kos.alpidea.DB;
import com.alpari.avia_kos.alpidea.models.User;
import com.alpari.avia_kos.alpidea.R;
import com.alpari.avia_kos.alpidea.adapters.SpinnerAdapter;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Avia-Kos on 10.05.16.
 */
public class SendIdeaFragment extends Fragment implements View.OnClickListener{
    View frame;
    SpinnerAdapter adapter;
    Button btnSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        frame = inflater.inflate(R.layout.fragment_send_idea, null);
        btnSend = (Button) frame.findViewById(R.id.btnSendIdea);
        btnSend.setOnClickListener(this);

        Spinner sIdeaTypes = (Spinner) frame.findViewById(R.id.sIdeaTypes);
        ArrayList<String> str = new ArrayList<>();
        str.add(getString(R.string.load_data));
        adapter = new SpinnerAdapter(getActivity(), android.R.layout.simple_spinner_item, str);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sIdeaTypes.setAdapter(adapter);
        GetIdeaTypesTask task = new GetIdeaTypesTask();
        task.execute();
        return frame;
    }

    @Override
    public void onClick(View v) {
        String type = ((Spinner)frame.findViewById(R.id.sIdeaTypes)).getSelectedItem().toString();
        String content = ((EditText)frame.findViewById(R.id.etIdea)).getText().toString();
        String res = ((EditText)frame.findViewById(R.id.etRes)).getText().toString();
        String goal = ((EditText)frame.findViewById(R.id.etGoal)).getText().toString();
        String time = ((EditText)frame.findViewById(R.id.etRealizeTime)).getText().toString();

        btnSend.setText(getString(R.string.send_data));
        btnSend.setEnabled(false);
        SendIdeaTask task = new SendIdeaTask(type, content, res, goal, time);
        task.execute();
    }

    private class GetIdeaTypesTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                return db.getIdeaTypes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> types) {
            super.onPostExecute(types);
            adapter.setSource(types);
            adapter.notifyDataSetChanged();
        }

    }


    private class SendIdeaTask extends AsyncTask<Void, Void, Boolean> {

        private String type;
        private String content;
        private String res;
        private String goal;
        private String time;

        public SendIdeaTask(String type, String content, String res, String goal, String time) {
            this.type = type;
            this.content = content;
            this.res = res;
            this.goal = goal;
            this.time = time;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                return db.sendIdea(User.getInstance().getLogin(), type, content, res, goal, time);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            try {
                if(success){
                    //Toast.makeText(getActivity(), "Успешно", Toast.LENGTH_SHORT).show();
                    User.getInstance().getIdeas().clear();
                    showMyIdeas();
                }
                else {
                    Toast.makeText(getActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    btnSend.setText(getString(R.string.send));
                    btnSend.setEnabled(true);
                }
            } catch (NullPointerException e) {
                Toast.makeText(getActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                btnSend.setText(getString(R.string.send));
                btnSend.setEnabled(true);
            }
        }

        private void showMyIdeas(){
            FragmentTransaction fTrans = getActivity().getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.flContent, new MyIdeasFragment());
            fTrans.commit();
            // set the toolbar title
            if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.title_my_ideas));
            }
        }
    }
}
