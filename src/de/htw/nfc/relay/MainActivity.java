package de.htw.nfc.relay;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_settings == item.getItemId()) {
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void onStartClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, StartActivityLegacy.class);
            startActivity(intent);
        }
        
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    public void onFinishClick(View v) {
        Intent intent = new Intent(this, FinishActivity.class);
        startActivity(intent);
    }
    
    public void onRaceClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Intent intent = new Intent(this, RaceActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, RaceActivityPrototype.class);
            startActivity(intent);
        }
    }
}
