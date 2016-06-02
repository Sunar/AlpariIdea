package com.alpari.avia_kos.alpidea.fragments;

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

/**
 * Created by Avia-Kos on 12.05.16.
 */
public class IdeasExpertFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener{
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
        pd.setTitle("Подождите");
        pd.setMessage("Идёт получение списка идей на проверку");
        pd.show();
        GetIdeasExpertTask task = new GetIdeasExpertTask();
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

        //attempt change status if this idea has opening first time
        if(User.getInstance().getId() != DB.ANALITIK_ID) {
            OpenIdeaTask task = new OpenIdeaTask();
            task.execute((Idea) l.getItemAtPosition(position));
        }
    }

    @Override
    public void onRefresh() {
        User.getInstance().getIdeas().clear();
        GetIdeasExpertTask task = new GetIdeasExpertTask();
        task.execute();
    }


    private class GetIdeasExpertTask extends AsyncTask<Void, Void, ArrayList<Idea>> {

        @Override
        protected ArrayList<Idea> doInBackground(Void... params) {
            if (User.getInstance().getIdeas().size() > 0)
                return User.getInstance().getIdeas();
            DB db = DB.getInstance();
            try {
                if(User.getInstance().getId() == DB.ANALITIK_ID)
                    User.getInstance().setIdeas(db.getIdeasForAnalitik());
                else
                    User.getInstance().setIdeas(db.getIdeasForExpert());

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

    private class OpenIdeaTask extends AsyncTask<Idea, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Idea... params) {

            DB db = DB.getInstance();
            try {
                db.expertOpenedIdea(params[0]);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
