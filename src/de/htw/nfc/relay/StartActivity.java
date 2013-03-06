package de.htw.nfc.relay;

import android.annotation.TargetApi;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;

public class StartActivity extends StartActivityPrototype implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    void enablePush() {
        mAdapter.setNdefPushMessageCallback(this, this);
        mAdapter.setOnNdefPushCompleteCallback(this, this);
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onPause() {
        super.onPause();
        if (null != mAdapter && isPushing) {
            mAdapter.setNdefPushMessageCallback(null, this);
        }
    }
    
    @Override
    public NdefMessage createNdefMessage(NfcEvent arg0) {
        if (isPushing) return mMessage;
        else return null;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        mTokenView.setText(R.string.label_notoken);
        mMessage = null;
        isPushing = false;
    }
    

}
