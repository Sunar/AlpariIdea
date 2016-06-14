package com.alpari.avia_kos.alpidea.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alpari.avia_kos.alpidea.DB;
import com.alpari.avia_kos.alpidea.models.Idea;
import com.alpari.avia_kos.alpidea.models.IdeaStatus;
import com.alpari.avia_kos.alpidea.R;
import com.alpari.avia_kos.alpidea.models.User;
import com.alpari.avia_kos.alpidea.adapters.SpinnerAdapter;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Avia-Kos on 12.05.16.
 */
public class IdeaDetailsFragment extends Fragment implements View.OnClickListener{


    private Idea idea;
    private SpinnerAdapter adapter;
    private ArrayList<String> ruksList = new ArrayList<>();

    //experts
    private LinearLayout llExpertBlock;
    private Spinner sRuks;
    //

    //ruks
    private LinearLayout llRukBlock;


    @Override
    public void setArguments(Bundle args) {
        this.idea = (Idea)args.get("idea");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_idea_details, container, false);

        ((TextView)v.findViewById(R.id.tvIdeaContent)).setText(idea.getContent());
        ((TextView)v.findViewById(R.id.tvIdeaRes)).setText(idea.getResources());
        ((TextView)v.findViewById(R.id.tvIdeaGoal)).setText(idea.getGoal());
        ((TextView)v.findViewById(R.id.tvIdeaTime)).setText(idea.getRealizTime());


