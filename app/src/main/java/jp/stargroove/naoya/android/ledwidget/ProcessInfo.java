package jp.stargroove.naoya.android.ledwidget;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class ProcessInfo {
    private static final String malComponents[] = {
		"com.applogsdk.ui.WebDialog"
	};

    String mMalName = "";
	boolean mIsMalware;
	String mPackageName;
	String mLabel;
	ApplicationInfo mApplicationInfo;
	int mPid;
	int mImportance;
	
	public ProcessInfo(ActivityManager.RunningAppProcessInfo rapi, 
				ApplicationInfo ai, PackageManager pm) {
		mImportance = rapi.importance;
		mPackageName = ai.packageName;
		mApplicationInfo = ai;
		mPid = rapi.pid;
		mLabel = (String) (pm.getApplicationLabel(ai));

		// check malware
		for (String malComponentName:malComponents) {
			try {
				pm.getActivityInfo(
						new ComponentName(mPackageName, malComponentName), 0
				);
				mIsMalware = true;
				mMalName = malComponentName;
			} catch (Exception e) {
			}
		}

	}
	
	public String getMalComponentName() {
		return mMalName;
	}
	
	public int getImportance() {
		return mImportance;
	}
	
	public String getPackageName() {
		return mPackageName;
	}
	public int getPid() {
		return mPid;
	}
	
	public String getName() {
		return mLabel;
	}

	public boolean isMalware() {
		return mIsMalware;
	}
}
