package de.htw.nfc.relay;

import android.annotation.TargetApi;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class RaceActivity extends RaceActivityPrototype implements NfcAdapter.OnNdefPushCompleteCallback {
    
    private static RaceActivity instance;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setupActionBar();
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    private final static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            instance.switchState(State.FINISHED);
        }
      };

      @Override
      public void onNdefPushComplete(NfcEvent arg0) {
          handler.obtainMessage(0).sendToTarget();
      }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void enablePush() {
        mAdapter.setNdefPushMessage(mMessage, this);
        mAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void disablePush() {
        mAdapter.setNdefPushMessage(null, this);
        mAdapter.setOnNdefPushCompleteCallback(null, this);
    }

}
