package com.commandercool.alex.print;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	
	private SharedPreferences prefs = null;
	private static final String PREFS_NAME = "myprefs";
	
	private String ip = null;
	private Integer port = 0;
	
	/** Getting preferences on start */
    public void onStart(){
    	super.onStart();
    	EditTextPreference ipField = (EditTextPreference) findPreference("ip_address");
        EditTextPreference portField = (EditTextPreference) findPreference("port");
        ipField.setOnPreferenceChangeListener(edirTxtListener);
        portField.setOnPreferenceChangeListener(edirTxtListener);
    	
        prefs = getSharedPreferences(PREFS_NAME, 1);
        
    	ip = prefs.getString("ip_address", (String) getText(R.string.set_ip_address));
        port = prefs.getInt("port", 8080);
    	
        ipField.setSummary(ip);
    	portField.setSummary(port.toString());
        
    	ipField.setDialogMessage(ip);
    	portField.setDialogMessage(port.toString());
    }
	
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.preferences);
	 }
	 
	 /** Saving preferences on pause */
	    protected void onPause() {
	        super.onPause();

	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString("ip_address", ip);
	        editor.putInt("port", port);
	        editor.commit();
	    }
	 
	 public OnPreferenceChangeListener edirTxtListener = new OnPreferenceChangeListener(){

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// TODO Auto-generated method stub
			String key = preference.getKey();
			
			if (key.matches("ip_address")){
				ip = newValue.toString();
			} else {
				try {
					port = Integer.parseInt(newValue.toString());
				} catch (Exception ex){
					Toast.makeText(getApplicationContext(), getText(R.string.port_err), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			preference.setSummary(newValue.toString());
			
			// Saving preferences
			SharedPreferences.Editor editor = prefs.edit();
	        editor.putString("ip_address", ip);
	        editor.putInt("port", port);
	        editor.commit();
			
			return true;
		}
		 
	 };
}