package com.docd.purefm.activities;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.docd.purefm.R;
import com.docd.purefm.adapters.BookmarksAdapter;
import com.docd.purefm.adapters.BrowserTabsAdapter;
import com.docd.purefm.browser.BrowserFragment;
import com.docd.purefm.browser.Browser.OnNavigateListener;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.PreviewHolder;
import com.docd.purefm.view.SequentalTextView;

public final class BrowserActivity extends FragmentActivity {

    public static final String EXTRA_REQUESTED_PATH = BookmarksActivity.class
            .getName() + "REQUESTED_PATH";

    public static final String TAG_DIALOG = "dialog";
    
    public static final int REQUEST_CODE_SETTINGS = 0;

    private ActionBar actionBar;
    private SequentalTextView title;
    
    BookmarksAdapter bookmarksAdapter;
    
    private ViewPager pager;
    private BrowserTabsAdapter pagerAdapter;

    /**
     * If not null it means we are in GET_CONTENT mode
     */
    private String mimeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browser);
        this.checkIntentAction(getIntent());
        this.initActiobBar();
        this.initView();
        this.checkForPath(getIntent());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        final FragmentManager fm = getFragmentManager();
        final Fragment f = fm.findFragmentByTag(TAG_DIALOG);
        if (f != null) {
            fm.beginTransaction().remove(f).commit();
            fm.executePendingTransactions();
        }
    }
    
    @Override
    public void onLowMemory() {
        PreviewHolder.recycle();
    }
    
    private void initActiobBar() {
        this.actionBar = this.getActionBar();
        this.actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_USE_LOGO);
        
        final View custom = LayoutInflater.from(this).inflate(
                R.layout.activity_browser_actionbar, null);
        this.actionBar.setCustomView(custom);
        
        this.title = (SequentalTextView) custom
                .findViewById(android.R.id.title);
    }
    
    private void initView() {
        this.pager = (ViewPager) this.findViewById(R.id.pager);
        this.pagerAdapter = new BrowserTabsAdapter(this.getSupportFragmentManager());
        this.pager.setAdapter(this.pagerAdapter);
        this.pager.setOffscreenPageLimit(2);
        
        final DrawerLayout drawerLayout = (DrawerLayout) this.findViewById(R.id.drawer);
        drawerLayout.setDrawerListener(new DrawerListener() {
            
            private boolean hadShowHomeAsUp;

            @Override
            public void onDrawerOpened(View arg0) {
                this.hadShowHomeAsUp = (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) == ActionBar.DISPLAY_HOME_AS_UP;
                actionBar.setTitle(R.string.menu_bookmarks);
                actionBar.setDisplayOptions(
                        ActionBar.DISPLAY_SHOW_HOME |
                        ActionBar.DISPLAY_USE_LOGO |
                        ActionBar.DISPLAY_SHOW_TITLE);
            }
            @Override
            public void onDrawerClosed(View arg0) {
                actionBar.setDisplayOptions(
                        ActionBar.DISPLAY_SHOW_HOME |
                        ActionBar.DISPLAY_SHOW_CUSTOM |
                        ActionBar.DISPLAY_USE_LOGO | 
                        (this.hadShowHomeAsUp ? ActionBar.DISPLAY_HOME_AS_UP : 0));
                if (bookmarksAdapter.isModified()) {
                    Settings.saveBookmarks(getApplicationContext(), bookmarksAdapter.getData());
                }
            }

            @Override
            public void onDrawerSlide(View arg0, float arg1) {
            }

            @Override
            public void onDrawerStateChanged(int arg0) {
            }
            
        });
        
        final ListView drawerList = (ListView) this.findViewById(R.id.drawerList);
        drawerList.setAdapter(bookmarksAdapter = new BookmarksAdapter(
                this,Settings.getBookmarks(getApplicationContext())));
    }

    public void invalidateList() {
        this.pagerAdapter.notifyDataSetChanged();
    }
    
    public SequentalTextView getTitleView() {
        return this.title;
    }
    
    public OnNavigateListener getOnNavagationListener() {
        return new OnNavigateListener() {
            
            private final File root = File.listRoots()[0];
            
            @Override
            public void onNavigate(GenericFile path) {
                invalidateOptionsMenu();
            }
            
            @Override
            public void onNavigationCompleted(GenericFile path) {
                title.setFile(path.toFile());
                actionBar.setDisplayHomeAsUpEnabled(!path.toFile().equals(this.root));
                invalidateOptionsMenu();
            }
            
        };
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        this.checkIntentAction(newIntent);
        this.checkForPath(newIntent);
    }
    
    private void setCurrentMimeType() {
        BrowserFragment f = pagerAdapter.getActiveFragment(pager, pager.getCurrentItem());
        if (f != null) {
            f.setMimeType(mimeType);
        }
    }
    
    private void setCurrentPath(GenericFile path) {
        BrowserFragment f = pagerAdapter.getActiveFragment(pager, pager.getCurrentItem());
        if (f != null) {
            f.getBrowser().navigate(path, true);
        }
    }

    private void checkIntentAction(Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_GET_CONTENT)) {
            mimeType = intent.getType();
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "*/*";
            }
            this.setCurrentMimeType();
        }
    }

    private void checkForPath(Intent intent) {
        final Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(EXTRA_REQUESTED_PATH)) {
            final GenericFile requested = PureFMFileUtils.newFile(extras.getString(EXTRA_REQUESTED_PATH));
            //if (requested.exists() && requested.isDirectory()) {
                this.setCurrentPath(requested);
            //}
        }
    }
    
    @Override
    public void onBackPressed() {
        final BrowserFragment f = pagerAdapter.getActiveFragment(pager, pager.getCurrentItem());
        if (f != null) {
            if (!f.onBackPressed()) {
                final AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage(R.string.dialog_quit_message);
                b.setCancelable(true);
                b.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PreviewHolder.recycle();
                        finish();
                    }
                });
                b.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                b.create().show();
            }
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.title.fullScrollRight();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                pagerAdapter.notifyDataSetChanged();
            }
        }
    }
}
