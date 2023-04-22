package com.example.readox.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.readox.databinding.ActivityDashboardUserBinding;
import com.example.readox.fragments.BookUserFragment;
import com.example.readox.R;
import com.example.readox.models.ModelCategory;
import com.example.readox.utils.CustomDialogs;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    public ViewPagerAdapter viewPagerAdapter;
    // to show in tabs
    public ArrayList<ModelCategory> categoryArrayList;
    //view binding
    private ActivityDashboardUserBinding binding;

    private CustomDialogs customDialogs;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //navigation view
    private NavigationView nav;
    //actionbar toggle
    private ActionBarDrawerToggle toggle;
    //Drawer layout
    private DrawerLayout drawerLayout;
    //tool bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarRl);
        setSupportActionBar(toolbar);

//        nav=(NavigationView)findViewById(R.id.navMenu);
//        drawerLayout=(DrawerLayout)findViewById(R.id.drawerL);
//        toggle= new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
//        toggle.syncState();
//        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//                switch (item.getItemId())
//                {
//                    case R.id.menu_guide:
//                        Toast.makeText(getApplicationContext(), "ReadOx Guide is Selected", Toast.LENGTH_SHORT).show();
//                        break;
////                    case R.id.menu_donate:
////                        Toast.makeText(getApplicationContext(), "Donate is Selected", Toast.LENGTH_SHORT).show();
////                        break;
//                }
//                drawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            }
//        });

        firebaseAuth = FirebaseAuth.getInstance();
        customDialogs = new CustomDialogs(this);
        checkUser();
        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        //handle logout click
        binding.logoutIVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
        categoryArrayList = new ArrayList<>();
        //load categories from database
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear before adding to list
                categoryArrayList.clear();
                ModelCategory modelAll = new ModelCategory("01","All","",1);
                ModelCategory modelMostViewed = new ModelCategory("02","Most Viewed","",1);
                ModelCategory modelMostDownloaded = new ModelCategory("03","Most Downloaded","",1);

                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getCategory(),
                        ""+modelAll.getUid()),
                        modelAll.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostViewed.getId(),
                        ""+modelMostViewed.getCategory(),
                        ""+modelMostViewed.getUid()),
                        modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostDownloaded.getId(),
                        ""+modelMostDownloaded.getCategory(),
                        ""+modelMostDownloaded.getUid()),
                        modelMostDownloaded.getCategory());

                //refresh list
                viewPagerAdapter.notifyDataSetChanged();
                //load data from firebase
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelCategory model=ds.getValue(ModelCategory.class);
                    //add data to list
                    categoryArrayList.add(model);
                    //add data to view pager adapter
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            ""+model.getId(),
                            ""+model.getCategory(),
                            ""+model.getUid()),model.getCategory());

                    viewPagerAdapter.notifyDataSetChanged();

                }


            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter to view pager
        binding.viewPager.setAdapter(viewPagerAdapter);

    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<BookUserFragment> fragmentList=new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context=context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
        private void addFragment(BookUserFragment fragment,String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null)
        {
            //user not logged in
            startActivity(new Intent(DashboardUserActivity.this,MainActivity.class));
            finish();
        }else{
            //get login details
            String email=firebaseUser.getEmail();
            //set mail in toolbar textview
            binding.subTitleTV.setText(email);
        }
    }

    private void showLogoutDialog(){
        customDialogs.customLogoutScreen(this, new CustomDialogs.OnDialogListener( ) {
            @Override
            public void onDialogYes(Dialog dialog) {
                firebaseAuth.signOut();
                checkUser();
            }

            @Override
            public void onDialogNo(Dialog dialog) {

            }
        });

    }

    private void showExitDialog(){
        customDialogs.customExitDialog(this, new CustomDialogs.OnDialogListener( ) {
            @Override
            public void onDialogYes(Dialog dialog) {

            }

            @Override
            public void onDialogNo(Dialog dialog) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }
}