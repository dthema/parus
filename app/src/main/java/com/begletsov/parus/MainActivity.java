package com.begletsov.parus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.begletsov.parus.ui.account.AccountFragment;
import com.begletsov.parus.ui.chat.ChatFragment;
import com.begletsov.parus.ui.communication.CommunicationFragment;
import com.begletsov.parus.ui.home.HomeFragment;
import com.begletsov.parus.viewmodels.ServiceViewModel;
import com.begletsov.parus.viewmodels.TTSViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private ServiceViewModel serviceViewModel;
    private TTSViewModel TTS;
    private BottomNavigationView navView;
    private Fragment fragmentHome;
    private Fragment fragmentCommunication;
    private Fragment fragmentAccount;
    private Fragment fragmentChat;
    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment active;
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment tmpActive = active;
            boolean choose = false;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fm.beginTransaction().hide(active).show(fragmentHome).commit();
                    active = fragmentHome;
                    choose = true;
                    break;
                case R.id.navigation_communication:
                    fm.beginTransaction().hide(active).show(fragmentCommunication).commit();
                    active = fragmentCommunication;
                    choose = true;
                    break;
                case R.id.navigation_account:
                    fm.beginTransaction().hide(active).show(fragmentAccount).commit();
                    active = fragmentAccount;
                    choose = true;
                    break;
                case R.id.navigation_chat:
                    fm.beginTransaction().hide(active).show(fragmentChat).commit();
                    active = fragmentChat;
                    choose = true;
                    break;
            }
            if (choose) {
                if (active != tmpActive)
                    fm.beginTransaction().addToBackStack(tmpActive.getTag()).commit();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        if (fm.getBackStackEntryCount() > 0) {
            fm.beginTransaction().hide(active).commit();
            active = fm.findFragmentByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());
            fm.beginTransaction().show(active).commit();
            Class<? extends Fragment> aClass = active.getClass();
            if (HomeFragment.class.equals(aClass)) {
                navView.setSelectedItemId(R.id.navigation_home);
            } else if (CommunicationFragment.class.equals(aClass)) {
                navView.setSelectedItemId(R.id.navigation_communication);
            } else if (AccountFragment.class.equals(aClass)) {
                navView.setSelectedItemId(R.id.navigation_account);
            } else if (ChatFragment.class.equals(aClass)) {
                navView.setSelectedItemId(R.id.navigation_chat);
            }
        }
        super.onBackPressed();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(ServiceViewModel.class);
        TTS = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(TTSViewModel.class);
        serviceViewModel.startWorkService();
        Intent intent = getIntent();
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (savedInstanceState == null) {
            fragmentHome = new HomeFragment();
            fragmentCommunication = new CommunicationFragment();
            fragmentAccount = new AccountFragment();
            fragmentChat = new ChatFragment();
            active = fragmentHome;
            fm.beginTransaction().add(R.id.fragment_container, fragmentChat, "4").hide(fragmentChat).commit();
            fm.beginTransaction().add(R.id.fragment_container, fragmentAccount, "3").hide(fragmentAccount).commit();
            fm.beginTransaction().add(R.id.fragment_container, fragmentCommunication, "2").hide(fragmentCommunication).commit();
            fm.beginTransaction().add(R.id.fragment_container, fragmentHome, "1").hide(fragmentHome).show(fragmentHome).commit();
        } else {
            fragmentHome = fm.findFragmentByTag("1");
            fragmentCommunication = fm.findFragmentByTag("2");
            fragmentAccount = fm.findFragmentByTag("3");
            fragmentChat = fm.findFragmentByTag("4");
            setVisibleFragment();
        }
        if (intent.getBooleanExtra("fromNotification", false)) {
            intent.putExtra("fromNotification", false);
            fm.beginTransaction().hide(active).show(fragmentChat).commit();
            active = fragmentChat;
            navView.setSelectedItemId(R.id.navigation_chat);
        }
    }

    private void setVisibleFragment() {
        assert fragmentHome != null;
        if (!fragmentHome.isHidden()) {
            fm.beginTransaction().hide(fragmentHome).commit();
            fm.beginTransaction().hide(fragmentCommunication).commit();
            fm.beginTransaction().hide(fragmentAccount).commit();
            fm.beginTransaction().hide(fragmentChat).show(fragmentHome).commit();
            active = fragmentHome;
            navView.setSelectedItemId(R.id.navigation_home);
        }
        assert fragmentCommunication != null;
        if (!fragmentCommunication.isHidden()) {
            fm.beginTransaction().hide(fragmentHome).commit();
            fm.beginTransaction().hide(fragmentCommunication).commit();
            fm.beginTransaction().hide(fragmentAccount).commit();
            fm.beginTransaction().hide(fragmentChat).show(fragmentCommunication).commit();
            active = fragmentCommunication;
            navView.setSelectedItemId(R.id.navigation_communication);
        }
        assert fragmentAccount != null;
        if (!fragmentAccount.isHidden()) {
            fm.beginTransaction().hide(fragmentHome).commit();
            fm.beginTransaction().hide(fragmentCommunication).commit();
            fm.beginTransaction().hide(fragmentAccount).commit();
            fm.beginTransaction().hide(fragmentChat).show(fragmentAccount).commit();
            active = fragmentAccount;
            navView.setSelectedItemId(R.id.navigation_account);
        }
        assert fragmentChat != null;
        if (!fragmentChat.isHidden()) {
            fm.beginTransaction().hide(fragmentHome).commit();
            fm.beginTransaction().hide(fragmentCommunication).commit();
            fm.beginTransaction().hide(fragmentAccount).commit();
            fm.beginTransaction().hide(fragmentChat).show(fragmentChat).commit();
            active = fragmentChat;
            navView.setSelectedItemId(R.id.navigation_chat);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        serviceViewModel.startOnlineService();
    }

    @Override
    protected void onPause() {
        serviceViewModel.startOnlineService();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        TTS.destroy();
        serviceViewModel.destroy();
        super.onDestroy();
    }
}