package de.htw.nfc.relay;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.PendingIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.text.TextUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public class RaceActivityPrototype extends Activity implements OnEditorActionListener {
    
    protected enum State {
        WAITING, CURRENT, FINISHED
    }
    
    private State mState;
    protected NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    
    private TextView mStatusView;
    private EditText mPlayerNumberView;
    private EditText mKeyView;
    private TextView mReceivedView;
    private TextView mSendView;
    
    private int mPlayerNumber;
    private int mKey;
    
    private byte[] mToken;
    private byte[] mEncryptedToken;
    protected NdefMessage mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);
        
        mStatusView = (TextView) findViewById(R.id.race_state);
        mPlayerNumberView = (EditText) findViewById(R.id.race_playernum);
        mKeyView = (EditText) findViewById(R.id.race_key);
        mReceivedView = (TextView) findViewById(R.id.race_recvd);
        mSendView = (TextView) findViewById(R.id.race_sent);
        mPlayerNumberView.setOnEditorActionListener(this);
        mKeyView.setOnEditorActionListener(this);
        
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        mKey = prefs.getInt("key", 0);
        mPlayerNumber = prefs.getInt("playernum", 0);
        mKeyView.setText(Integer.toString(mKey));
        mPlayerNumberView.setText(Integer.toString(mPlayerNumber));
        
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
        
        mState = State.WAITING;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        try {
            mKey = Integer.parseInt(mKeyView.getText().toString());
            mPlayerNumber = Integer.parseInt(mPlayerNumberView.getText().toString());
        } catch (Exception e) { /* ignore gracefully */ }
        saveMembers();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
            if (State.CURRENT == mState) {
                mAdapter.enableForegroundNdefPush(this, mMessage);
            }
        }
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void switchState(State s) {
        mState = s;
        switch (s) {
        case WAITING:
            mStatusView.setText(R.string.label_waiting);
            mReceivedView.setText(R.string.label_notoken);
            mSendView.setText(R.string.label_notoken);
            break;
        case CURRENT:
            mPlayerNumberView.setText(Integer.toString(mPlayerNumber));
            mReceivedView.setText(TokenHelper.hexString(mToken));
            updateToken();
            mStatusView.setText(R.string.label_current);
            enablePush();
            
            break;
        default:
            mStatusView.setText(R.string.label_sent);
            disablePush();
            break;
        }
    }
    
    protected void disablePush() { }
    
    protected void enablePush() { }
    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("Race", "onNewIntent");
        if (State.CURRENT == mState) return;
        if (intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            for (Parcelable p : rawMsgs) {
                NdefMessage msg = (NdefMessage) p;
                if (msg.getRecords().length > 0) {
                    NdefRecord rec = msg.getRecords()[0];
                    if (new String(rec.getType()).equals("nfc/relayrace")) {
                        mToken = rec.getPayload();
                        mPlayerNumber = rec.getId()[0];
                        loadKey();
                        saveMembers();
                        switchState(State.CURRENT);
                    }
                }
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        try {
            mKey = Integer.parseInt(mKeyView.getText().toString());
            mPlayerNumber = Integer.parseInt(mPlayerNumberView.getText().toString());
            if (mPlayerNumberView == arg0) {
                loadKey();
            }
        } catch (Exception e) { /* ignore gracefully */ }
        saveMembers();
        updateToken();
        return false;
    }
    
    private void saveMembers() {
        updateToken();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt("key", mKey);
        editor.putInt("playernum", mPlayerNumber);
        editor.commit();
    }
    
    private void loadKey() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String keylist = prefs.getString("keylist", "");
        String[] entries = TextUtils.split(keylist, ",");
        if ((mPlayerNumber >= 0) && (mPlayerNumber < entries.length)) {
            mKeyView.setText(entries[mPlayerNumber]);
            mKey = Integer.parseInt(entries[mPlayerNumber]);
        }
    }
    
    private void updateToken() {
        if (null != mToken) {
            mEncryptedToken = TokenHelper.getInstance().encryptToken(mToken, mKey, mPlayerNumber);
            mSendView.setText(TokenHelper.hexString(mEncryptedToken));
            NdefRecord rec = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                    new String("nfc/relayrace").getBytes(),
                    new byte[]{(byte) (mPlayerNumber + 1)}, mEncryptedToken);
            mMessage = new NdefMessage(new NdefRecord[] {rec});
        } else {
            mReceivedView.setText(R.string.label_notoken);
            mSendView.setText(R.string.label_notoken);
            mMessage = null;
        }
    }

}
