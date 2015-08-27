package com.wsclient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.axis2.databinding.ADBBean;

public abstract class MonitorDataRecorder {
	protected String url;
	protected StringBuilder data;
	protected final int scannerId;	
	private HashMap<String, String> ouis;
	
	public static final String PROTOCOL = "W"; //W=WiFi B=Bluetooth
	public static final String FRAMESEPARATOR = ";";
	public static final SimpleDateFormat TIMESTAMPFORMAT = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public MonitorDataRecorder(String url, int scannerID, HashMap<String, String> ouis){
		this.url = url;
		this.ouis=ouis;
		data = new StringBuilder();
		this.scannerId=scannerID;
	}
	
	protected void formatTimestamp(String[] chunks){		
		String unformatedDate = chunks[CaptureFields.TIMESTAMP];

		Date date = new Date();
		try {
			date = CaptureFields.TIMESTAMPFORMAT.parse(unformatedDate.substring(0, unformatedDate.length()-6));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		chunks[0] = MonitorDataRecorder.TIMESTAMPFORMAT.format(date);		
	}
	
	protected void setSourceDeviceType(String[] chunks){
		String sourceMacAddr = chunks[CaptureFields.SRCMAC];
		
		if(sourceMacAddr.length()>=8)		//ignore company details
			sourceMacAddr = sourceMacAddr.substring(0, 8);
		
		chunks[CaptureFields.SRCDEVTYPE] = ouis.get(sourceMacAddr)+"-"+chunks[CaptureFields.SRCDEVTYPE];
	}
	
	public abstract void putData(String tupla);
	public abstract String insertDTO();	
	public abstract String insert();
	public abstract String insertMultiple();
	public abstract String insertMultipleDTO();
	public abstract ADBBean buildDTO(String tupla)throws ParseException;
}
