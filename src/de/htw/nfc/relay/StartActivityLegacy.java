package de.htw.nfc.relay;

public class StartActivityLegacy extends StartActivityPrototype {

    @SuppressWarnings("deprecation")
    @Override
    void enablePush() {
        mAdapter.enableForegroundNdefPush(this, mMessage);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        if (null != mAdapter) {
            mAdapter.disableForegroundNdefPush(this);
        }
    }

}
