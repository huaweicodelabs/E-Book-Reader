/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.view.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.huawei.agconnect.applinking.AGConnectAppLinking;
import com.huawei.agconnect.applinking.AppLinking;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.elibri.R;
import com.huawei.elibri.java.SavedPreference;
import com.huawei.elibri.java.iap.SubscriptionUtils;
import com.huawei.elibri.java.interfaces.DBOpenInterface;
import com.huawei.elibri.java.service.model.Profile;
import com.huawei.elibri.java.service.repository.CloudDb;
import com.huawei.elibri.java.service.repository.CloudDbQueries;
import com.huawei.elibri.java.utility.Constants;
import com.huawei.elibri.java.utility.Util;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.entity.OrderStatusCode;

import static com.huawei.elibri.java.utility.Util.isOnline;

/**
 * Base activity for all the fragments and navigation drawer
 *
 * @author: lWX916345
 * @since: 02-11-2020
 */
public class MainActivity extends AppCompatActivity {
    /**
     * To navigate between drawer classes
     */
    public NavController navController;
    /**
     * For drawer menu
     */
    public DrawerLayout drawer;
    /**
     * Cloud db zone object
     */
    protected CloudDBZone mCloudDBZone;
    private String agcLink = null;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initializeDrawer();
        isOnline(getApplicationContext());
        createAppLinking();
    }

    /**
     * Initializes drawer
     */
    private void initializeDrawer() {
        AGConnectAppLinking.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_my_book, R.id.nav_bookmark, R.id.nav_premium,
                R.id.nav_profile, R.id.nav_invite, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        setUserData(navigationView);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination,
                @Nullable Bundle arguments) {
                switch (destination.getId()) {
                    case R.id.nav_my_book:
                    case R.id.nav_bookmark:
                    case R.id.nav_premium:
                    case R.id.nav_profile:
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        break;
                    default:
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        break;
                }
            }
        });

        navigationView.getMenu().findItem(R.id.nav_invite).setOnMenuItemClickListener(menuItem -> {
            shareLink();
            return true;
        });
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(menuItem -> {
            signOut();
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Initialize app linking
     */
    private void createAppLinking() {
        AppLinking.Builder builder =
                new AppLinking.Builder()
                        .setUriPrefix(Constants.DOMAIN_URI_PREFIX)
                        .setDeepLink(Uri.parse(Constants.DEEP_LINK))
                        .setAndroidLinkInfo(new AppLinking.AndroidLinkInfo.Builder().build());
        builder.buildShortAppLinking()
                .addOnSuccessListener(
                        shortAppLinking -> {
                            agcLink = shortAppLinking.getShortUrl().toString();
                        })
                .addOnFailureListener(
                        exception -> {
                            Toast.makeText(getApplicationContext(), "exception occured", Toast.LENGTH_SHORT).show();
                        });
    }

    /**
     * Sets user data in Navigation drawer
     *
     * @param navigationView : NavigationView
     */
    private void setUserData(NavigationView navigationView) {
        TextView tvUsrName = navigationView.getHeaderView(0).findViewById(R.id.tvUserName);
        TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvUserEmail);
        tvUsrName.setText(SavedPreference.getInstance(getApplicationContext()).getName());
        tvEmail.setText(SavedPreference.getInstance(getApplicationContext()).fetchEmailId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    private void signOut() {
        if (AGConnectAuth.getInstance().getCurrentUser() != null) {
            AGConnectAuth.getInstance().signOut();
            SavedPreference.getInstance(getApplicationContext()).logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            if (AGConnectAuth.getInstance().getCurrentUser() == null) {
                this.getSharedPreferences("com.agc.fb", Context.MODE_PRIVATE).edit().clear().apply();
                this.getSharedPreferences("AuthStatePreference", Context.MODE_PRIVATE).edit().clear().apply();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLink() {
        if (agcLink != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, agcLink);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (resultCode == Activity.RESULT_OK) {
                int purchaseResult = SubscriptionUtils.getPurchaseResult(MainActivity.this, data);
                if (purchaseResult == OrderStatusCode.ORDER_STATE_SUCCESS) {
                    Toast.makeText(MainActivity.this, R.string.pay_success, Toast.LENGTH_SHORT).show();
                    upsertData();
                    return;
                }
                if (purchaseResult == OrderStatusCode.ORDER_STATE_CANCEL) {
                    Toast.makeText(MainActivity.this, R.string.cancel, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, R.string.pay_fail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.cancel, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialize DB
     *
     * @param dbOpenInterface interface for fetching DB result
     */
    public void initializeDb(DBOpenInterface dbOpenInterface) {
        if (CloudDb.getInstance().getCloudDBZone() != null) {
            if (dbOpenInterface != null) {
                dbOpenInterface.success(CloudDb.getInstance().getCloudDBZone());
            }
        } else {
            CloudDb.getInstance().createObjectType();
            CloudDb.getInstance()
                    .openCloudDBZone(
                            new DBOpenInterface() {
                                @Override
                                public void success(CloudDBZone cloudDBZone) {
                                    if (dbOpenInterface != null) {
                                        dbOpenInterface.success(cloudDBZone);
                                    }
                                }

                                @Override
                                public void failure() {
                                }
                            });
        }
    }

    private void upsertData() {
        SavedPreference.getInstance(getApplicationContext()).isSubscribed(true);
        initializeDb(new DBOpenInterface() {
            @Override
            public void success(CloudDBZone cloudDBZone) {
                Util.stopProgressBar();
                mCloudDBZone = cloudDBZone;
                checkIfUserExists();
            }

            @Override
            public void failure() {
                Util.stopProgressBar();
            }
        });
    }

    /**
     * Checks if user data exists in DB or not
     *
     */
    private void checkIfUserExists() {
        if (mCloudDBZone == null) {
            Log.w("TAG", "CloudDBZone is null, try re-open it");
            return;
        }
        SavedPreference sp = SavedPreference.getInstance(getApplicationContext());
        Task<CloudDBZoneSnapshot<Profile>> queryTask = CloudDbQueries.getInstance().getUserProfile(sp, mCloudDBZone);
        queryTask
                .addOnSuccessListener(
                        snapshot -> {
                            Profile profile=processProfileQueryResult(snapshot);
                            profile.setSubscription(true);
                            Task<Integer> upsertTask = mCloudDBZone.executeUpsert(profile);
                            upsertTask
                                    .addOnSuccessListener(cloudDBZoneResult -> {
                                        Log.d("TAG", "Profile data added successfully");
                                    })
                                    .addOnFailureListener(
                                            e -> {
                                                Log.d("TAG", "addOnFailureListener for profile");
                                            });
                            Log.d("TAG", "Fetch user profile data");
                        })
                .addOnFailureListener( e -> {
                    Log.d("TAG", "addOnFailureListener");
                });
    }

    /**
     * Returns user Profile
     *
     * @param snapshot : Cloud DB result
     * @return user Profile
     */
    private Profile processProfileQueryResult(CloudDBZoneSnapshot<Profile> snapshot) {
        CloudDBZoneObjectList<Profile> bookInfoCursor = snapshot.getSnapshotObjects();
        Profile interest = null;
        try {
            while (bookInfoCursor.hasNext()) {
                interest = bookInfoCursor.next();
            }
        } catch (AGConnectCloudDBException e) {
            Log.w("TAG", "processQueryResult: AGConnectCloudDBException");
        } finally {
            snapshot.release();
        }
        return interest;
    }

}
