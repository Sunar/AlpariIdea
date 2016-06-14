package com.alpari.avia_kos.alpidea.fragments;

/**
 * Created by Avia-Kos on 10.05.16.
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alpari.avia_kos.alpidea.DB;
import com.alpari.avia_kos.alpidea.models.Idea;
import com.alpari.avia_kos.alpidea.MySwipeRefreshLayout;
import com.alpari.avia_kos.alpidea.R;
import com.alpari.avia_kos.alpidea.models.User;
import com.alpari.avia_kos.alpidea.adapters.MyIdeasAdapter;

import java.sql.SQLException;
import java.util.ArrayList;

public class MyIdeasFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener{
    MyIdeasAdapter adapter;
    ProgressDialog pd;
    MySwipeRefreshLayout swipeLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.swipable_list, null);
        swipeLayout = (MySwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
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
        pd.setMessage(getString(R.string.get_my_ideas_process));
        pd.show();
        GetMyIdeasTask task = new GetMyIdeasTask();
        task.execute();
        adapter = new MyIdeasAdapter(getActivity(), R.layout.ideas_item, new ArrayList<Idea>());
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Fragment fragment = new IdeaDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("idea", (Idea)l.getItemAtPosition(position));
        fragment.setArguments(bundle);
        FragmentTransaction fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.flContent, fragment);
        fTrans.addToBackStack(null);
        fTrans.commit();
    }

    @Override
    public void onRefresh() {
        User.getInstance().getIdeas().clear();
        GetMyIdeasTask task = new GetMyIdeasTask();
        task.execute();
    }

    private class GetMyIdeasTask extends AsyncTask<Void, Void, ArrayList<Idea>> {

        @Override
        protected ArrayList<Idea> doInBackground(Void... params) {
            if (User.getInstance().getIdeas().size() > 0)
                return User.getInstance().getIdeas();
            DB db = DB.getInstance();
            try {
                User.getInstance().setIdeas(db.getMyIdeas());
                return User.getInstance().getIdeas();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Idea> ideas) {
            super.onPostExecute(ideas);
            pd.dismiss();
            swipeLayout.setRefreshing(false);
            if(ideas != null){
                adapter.setSource(ideas);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pd.dismiss();
        }
    }
}
