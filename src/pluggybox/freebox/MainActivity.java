package pluggybox.freebox;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();
    private TextView textServiceInfo;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        textServiceInfo = (TextView)findViewById(R.id.textViewServiceInfo);

        textServiceInfo.setText("Attente...");
        handler.postDelayed(new Runnable() {
            public void run() {
            	textServiceInfo.setText("Go!");
                setUp();
            }
            }, 1000);

    } /** Called when the activity is first created. */

    private String wifiStateToString(int state)
    {
    	String resultat="?";
    	switch(state)
    	{
    	case android.net.wifi.WifiManager.WIFI_STATE_DISABLED:
    		resultat = "WIFI_STATE_DISABLED";
    		break;
    	case android.net.wifi.WifiManager.WIFI_STATE_DISABLING:
    		resultat = "WIFI_STATE_DISABLING";
    		break;
    	case android.net.wifi.WifiManager.WIFI_STATE_ENABLED:
    		resultat = "WIFI_STATE_ENABLED";
    		break;
    	case android.net.wifi.WifiManager.WIFI_STATE_ENABLING:
    		resultat = "WIFI_STATE_ENABLING";
    		break;
    	case android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN:
    		default:
    		resultat = "?";
    		break;
    	}
    	return resultat;
    }

    private String type = "_fbx-api._tcp.";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    private ServiceInfo serviceInfo;
    private void setUp() {
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        
        lock = wifi.createMulticastLock("mylockthereturn");
        lock.setReferenceCounted(true);
        lock.acquire();

        textServiceInfo.setText("Statut WIFI: " +wifiStateToString(wifi.getWifiState()));
        if(wifi.isWifiEnabled() == true)
        {
        	
        	WifiInfo connexion = wifi.getConnectionInfo();
        	textServiceInfo.setText("Connexion WIFI: " + connexion.getSSID());
        }


        try {
            jmdns = JmDNS.create();
            jmdns.addServiceListener(type, listener = new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent ev) {
                    notifyUser("Service resolved: " + ev.getInfo().getQualifiedName() + " port:" + ev.getInfo().getPort());
                }

                @Override
                public void serviceRemoved(ServiceEvent ev) {
                    notifyUser("Service removed: " + ev.getName());
                }

                @Override
                public void serviceAdded(ServiceEvent event) {
                    // Required to force serviceResolved to be called again (after the first search)
                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            });
        } catch (Exception e) {
            textServiceInfo.setText("Erreur: " +e.getMessage());
            return;
        }
        
    }


    private void notifyUser(final String msg) {
        handler.postDelayed(new Runnable() {
            public void run() {

        TextView t = (TextView)findViewById(R.id.textViewServiceInfo);
        t.setText(msg+"\n=== "+t.getText());
            }
            }, 1);

    }

    @Override
        protected void onStart() {
        super.onStart();
        //new Thread(){public void run() {setUp();}}.start();
    }

    @Override
        protected void onStop() {
            if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(type, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jmdns = null;
            }
            //repo.stop();
        //s.stop();
        lock.release();
            super.onStop();
    }
}