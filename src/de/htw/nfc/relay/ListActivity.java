package de.htw.nfc.relay;

import java.util.Vector;

import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.text.InputType;
import android.text.TextUtils;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public class ListActivity extends Activity implements OnEditorActionListener {
    
    private LinearLayout mList;
    private Vector<LinearLayout> mPlayerViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setupActionBar();
        
        mPlayerViews = new Vector<LinearLayout>();
        mList = (LinearLayout) findViewById(R.id.list_layout);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String[] entries = TextUtils.split(prefs.getString("keylist", ""), ",");
        for (String key: entries) {
            addItem(key);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.list_add:
            addItem();
            return true;
        case R.id.list_remove:
            removeItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeItem() {
        if (mPlayerViews.size() > 0) {
            LinearLayout item = mPlayerViews.lastElement();
            mPlayerViews.remove(item);
            mList.removeView(item);
            saveList();
        }
    }
    
    private void addItem() {
        addItem(Integer.toString(mPlayerViews.size()));
    }

    private void addItem(String key) {
        LinearLayout item = new LinearLayout(this);
        item.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.cell_height))
        );
        
        TextView label = new TextView(this);
        label.setText(getString(R.string.label_keynum, mPlayerViews.size()));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER_VERTICAL;
        lp.weight = 1;
        label.setLayoutParams(lp);
        item.addView(label);
        
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setGravity(Gravity.RIGHT);
        input.setText(key);
        input.setLayoutParams(lp);
        input.setOnEditorActionListener(this);
        item.addView(input);
        
        mPlayerViews.add(item);
        mList.addView(item);
        saveList();
    }
    
    private void saveList() {
        String[] entries = new String[mPlayerViews.size()];
        for (LinearLayout item: mPlayerViews) {
            EditText input = (EditText) item.getChildAt(1);
            entries[mPlayerViews.indexOf(item)] = input.getText().toString();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("keylist", TextUtils.join(",", entries));
        editor.commit();
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        saveList();
        return false;
    }
    
    @Override
    protected void onPause() {
        saveList();
        super.onPause();
    }

}
