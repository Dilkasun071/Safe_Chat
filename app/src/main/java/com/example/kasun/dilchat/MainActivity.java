package com.example.kasun.dilchat;

import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        //If There is a Current User
        if(mAuth.getCurrentUser() != null){
            //mUserDatabase => User
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                //case 0:
                //    requestFragment reqf = new requestFragment();
                //    return reqf;
                case 0:
                    chatFragment reqf1 = new chatFragment();
                    return reqf1;
                case 1:
                    userFragment reqf2 = new userFragment();
                    return reqf2;
                default:
                    return null;
            }
        }
        @Override
        public int getCount() {
            return 2    ;
        }
    }

    //Start
    private FirebaseAuth mAuth;
    //private Toolbar mtoolbar;
    private Button btn1,btn2,btn3;

    @Override
    protected void onStop() {
        super.onStop();
        //Current User
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //If Current User Exits and Stop=> Time added
        if(currentUser != null){
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //If There is a Current User
        if(currentUser == null){
            sendtoStart();
        }else{
           mUserDatabase.child("online").setValue("true");
        }
    }
    //Send To Start Activity
    private void sendtoStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    //for Mainmenu Bar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
    //for MainMenu Action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout){
            FirebaseAuth.getInstance().signOut();
            sendtoStart();
        }
        if(item.getItemId() == R.id.main_account_setting){
            Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(settingIntent);
        }
        if(item.getItemId() == R.id.main_all_users){
            Intent settingIntent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(settingIntent);
        }
        return true;
    }
}
