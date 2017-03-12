package jp.stargroove.naoya.android.ledwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class Widget extends AppWidgetProvider {
    private Context con;

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
 
        // サービスの起動
        Intent intent = new Intent(context, LedService.class);
        context.startService(intent);
        
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // サービスの起動
        Intent intent = new Intent(context, LedService.class);
        context.stopService(intent);
    	
    }
}
