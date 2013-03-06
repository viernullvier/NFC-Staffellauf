package de.htw.nfc.relay;

import java.util.Arrays;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.PendingIntent;
import android.util.Base64;
import android.widget.TextView;
import android.text.TextUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Build;
import android.preference.PreferenceManager;

public class FinishActivity extends Activity {
    
    private TextView mTokenLabel;
    private TextView mExpectedLabel;
    private TextView mReceivedLabel;
    private TextView mIdentLabel;
    
    private byte[] mToken;
    private byte[] mExpected;
    
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        setupActionBar();
        
        mTokenLabel = (TextView) findViewById(R.id.finish_token);
        mExpectedLabel = (TextView) findViewById(R.id.finish_expect);
        mReceivedLabel = (TextView) findViewById(R.id.finish_recv);
        mIdentLabel = (TextView) findViewById(R.id.finish_ident);
        
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("nfc/relayrace");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] { ndef };
        mTechLists = new String[][] { new String[] { Ndef.class.getName() } };
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String[] entries = TextUtils.split(prefs.getString("keylist", ""), ",");
        mToken = Base64.decode(prefs.getString("token", ""), Base64.DEFAULT);
        if (mToken.length > 0) {
            mTokenLabel.setText(TokenHelper.hexString(mToken));
        } else {
            mTokenLabel.setText(R.string.label_notoken);
        }
        mExpected = mToken.clone();
        int i = 0;
        for (String entry: entries) {
            int key = Integer.parseInt(entry);
            mExpected = TokenHelper.getInstance().encryptToken(mExpected, key, i);
            i++;
        }
        if (mExpected.length > 0) {
            mExpectedLabel.setText(TokenHelper.hexString(mExpected));
        } else {
            mExpectedLabel.setText(R.string.label_notoken);
        }
        
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            //TextView v = (TextView) findViewById(R.id.hello_world);
            for (Parcelable p : rawMsgs) {
                NdefMessage msg = (NdefMessage) p;
                if (msg.getRecords().length > 0) {
                    NdefRecord rec = msg.getRecords()[0];
                    byte[] received = rec.getPayload();
                    mReceivedLabel.setText(TokenHelper.hexString(received));
                    if (Arrays.equals(mExpected, received)) {
                        mIdentLabel.setText(R.string.label_yes);
                        mIdentLabel.setTextColor(getResources().getColor(R.color.label_ident));
                    } else {
                        mIdentLabel.setText(R.string.label_no);
                        mIdentLabel.setTextColor(getResources().getColor(R.color.label_not_ident));
                    }
                }
            }
        }
        super.onNewIntent(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}
