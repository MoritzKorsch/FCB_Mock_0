package de.evolutionid.fcbmock0;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import de.evolutionid.fcbmock0.fragments.PointsFragment;
import de.evolutionid.fcbmock0.fragments.ScanFragment;


public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    ViewPagerAdapter adapter;
    NfcAdapter nfcAdapter;
    Tag tag;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up all views and fragments
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();
        adapter = new ViewPagerAdapter(fragmentManager);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //Initialize the NFC component
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        viewPager.setCurrentItem(1);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter.addFragment(new ScanFragment(), "SCAN");
        adapter.addFragment(new PointsFragment(), "POINTS");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            if (fragment != null && title != null) {
                mFragmentList.add(fragment);
                mFragmentTitleList.add(title);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    /**
     * Handles new intents (generally by scanning an NFC tag).
     *
     * @param intent the discovered intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        //Locally store the tag as object
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        //If intent is a NFC tag
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

            // Move view to ScanFragment
            //viewPager.setCurrentItem(0, true);

            //get the message
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            // Pass intent or its data to the fragment's method
            Bundle forFragment = new Bundle();
            forFragment.putParcelable("tag", tag);
            forFragment.putParcelableArray("message", parcelables);

            //ScanFragment scanFragment = (ScanFragment) fragmentManager.findFragmentByTag("SCAN");
            ScanFragment scanFragment = (ScanFragment) adapter.getItem(0);
            scanFragment.setTag(tag);
            scanFragment.processNfcIntent(forFragment);
        }
    }


    /**
     * Gets called when app is active (in foreground).
     */
    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatchSystem();
    }

    /**
     * Gets called when focus is lost (e.g. user returns to the home screen).
     */
    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }

    /**
     * Sets the app to an intent-listening 'mode', so all of the tag discoveries get dispatched to it.
     */
    private void enableForegroundDispatchSystem() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    /**
     * Stops the app from listening for NFC intents.
     * This saves battery and CPU usage.
     */
    private void disableForegroundDispatchSystem() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    public void transferPoints(int pointsGained) {
        PointsFragment pointsFragment = (PointsFragment) adapter.getItem(1);
        pointsFragment.addPoints(pointsGained);
    }
}