        //initialization the block of project group
        llExpertBlock = (LinearLayout) v.findViewById(R.id.llExpertBlock);
        sRuks = (Spinner) v.findViewById(R.id.sRuks);
        sRuks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    ((Button)llExpertBlock.findViewById(R.id.btnAcceptIdea)).setText(getString(R.string.accept_idea));
                }
                else
                    if (position > 1)
                        ((Button)llExpertBlock.findViewById(R.id.btnAcceptIdea)).setText("Отправить эксперту");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        (llExpertBlock.findViewById(R.id.btnAcceptIdea)).setOnClickListener(this);
        (llExpertBlock.findViewById(R.id.btnRejectIdea)).setOnClickListener(this);

        //initialization the block for dept heads
        llRukBlock = (LinearLayout) v.findViewById(R.id.llRukBlock);

        //if user is expert (from pg), show expert block
        if(User.getInstance().isExpert()){
            showExpertBlock(idea.getStatus().getId());
        }

        //if user is head of some dept., special block will be shown
        if(User.getInstance().isRuk()){
            showRukBlock(idea.getStatus().getId());
        }

        return v;
    }

    @Override
    public void onClick(View v) {
        DecisionIdeaTask task;
        int stId = idea.getStatus().getId();
        int newId = 0;
        switch (v.getId()){
            case R.id.btnRejectIdea:
                if(stId == IdeaStatus.UNDER_CONSIDERING) {
                    if(sRuks.getSelectedItemPosition() == 0) {
                        Toast.makeText(getActivity(), "Выберите эксперта!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    newId = IdeaStatus.REJECTED_BY_EXPERT;
                }
                else if(stId == IdeaStatus.SENT_TO_RUK)
                    newId = IdeaStatus.RUK_DECIDED;
                else if(stId == IdeaStatus.RUK_DECIDED)
                    newId = IdeaStatus.REJECTED_BY_RUK;
                else if(stId == IdeaStatus.IDEA_ACCEPTED)
                    newId = IdeaStatus.REJECTED_ON_DISCUSSION;
                task = new DecisionIdeaTask(newId, (sRuks.getSelectedItemPosition() == 0)? null : (String)sRuks.getSelectedItem());
                task.execute(idea);
                break;
            case R.id.btnAcceptIdea:
                if(stId == IdeaStatus.UNDER_CONSIDERING) {
                    if(sRuks.getSelectedItemPosition() == 0) {
                        Toast.makeText(getActivity(), "Выберите эксперта!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(sRuks.getSelectedItemPosition() == 1)
                        newId = IdeaStatus.IDEA_ACCEPTED;
                    else
                        newId = IdeaStatus.SENT_TO_RUK;
                }
                else if(stId == IdeaStatus.SENT_TO_RUK)
                    newId = IdeaStatus.RUK_DECIDED;
                else if(stId == IdeaStatus.RUK_DECIDED)
                    newId = IdeaStatus.IDEA_ACCEPTED;
                else if(stId == IdeaStatus.IDEA_ACCEPTED)
                    newId = IdeaStatus.ACCEPTED_ON_DISCUSSION;
                task = new DecisionIdeaTask(newId, (sRuks.getSelectedItemPosition() == 0)? null : (String)sRuks.getSelectedItem());
                task.execute(idea);
                break;
            case R.id.btnSendRukDecision:
                SendRukDecisionTask additionalTask = new SendRukDecisionTask(((TextView)llRukBlock.findViewById(R.id.etRukDecision)).getText().toString());
                additionalTask.execute(idea);
                break;
        }
    }

    private void showExpertBlock(int statusId){
        llExpertBlock.setVisibility(View.VISIBLE);
        sRuks.setVisibility(View.GONE);
        (llExpertBlock.findViewById(R.id.llExpertButtons)).setVisibility(View.GONE);
        String s;
        switch (statusId) {
            case IdeaStatus.NOT_CONSIDERED:
            case IdeaStatus.UNDER_CONSIDERING:
                ruksList.add("Выберите эксперта");
                ruksList.add("Без участия эксперта");
                adapter = new SpinnerAdapter(getActivity(), android.R.layout.simple_spinner_item, ruksList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sRuks.setVisibility(View.VISIBLE);
                sRuks.setAdapter(adapter);
                (llExpertBlock.findViewById(R.id.llExpertButtons)).setVisibility(View.VISIBLE);
                GetRuksTask task = new GetRuksTask();
                task.execute();
                break;
            case IdeaStatus.SENT_TO_RUK:
                (llExpertBlock.findViewById(R.id.tvRukDecision)).setVisibility(View.VISIBLE);
                s = "Мнение эксперта: " + idea.getRukDecision();
                ((TextView)llExpertBlock.findViewById(R.id.tvRukDecision)).setText(s);
                break;
            case IdeaStatus.REJECTED_BY_EXPERT:
                s = "Решение принято! Идея отсеяна";
                ((TextView)llExpertBlock.findViewById(R.id.tvDecision)).setText(s);
                (llExpertBlock.findViewById(R.id.tvDecision)).setVisibility(View.VISIBLE);
                break;
            case IdeaStatus.RUK_DECIDED:
                (llExpertBlock.findViewById(R.id.tvRukDecision)).setVisibility(View.VISIBLE);
                s = "Мнение эксперта: " + idea.getRukDecision();
                ((TextView)llExpertBlock.findViewById(R.id.tvRukDecision)).setText(s);
                (llExpertBlock.findViewById(R.id.llExpertButtons)).setVisibility(View.VISIBLE);
                break;
            case IdeaStatus.REJECTED_BY_RUK:
                s = "Решение принято! От идеи отказались";
                ((TextView)llExpertBlock.findViewById(R.id.tvDecision)).setText(s);
                (llExpertBlock.findViewById(R.id.tvDecision)).setVisibility(View.VISIBLE);
                break;
            case IdeaStatus.IDEA_ACCEPTED:
                s = "Решение принято! Идея согласована и отправлена аналитику";
                ((TextView)llExpertBlock.findViewById(R.id.tvDecision)).setText(s);
                (llExpertBlock.findViewById(R.id.tvDecision)).setVisibility(View.VISIBLE);
                if(User.getInstance().getId() == DB.ANALITIK_ID)
                    (llExpertBlock.findViewById(R.id.llExpertButtons)).setVisibility(View.VISIBLE);
                break;
            case IdeaStatus.ACCEPTED_ON_DISCUSSION:
                s = "Идея принята окончательно, начислены баллы";
                ((TextView)llExpertBlock.findViewById(R.id.tvDecision)).setText(s);
                (llExpertBlock.findViewById(R.id.tvDecision)).setVisibility(View.VISIBLE);
                break;
            case IdeaStatus.REJECTED_ON_DISCUSSION:
                s = "Идея в итоге отклонена, начислены баллы";
                ((TextView)llExpertBlock.findViewById(R.id.tvDecision)).setText(s);
                (llExpertBlock.findViewById(R.id.tvDecision)).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showRukBlock(int statusId){
        llRukBlock.setVisibility(View.VISIBLE);
        switch (statusId) {
            case IdeaStatus.SENT_TO_RUK:
                llRukBlock.findViewById(R.id.btnSendRukDecision).setVisibility(View.VISIBLE);
                llRukBlock.findViewById(R.id.btnSendRukDecision).setOnClickListener(this);
                llRukBlock.findViewById(R.id.etRukDecision).setEnabled(true);
                break;
            default:
                llRukBlock.findViewById(R.id.btnSendRukDecision).setVisibility(View.GONE);
                llRukBlock.findViewById(R.id.etRukDecision).setEnabled(false);
                break;
        }
    }

    private class DecisionIdeaTask extends AsyncTask<Idea, Void, Boolean> {
        int statusId;
        String rukName;

        public DecisionIdeaTask(int newStatusId, String rukName){
            statusId = newStatusId;
            this.rukName = rukName;
        }

        @Override
        protected Boolean doInBackground(Idea... params) {

            DB db = DB.getInstance();
            try {
                db.expertPointedIdea(params[0], statusId, rukName);
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            showExpertBlock(statusId);
        }
    }

    private class GetRuksTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                return db.getRuks();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> ruks) {
            super.onPostExecute(ruks);
            ruksList.addAll(ruks);
            adapter.setSource(ruksList);
            adapter.notifyDataSetChanged();
        }

    }

    private class SendRukDecisionTask extends AsyncTask<Idea, Void, Boolean> {
        String opinion;

        public SendRukDecisionTask(String opinion){
            this.opinion = opinion;
        }

        @Override
        protected Boolean doInBackground(Idea... params) {

            DB db = DB.getInstance();
            try {
                db.sendRukDecision(IdeaStatus.RUK_DECIDED, opinion, params[0]);
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            showRukBlock(IdeaStatus.RUK_DECIDED);
        }
    }
}
