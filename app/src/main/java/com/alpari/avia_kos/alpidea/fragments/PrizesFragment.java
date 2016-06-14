package com.alpari.avia_kos.alpidea.fragments;

/**
 * Created by Avia-Kos on 10.05.16.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.alpari.avia_kos.alpidea.DB;
import com.alpari.avia_kos.alpidea.models.Prize;
import com.alpari.avia_kos.alpidea.models.User;
import com.alpari.avia_kos.alpidea.activities.ProfileActivity;
import com.alpari.avia_kos.alpidea.adapters.PrizesAdapter;
import com.alpari.avia_kos.alpidea.R;

import java.sql.SQLException;
import java.util.ArrayList;

public class PrizesFragment extends ListFragment {

    PrizesAdapter adapter;
    ProgressDialog pd;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pd = new ProgressDialog(getActivity());
        pd.setTitle(getString(R.string.wait));
        pd.setMessage(getString(R.string.get_prizes_process));
        pd.show();
        GetPrizesTask task = new GetPrizesTask();
        task.execute();
        adapter = new PrizesAdapter(getActivity(), R.layout.prizes_item, new ArrayList<Prize>());
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Prize prize = (Prize)l.getItemAtPosition(position);
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        String title = getString(R.string.prize_buying);
        String content = getString(R.string.prize_warning) + prize.getPoint();
        ad.setTitle(title);  // заголовок
        ad.setMessage(content); // сообщение
        ad.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if(User.getInstance().getCurrentPoints() >= prize.getPoint()){
                    BuyPrizeTask task = new BuyPrizeTask();
                    task.execute(prize);
                }
                else Toast.makeText(getActivity(), getString(R.string.point_is_not_enough),
                        Toast.LENGTH_SHORT).show();
            }
        });
        ad.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(getActivity(), getString(R.string.operation_canceled),
                        Toast.LENGTH_SHORT).show();
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(getActivity(), getString(R.string.operation_canceled),
                        Toast.LENGTH_SHORT).show();
            }
        });
        ad.show();
    }

    private class GetPrizesTask extends AsyncTask<Void, Void, ArrayList<Prize>> {

        @Override
        protected ArrayList<Prize> doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                return db.getPrizes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Prize> prizes) {
            super.onPostExecute(prizes);
            pd.dismiss();
            if(prizes != null){
                adapter.setSource(prizes);
                //PrizesFragment.this.prizes = prizes;
                //adapter = new PrizesAdapter(getActivity(), R.layout.prizes_item, prizes);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pd.dismiss();
        }
    }

    private class BuyPrizeTask extends AsyncTask<Prize, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Prize... params) {
            DB db = DB.getInstance();
            try {
                return db.buyPrize(params[0]);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if(success){
                Toast.makeText(getActivity(), getString(R.string.prize_bought), Toast.LENGTH_SHORT).show();
                ((ProfileActivity)getActivity()).new GetPointsStatusesMessagesIdeas().execute();
            }
            else {
                Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pd.dismiss();
        }
    }
}
