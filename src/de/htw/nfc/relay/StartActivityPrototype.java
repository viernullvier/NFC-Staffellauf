package de.htw.nfc.relay;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.app.Activity;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.text.TextUtils;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public abstract class StartActivityPrototype extends Activity  {
    
    private TextView mPlayerCountView;
    protected TextView mTokenView;
    protected NfcAdapter mAdapter;
    protected NdefMessage mMessage;
    protected boolean isPushing;
    
    abstract void enablePush();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mPlayerCountView = (TextView) findViewById(R.id.start_playercnt);
        mTokenView = (TextView) findViewById(R.id.start_token);
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        isPushing = false;
    }
    
    @Override
    protected void onResume() {
        isPushing = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String[] entries = TextUtils.split(prefs.getString("keylist", ""), ",");
        mPlayerCountView.setText(Integer.toString(entries.length));
        super.onResume();
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onStartRelayClick(View v) {
        if (null != mAdapter) {
            isPushing = true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String[] entries = TextUtils.split(prefs.getString("keylist", ""), ",");
            byte[] token = TokenHelper.getInstance().makeToken(entries.length);
            mTokenView.setText(TokenHelper.hexString(token));
            prefs.edit().putString("token", Base64.encodeToString(token, Base64.DEFAULT)).commit();
            NdefRecord rec = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                    new String("nfc/relayrace").getBytes(),
                    new byte[]{0}, token);
            mMessage = new NdefMessage(new NdefRecord[] {rec});
            enablePush();
        }
    }

}
