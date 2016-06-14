package com.alpari.avia_kos.alpidea.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.alpari.avia_kos.alpidea.DB;
import com.alpari.avia_kos.alpidea.R;
import com.alpari.avia_kos.alpidea.models.User;
import com.alpari.avia_kos.alpidea.fragments.IdeasExpertFragment;
import com.alpari.avia_kos.alpidea.fragments.MessagesFragment;
import com.alpari.avia_kos.alpidea.fragments.MyIdeasFragment;
import com.alpari.avia_kos.alpidea.fragments.PrizesFragment;
import com.alpari.avia_kos.alpidea.fragments.SendIdeaFragment;

import java.sql.SQLException;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private boolean doubleBackToExitPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setStartNavigationView();

        setStartFragment();

    }

    private void setStartFragment(){
        //show my ideas in first load
        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        String title;
        if(User.getInstance().isExpert() || User.getInstance().isRuk()){
            title = getString(R.string.title_expert_ideas);
            fTrans.replace(R.id.flContent, new IdeasExpertFragment());
        }
        else {
            title = getString(R.string.title_my_ideas);
            fTrans.replace(R.id.flContent, new MyIdeasFragment());
        }

        fTrans.commit();

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setStartNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set nickname in profile bar
        View header = navigationView.getHeaderView(0);
        TextView text = (TextView) header.findViewById(R.id.tvName);
        text.setText(User.getInstance().getLogin());

        //get points to show and statuses
        GetPointsStatusesMessagesIdeas task = new GetPointsStatusesMessagesIdeas();
        task.execute();

        //hide part of menu if user is not expert
        if(User.getInstance().isExpert() || User.getInstance().isRuk()) {
            navigationView.getMenu().findItem(R.id.header_menu_ideas).setVisible(false);
        }
        else {
            navigationView.getMenu().findItem(R.id.header_menu_expert).setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce  || getSupportFragmentManager().getBackStackEntryCount() != 0) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.repeat_exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        String title = getString(R.string.app_name);

        int id = item.getItemId();

        if (id == R.id.messages) {
            title = getString(R.string.title_messages);
            fragment = new MessagesFragment();
        } else if (id == R.id.prizes) {
            title = getString(R.string.title_prizes);
            fragment = new PrizesFragment();
        } else if (id == R.id.my_ideas) {
            title = getString(R.string.title_my_ideas);
            fragment = new MyIdeasFragment();
        } else if (id == R.id.send_idea) {
            title = getString(R.string.title_send_idea);
            fragment = new SendIdeaFragment();
        } else if (id == R.id.exit) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            ProfileActivity.this.startActivity(intent);
            User.getInstance().exit();
            finish();
        } else if (id == R.id.ideas_expert) {
            title = getString(R.string.title_expert_ideas);
            fragment = new IdeasExpertFragment();
        }

        if(fragment != null){
            fTrans.replace(R.id.flContent, fragment);
            fTrans.commit();
        }
        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class GetPointsStatusesMessagesIdeas extends AsyncTask<Void, Void, Integer> {

        private Menu menu = navigationView.getMenu();
        private User user = User.getInstance();
        @Override
        protected Integer doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                db.getListOfStatuses();
                user.setMessages(db.getMessages());
                user.setIdeas((user.isExpert()||user.isRuk())? db.getIdeasForExpert() : db.getMyIdeas());
                return db.getMyPoints();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Integer points) {
            super.onPostExecute(points);
            user.setCurrentPoints(points);
            MenuItem miPoints = menu.findItem(R.id.infoMyPoints);
            miPoints.setTitle(getString(R.string.title_points) + ": " + points);

            MenuItem miMessages = menu.findItem(R.id.messages);
            int newMsg = user.newMessagesCount();
            miMessages.setTitle((newMsg == 0)? getString(R.string.title_messages) : getString(R.string.title_messages) + " (" + newMsg + ")");

            if(!user.isExpert() && !user.isRuk())
                return;

            int newIdeas = user.newIdeasCount();
            MenuItem miIdea = menu.findItem(R.id.ideas_expert);
            miIdea.setTitle((newIdeas == 0)? getString(R.string.title_expert_ideas) : getString(R.string.title_expert_ideas) + " (" + newIdeas + ")");
        }

    }
}
