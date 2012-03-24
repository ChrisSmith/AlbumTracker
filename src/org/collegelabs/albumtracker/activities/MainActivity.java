package org.collegelabs.albumtracker.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.R;
import org.collegelabs.albumtracker.authenticator.AuthenticatorActivity;
import org.collegelabs.albumtracker.content.AlbumProvider;
import org.collegelabs.albumtracker.fragments.AlbumGrid;
import org.collegelabs.albumtracker.fragments.AlbumGrid.Query;
import org.collegelabs.albumtracker.syncadapter.BackgroundService;
import org.collegelabs.library.utils.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends BaseActivity{

	public static final String BUNDLE_QUERY = "query";
	
	private ViewPager  mViewPager;
	private TabsAdapter mTabsAdapter;
	    
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		
		Log.d(Constants.TAG,"Running with debug logs on: "+BuildConfig.DEBUG);
		
		Query selectedTab = Query.All;
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			String q = extras.getString(BUNDLE_QUERY);
			selectedTab = Query.valueOf(q);
		}
		
		if(BuildConfig.DEBUG) Utils.getInstance().enableStrictMode();
		
		setContentView(R.layout.activity_main);
		
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayShowHomeEnabled(false);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
		final Query[] tabs = {Query.All, Query.New, Query.Starred};
		int selected = 0;
        
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, actionbar, mViewPager);
        
        for (int i=0; i<tabs.length; i++){
        	ActionBar.Tab tab = actionbar.newTab()
        		.setText(tabs[i].toString())
        		.setTag(tabs[i]);
        	
        	if(tabs[i] == selectedTab)
        		selected = i;
            
            mTabsAdapter.addTab(tab);
        }
        
        actionbar.setSelectedNavigationItem(selected);
        
        
        //This is a disk read
        new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
		        //Check to see if at least one account is setup
				AccountManager manager = AccountManager.get(MainActivity.this);
				Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE);
				
				return accounts.length == 0;
			}
			@Override
			public void onPostExecute(Boolean noAccounts){
				if(noAccounts){
					Intent i = new Intent(MainActivity.this, AuthenticatorActivity.class);
					startActivity(i);
				}
			}	
        }.execute();
	}
	
	public void syncAccounts(){		
		AccountManager manager = AccountManager.get(this);
		Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		if(accounts.length == 0){
			startActivity(new Intent(this, AuthenticatorActivity.class));
			return;
		}
		
		for(Account account : accounts){
			ContentResolver.requestSync(account, AlbumProvider.AUTHORITY, new Bundle());			
		}
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		//In the background, cleanup our cached files
		Intent i = new Intent(this,BackgroundService.class);
		i.setAction(BackgroundService.ACTION_CLEANUP_CACHE);
		startService(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(0, Constants.MENU_START_SYNC, 0, "Sync Now");
//		menu.add(0, Constants.MENU_COPY_DB, 0, "Move DB to SDcard");
//		menu.add(0, Constants.MENU_RESTORE_DB, 0, "Restore DB from SDcard");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case Constants.MENU_COPY_DB:
			copyToSDCard();
			return true;
		case Constants.MENU_RESTORE_DB:
			restoreFromSD();
			return true;
		case Constants.MENU_START_SYNC:
			syncAccounts();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//TODO this doesn't work...
	private void restoreFromSD(){
		try {
			String path = Environment.getExternalStorageDirectory().getPath();
			path+="/tmp/"+getPackageName()+"/";
			File sd = new File(path);
			if(!sd.exists())
				sd.mkdirs();

			if (sd.canRead()) {

				String backupDBPath = AlbumProvider.DATABASE_NAME;
				File currentDB = getDatabasePath(AlbumProvider.DATABASE_NAME);
				File backupDB = new File(sd, backupDBPath);
				if (currentDB.exists()) {
					currentDB.delete();	
				}
			
				if(!backupDB.exists()){
					Toast.makeText(this, "Unable find backup", Toast.LENGTH_SHORT).show();
					return;
				}
				
				FileChannel src = new FileInputStream(backupDB).getChannel();
				FileChannel dst = new FileOutputStream(currentDB).getChannel();

				dst.transferFrom(src, 0, src.size());
				dst.force(true);

				src.close();
				dst.close();

				Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
			
				//the content provider needs to reopen the database with the new data,
				//so we need to kill the process?
				//can we just notify the list adapters?
				android.os.Process.killProcess(android.os.Process.myPid());
				
			}else{
				Toast.makeText(this, "Unable to read external", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	//TODO this doesn't work...
	private void copyToSDCard(){
		try {
			String path = Environment.getExternalStorageDirectory().getPath();
			path+="/tmp/"+getPackageName()+"/";
			File sd = new File(path);
			if(!sd.exists())
				sd.mkdirs();

			if (sd.canWrite()) {

				String backupDBPath = AlbumProvider.DATABASE_NAME;
				File currentDB = getDatabasePath(AlbumProvider.DATABASE_NAME);
				File backupDB = new File(sd, backupDBPath);
				
				if(backupDB.exists()){
					backupDB.delete();
				}
				
				if (currentDB.exists()) {
					FileChannel src;
					src = new FileInputStream(currentDB).getChannel();

					FileChannel dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());

					dst.force(true);

					src.close();
					dst.close();

					Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(this, "Database doesn't exist", Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(this, "Unable to write to sdcard", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	
    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     * 
     * - via Jake Wharton
     */
    public static class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, ActionBar.TabListener {
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private int tabsCount = 0;
        
        public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mActionBar = actionBar;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab) {
        	tabsCount++;
            mActionBar.addTab(tab.setTabListener(this));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return tabsCount;
        }

        @Override
        public Fragment getItem(int position) {
        	Bundle args = new Bundle();
        	args.putString(AlbumGrid.BUNDLE_QUERY, mActionBar.getTabAt(position).getTag().toString());
        	Fragment f = new AlbumGrid();
        	f.setArguments(args);
        	return f;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}
    }
}
