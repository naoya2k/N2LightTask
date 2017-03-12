package jp.stargroove.naoya.android.ledwidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

class BatteryStatus extends BroadcastReceiver {
	TextView mStatusText;
	TextView mBatteryText;
	ActivityManager.MemoryInfo mMemoryInfo; 

    int status;
    int health;
    boolean present;
    int level;
    int scale = 1;
//    int icon_small;
    int plugged;
    int voltage;
    int temperature;
    String technology = "";

	public BatteryStatus(TextView status, TextView battery) {
		mBatteryText = battery;
		mStatusText = status;
	}
	
	public void setMemoryInfo(ActivityManager.MemoryInfo memInfo) {
		mMemoryInfo = memInfo;
		refreshTextView();
	}
	public void refreshTextView() {
        String statusString = "unknown";
        switch (status) {
        case BatteryManager.BATTERY_STATUS_CHARGING: statusString = "Charging"; break;
        case BatteryManager.BATTERY_STATUS_DISCHARGING: statusString = "Discharging"; break;
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING: statusString = "not Charging"; break;
        case BatteryManager.BATTERY_STATUS_FULL: statusString = "Full"; break;
        }
        
//        String healthString = "unknown";
//        switch (health) {
//        case BatteryManager.BATTERY_HEALTH_GOOD: healthString = "good"; break;
//        case BatteryManager.BATTERY_HEALTH_OVERHEAT: healthString = "overheat"; break;
//        case BatteryManager.BATTERY_HEALTH_DEAD: healthString = "dead"; break;
//        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: healthString = "voltage"; break;
//        case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: healthString = "unspecified failure"; break;
//        }
        
        String acString = "";
        switch (plugged) {
        case BatteryManager.BATTERY_PLUGGED_AC: acString = ", AC"; break;
        case BatteryManager.BATTERY_PLUGGED_USB: acString = ", USB"; break;
        }

        mBatteryText.setText(String.format("%3d%% (%s)\n", level * 100 / scale, statusString));
        String mes = String.format("%s (%2.1f℃, %1.2fV%s)\n", technology, temperature / 10.0d, voltage / 1000.0d, acString); 
        mes += String.format("availMem: %dk\n", mMemoryInfo.availMem / 1024);
        mStatusText.setText(mes);
	}

	@Override
    public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            health = intent.getIntExtra("health", 0);
            present = intent.getBooleanExtra("present", false);
            level = intent.getIntExtra("level", 0);
            scale = intent.getIntExtra("scale", 0);
//            icon_small = intent.getIntExtra("icon-small", 0);
            plugged = intent.getIntExtra("plugged", 0);
            voltage = intent.getIntExtra("voltage", 0);
            temperature = intent.getIntExtra("temperature", 0);
            technology = intent.getStringExtra("technology");
            
            refreshTextView();
        }
    }
	
}

public class MainActivity extends Activity implements OnClickListener {
	static final String TAG = "Ns Light&Task";

	BatteryStatus mBroadcastReceiver;

    Handler mH = new Handler();
	ListView mListView;
	ToggleButton torchToggleButton;
	TextView mStatusText;
	Camera mCamera;
	ActivityManager mActivityManager;
	PackageManager mPackageManager;
	List<ApplicationInfo> mApplicationList;
	ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
	
	int mThresholdImportance = 398;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        

        mPackageManager = getPackageManager();
        mApplicationList = mPackageManager.getInstalledApplications(0);
    	setContentView(R.layout.main);
        mListView = (ListView) findViewById(R.id.mainListView);
        torchToggleButton = (ToggleButton) findViewById(R.id.torchToggleButton);
        torchToggleButton.setOnClickListener(this);
        
        mBroadcastReceiver = new BatteryStatus ( 
            	(TextView) findViewById(R.id.statusText), 
            	(TextView) findViewById(R.id.batteryText)); 
        	
        
        findViewById(R.id.radio0).setOnClickListener(this);
        findViewById(R.id.radio1).setOnClickListener(this);
        findViewById(R.id.radio2).setOnClickListener(this);

        mActivityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
        
//      	Log.d(TAG,"memoryInfo availMem  :"+memoryInfo.availMem);
//      	Log.d(TAG,"memoryInfo lowMemory :"+memoryInfo.lowMemory);
//      	Log.d(TAG,"memoryInfo threshold :"+memoryInfo.threshold);
    }

    ArrayList<ProcessInfo> mProcesses;
    int mProcessNum;
    Button mLastKillButton = null;

    private void refreshKillButton(Button newKillButton, final ProcessInfo proc) {
    	if (mLastKillButton != null) {
    		mLastKillButton.setVisibility(View.INVISIBLE);
    	}
    	mLastKillButton = newKillButton;
		newKillButton.setVisibility(View.VISIBLE);
		newKillButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String packageName = proc.getPackageName();
//					mActivityManager.killBackgroundProcesses(packageName);
					mActivityManager.restartPackage(packageName);
				} catch (Exception e) {
					Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
				}
				mH.post(new Runnable() { public void run() {refreshList();}});
			}
			
		});
    }
    private void refreshListView() {
    	ListAdapter la = new ListAdapter() {
			@Override
			public int getCount() { return mProcessNum; }
			@Override
			public Object getItem(int position) { return mProcesses.get(position); }

			@Override
			public long getItemId(int position) { return position; }

			@Override
			public int getItemViewType(int position) { return 0; }

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				final ProcessInfo proc = mProcesses.get(position);
				final int importance = proc.getImportance();
				if (v == null) { 
					v = View.inflate(MainActivity.this, R.layout.processitem, null);
				}
				final TextView pid = (TextView) v.findViewById(R.id.pi_pidtext); 
				final TextView name = (TextView) v.findViewById(R.id.pi_nametext);
				final TextView pname = (TextView) v.findViewById(R.id.pi_packagetext);
				final Button killButton = (Button) v.findViewById(R.id.button1);
				
				if (mListView.getSelectedItemPosition() == position) {
					refreshKillButton(killButton, proc);
				} else {
					killButton.setVisibility(View.INVISIBLE);
				}
				pid.setText(""+ proc.getPid());
				name.setText(proc.getName());
				
				if (proc.isMalware()) {
					pname.setText(proc.getPackageName() + "*");
				} else {
					pname.setText(proc.getPackageName());
				}

				if (importance >= 400) {
					name.setTextColor(0xffffffff);
				} else if (importance >= 300) {
					name.setTextColor(0xffffdd88);
				} else if (importance >= 100) {
					name.setTextColor(0xff88ddff);
				} else {
					name.setTextColor(0xffffaa44);
				}
				
				return v;
			}

			@Override
			public int getViewTypeCount() { return 1; }

			@Override public boolean hasStableIds() {	return false; }
			@Override public boolean isEmpty() { return false; }
			@Override public boolean isEnabled(int position) { return true; }
			@Override public void registerDataSetObserver(DataSetObserver observer) {}
			@Override public void unregisterDataSetObserver(DataSetObserver observer) {}
			@Override public boolean areAllItemsEnabled() { return true;}
    	};
    	mListView.setAdapter(la);
    	mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position, long id) {
				final Button killButton = (Button) v.findViewById(R.id.button1);
				refreshKillButton(killButton, mProcesses.get(position));
			}
		});
    }

    private void refreshList() {
    	List<ActivityManager.RunningAppProcessInfo> rapis = mActivityManager.getRunningAppProcesses();
        mProcesses = new ArrayList<ProcessInfo>(0);
    	java.util.Collections.sort(rapis, new Comparator<ActivityManager.RunningAppProcessInfo>() {
			@Override
			public int compare(RunningAppProcessInfo a, RunningAppProcessInfo b) { 	return a.pid - b.pid; }
    	});
    	
		for (RunningAppProcessInfo rapi: rapis) {
			if (rapi.importance < mThresholdImportance) continue;
			String b = rapi.processName;
	    	for (ApplicationInfo ai: mApplicationList) {
	    		String a = ai.packageName;
    			if (a.equals(b)) {
    				mProcesses.add(new ProcessInfo(rapi, ai, mPackageManager));    				
    				break;
    			}
    		}
    	}
    	
		
    	mProcessNum = mProcesses.size();
    	Log.d(TAG, "process num: " + mProcessNum);
    	refreshListView();

      	//���������̎擾
      	mActivityManager.getMemoryInfo(memoryInfo);
      	mBroadcastReceiver.setMemoryInfo(memoryInfo);
      	mBroadcastReceiver.refreshTextView();
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	refreshList();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
    	super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        // 	turn(false);
    	java.lang.System.exit(0);
    }

    
    /**
     * ���C�g�̃g�O���{�^���̏���
     */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (v == torchToggleButton) {
			boolean torch = handleLed(torchToggleButton.isChecked());
			torchToggleButton.setChecked(torch);
		}
		switch (id) {
		case R.id.radio0:
			mThresholdImportance = 398;
			refreshList();
			break;
		case R.id.radio1:
			mThresholdImportance = 298;
			refreshList();
			break;
		case R.id.radio2:
			mThresholdImportance = -99;
			refreshList();
			break;
			
			
		}
		
	}
	/**
	 * ���C�g�̃R���g���[�����s��
	 * @param t true��Camera�I�[�v��&���C�g�_���Bfalse�Ń��C�g����&Camera�����[�X
	 * @return LED��������true, ���Ȃ�������false
	 */
	private boolean handleLed(boolean t) {
		boolean led = false;
		try {
			if (t) {
				if (mCamera == null) {
					mCamera = Camera.open();
				}
				Camera.Parameters params = mCamera.getParameters();
				params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				mCamera.setParameters(params);
				// LED�󋵂��擾����B
				params = mCamera.getParameters();
				if (params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
					led = true;
				}
			} else {
				if (mCamera != null) {
					Camera.Parameters params = mCamera.getParameters();  
					params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					mCamera.setParameters(params);
					mCamera.release();
					mCamera = null;
				}
			}
		} catch (Throwable e) {}
		return led;
	}
	
	/*
	 * ���j���[���� 
	 */
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && shellView){
			menuAction(MENU_ID_BACK);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	
	private boolean shellView = false;
	static final private int MENU_ID_PS = 1;
	static final private int MENU_ID_MEMINFO = 2;
	static final private int MENU_ID_BACK = 3;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ���j���[�A�C�e����ǉ����܂�
//        menu.add(Menu.NONE, MENU_ID_TOP, Menu.NONE, "Top");
        menu.add(Menu.NONE, MENU_ID_MEMINFO, Menu.NONE, "dumpsys meminfo");
        menu.add(Menu.NONE, MENU_ID_PS, Menu.NONE, "ps");
        menu.add(Menu.NONE, MENU_ID_BACK, Menu.NONE, "back");
        return super.onCreateOptionsMenu(menu);
    }
    // �I�v�V�������j���[���\�������x�ɌĂяo����܂�
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(MENU_ID_PS).setVisible(!shellView);
        menu.findItem(MENU_ID_MEMINFO).setVisible(!shellView);
        menu.findItem(MENU_ID_BACK).setVisible(shellView);
        return super.onPrepareOptionsMenu(menu);
    }
    // �I�v�V�������j���[�A�C�e�����I�����ꂽ���ɌĂяo����܂�
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	menuAction(item.getItemId());
    	return true;
    }
    private void menuAction(int menuId) {
        TextView tv =(TextView) this.findViewById(R.id.psText); 
        ScrollView sv =(ScrollView) this.findViewById(R.id.psScrollView); 
       	shellView = !shellView;
       	if (shellView) {
       		this.findViewById(R.id.linearLayout1).setVisibility(View.GONE);
       		this.findViewById(R.id.mainListView).setVisibility(View.GONE);
       		sv.setVisibility(View.VISIBLE);
       		if (menuId == MENU_ID_MEMINFO) {
       			tv.setText(Shell.run("dumpsys meminfo"));
       		} else {
       			tv.setText(Shell.run("ps"));
       		}
       	} else {
       		this.findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
       		this.findViewById(R.id.mainListView).setVisibility(View.VISIBLE);
       		sv.setVisibility(View.GONE);
       	}
    }
}

class Shell {
	private static String out;
	private static String err;
    private static int rval;

    static private Thread newStreamThread(final StringBuilder out, final java.io.InputStream in) {
        Thread t = new Thread() {
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String ls_1 = null;
                try {
                    while ((ls_1 = br.readLine()) != null) {
                        out.append(ls_1).append('\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try { br.close(); } catch (IOException e) {}
                }
            }
        };
        t.start();
    	return t;
    }
    static public String run(String cmd) {
	    try { 
	        final Process m_process = Runtime.getRuntime().exec(cmd);
	        final StringBuilder sbread = new StringBuilder();
	        final StringBuilder sberr = new StringBuilder();
	        Thread tout = newStreamThread(sbread, m_process.getInputStream());
		    Thread terr = newStreamThread(sberr, m_process.getInputStream());
	    	for (int i = 0; i < 20 && tout.isAlive(); i++) {
        		Thread.sleep(50);
	    	}
            if (tout.isAlive()) tout.interrupt();
            if (terr.isAlive()) terr.interrupt();

            out = sbread.toString();
            err = sberr.toString();
            return out + err;
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "system.exec error :" + e.getMessage());
            return null;
        }
    }

}