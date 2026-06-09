package com.example.fonosshoppee.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.fonosshoppee.service.AudioPlayerService;

public class SleepTimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AudioPlayerService.class);
        serviceIntent.setAction("ACTION_PAUSE");
        context.startService(serviceIntent);
    }
}