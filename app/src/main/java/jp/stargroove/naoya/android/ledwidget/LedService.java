package jp.stargroove.naoya.android.ledwidget;

import android.app.PendingIntent;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;


public class LedService extends Service {
	static final String TAG = MainActivity.TAG;
	Camera mCamera;
	class BR extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			android.util.Log.d(TAG, "ServiceBR onReceive");
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			handleLed(false);
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
		}
        // �T�[�r�X�̋N��
        Intent it = new Intent(context, LedService.class);
        context.startService(it);
	}
	}

	/**
	 * ���C�g��Ԃ��擾
	 * @return LED��������true, ���Ȃ�������false
	 */
	private boolean getLedState() {
		if (mCamera == null) { return false; }
		try {
			return mCamera.getParameters().getFlashMode().equals(
						Camera.Parameters.FLASH_MODE_TORCH);
		} catch (Throwable t) {
			return false;
		}
	}
	
	/**
	 * ���C�g�̃R���g���[�����s��
	 * @param t true��Camera�I�[�v��&���C�g�_���Bfalse�Ń��C�g����&Camera�����[�X
	 */
	private void handleLed(boolean t) {
		try {
			if (t) {
				if (mCamera == null) { mCamera = Camera.open(); }
				Camera.Parameters params = mCamera.getParameters();
				params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				mCamera.setParameters(params);
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
	}

	
	@Override
	public IBinder onBind(Intent intent) { return null; }

    private final String BUTTON_CLICK_ACTION = "BUTTON_CLICK_ACTION";
    
    boolean mLedState = false; 
    BroadcastReceiver bReceiver;
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	handleLed(false); // �T�[�r�X�I�����ɂ�LED�����Ȃ��ƁB
    	android.util.Log.d(TAG, "service onDestroy");
    	if (bReceiver != null) {
    		unregisterReceiver(bReceiver);
    		bReceiver = null;
    	}
    }
    

    @Override
    public void onStart(Intent intent, int startId) {
    	super.onStart(intent, startId);

    	android.util.Log.d(TAG, "service onStart");
    	
    	if (bReceiver == null) {
    		bReceiver = new BR();
    		IntentFilter iFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
    		iFilter.addAction(Intent.ACTION_SCREEN_OFF);
    		registerReceiver(bReceiver, iFilter);
    	}

    	
        // �{�^���������ꂽ���ɔ��s�����C���e���g����������
        Intent buttonIntent = new Intent();
        buttonIntent.setAction(BUTTON_CLICK_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, buttonIntent, 0);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widgetlayout);
        remoteViews.setOnClickPendingIntent(R.id.wi_torchToggleImage, pendingIntent);
 
        // �{�^���������ꂽ���ɔ��s���ꂽ�C���e���g�̏ꍇ�͕�����ύX����
        if (BUTTON_CLICK_ACTION.equals(intent.getAction())) {
        	handleLed(!mLedState);
        	// void setImageViewResource(int viewId, int srcId) ���g���΂����Ƃ���
        }
    	mLedState = getLedState();
//    	remoteViews.setTextViewText(R.id.wi_torchToggleButton, 
//        		  mLedState ? "LED\n(on)" : "LED\n(off)");
    	if (mLedState) {
    		remoteViews.setImageViewResource(R.id.wi_torchToggleImage, R.drawable.widgeton);
    	} else {
    		remoteViews.setImageViewResource(R.id.wi_torchToggleImage, R.drawable.widgetoff);
    	}


        // AppWidget�̉�ʍX�V
        ComponentName thisWidget = new ComponentName(this, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, remoteViews);
        
    }
 
}
