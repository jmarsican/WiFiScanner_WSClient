package com.wsclient;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CaptureFields {
	/*esta clase centraliza los tipos de campos y el orden con que TShark los devuelve en stdout
		adaptandolos a la interfaz que requiere el WebService*/
	
	//id_scanner|timestamp|source_mac|destination_mac|signal|frame_type|source_device_type|protocol|ssid
	public static final int TIMESTAMP=0;
	public static final int SRCMAC=1;
	public static final int DSTMAC=2;
	public static final int SIGNAL=3;
	public static final int FRAMETYPE=4;
	public static final int SRCDEVTYPE=5;
	public static final int SSID=6;
	
	public static final int TOTALFIELDS=7;
	
	public static final String FIELDSEPARATOR = "|";
	
	public static final String DEFAULT_SSID = "NN";	
	
	public static final String DEFAULT_SIGNAL = "0";
	
	public static final SimpleDateFormat TIMESTAMPFORMAT = 
			new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS", Locale.ENGLISH);
	
	public static String formatAddressMAC(String mac){
		String out = mac.replace(":", "");
		out = out.toUpperCase();
		return out;
	}
	
	public static final String[] PARAMS = {
		"-e frame.time",
		"-e wlan.sa",
		"-e wlan.da",
		"-e radiotap.dbm_antsignal",
		"-e wlan.fc.type_subtype",
		"-e wlan.fc.fromds",
		"-e wlan_mgt.ssid"
	};
}
