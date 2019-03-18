package com.example.chrys.intouch;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class MainActivity extends AppCompatActivity{

    TextView text;
    Toast t1,t2;

    ListView lv;
    WifiManager wifiManager;
    String wifis[];
    WifiScanReceiver wifiReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv=(ListView)findViewById(R.id.wifiList);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
                String value = (String)adapter.getItemAtPosition(position);
                connectToWiFi(value);
            }
        });

        wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiScanReceiver();
        wifiManager.startScan();


        //wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        //text = (TextView) findViewById(R.id.showWiFis);
        //WiFiStatus();
    }

//    void WiFiStatus(){
//
//        if(!wifiManager.isWifiEnabled()){
//            wifiManager.setWifiEnabled(true);
//        }
//        if(wifiManager.getConnectionInfo().getNetworkId() == -1){
//            WiFiBroadcastReceiver WiFiReceiver = new WiFiBroadcastReceiver();
//            registerReceiver(WiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//            t1 = Toast.makeText(getApplicationContext(),"Searching for WiFi networks in your area..",Toast.LENGTH_SHORT);
//            t1.show();
//        }else{
//            t2 = Toast.makeText(getApplicationContext(),"Connected to " + wifiManager.getConnectionInfo().getSSID(),Toast.LENGTH_SHORT);
//            if(t1 != null) t1.cancel();
//            t2.show();
//            text.setText(wifiManager.getConnectionInfo().toString());
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi_example, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
        //WiFiStatus();
    }

    @Override
    protected void onPause()
    {
        unregisterReceiver(wifiReceiver);
        super.onPause();
        //WiFiStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if(wifiManager.isWifiEnabled()){
//            wifiManager.setWifiEnabled(false);
//        }
//        WiFiStatus();
    }

//    class WiFiBroadcastReceiver extends BroadcastReceiver {
//
//        public void onReceive(Context context, Intent intent){
//            StringBuffer stringBuffer = new StringBuffer();
//            List<ScanResult> WiFiList = wifiManager.getScanResults();
//            for(ScanResult scanResult : WiFiList) {
//                stringBuffer.append(scanResult + "\n");
//            }
//            text.setText(stringBuffer);
//            WiFiStatus();
//        }
//
//    }

    private class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            wifis = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++){
                wifis[i] = ((wifiScanList.get(i)).toString().replaceAll("," , "\n"));
            }
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,wifis));
        }
    }

    protected void connectToWiFi(String value) {
        String items[] = value.split("\n");
        String networkSSID = "";
        EditText et = (EditText) findViewById(R.id.EditText01);
        String networkPass = et.getText().toString();
        WifiConfiguration conf = new WifiConfiguration();

        for(int i=0; i<items.length; i++){
            if(items[i].contains("SSID") && !items[i].contains("BSSID")){
                String SSID_items[] = items[i].split("\\s"); //SSID: [THE_SSID]
                networkSSID = SSID_items[SSID_items.length-1];
            }

            if(items[i].contains("capabilities")){ //capabilities: [THE_CAPABILITIES]
                if(items[i].contains("WPA")){
                    conf.preSharedKey = "\""+ networkPass +"\"";
                }else if(items[i].contains("WEP")){
                    conf.wepKeys[0] = "\"" + networkPass + "\"";
                    conf.wepTxKeyIndex = 0;
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                }else{ //we suppose that network does not require password
                    //nothing to do
                }
            }
        }

        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Toast.makeText(getApplicationContext(), "Connecting to: "+ networkSSID +"...", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

}
