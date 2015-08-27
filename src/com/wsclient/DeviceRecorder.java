package com.wsclient;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import com.monitors.ws.WebServiceStub;
import com.monitors.ws.WebServiceStub.Device;

public class DeviceRecorder extends MonitorDataRecorder {

	public static final int TIMESTAMP=1;
	public static final int SRCMAC=2;
	public static final int SIGNAL=3;
	public static final int SRCDEVTYPE=4;
	
	public DeviceRecorder(String url, int scannerID, HashMap<String, String> ouis) {
		super(url, scannerID, ouis);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void putData(String tupla) {
		String[] chunks = tupla.split("\\"+CaptureFields.FIELDSEPARATOR,-1);
		formatTimestamp(chunks);
		setSourceDeviceType(chunks);
		chunks[CaptureFields.SRCMAC]=CaptureFields.formatAddressMAC(chunks[CaptureFields.SRCMAC]);

		//reassemble chunks
		//id_scanner|timestamp|mac|signal|source_device_type|protocol
		data.append(scannerId+CaptureFields.FIELDSEPARATOR);
		data.append(chunks[CaptureFields.TIMESTAMP]+CaptureFields.FIELDSEPARATOR);
		data.append(chunks[CaptureFields.SRCMAC]+CaptureFields.FIELDSEPARATOR);
		data.append(CaptureFields.DEFAULT_SIGNAL+CaptureFields.FIELDSEPARATOR); //No antenna dbm signal -> 0 default
		data.append(chunks[CaptureFields.SRCDEVTYPE]+CaptureFields.FIELDSEPARATOR);
		data.append(PROTOCOL+CaptureFields.FIELDSEPARATOR);
		
		//data1;data2;...;dataN
		data.append(FRAMESEPARATOR);// TODO concat tuplas 
	}
	
	@Override
	public Device buildDTO(String tupla) throws ParseException {
		String[] chunks = tupla.split("\\"+CaptureFields.FIELDSEPARATOR);
		Date datetime = new Date();
		datetime = MonitorDataRecorder.TIMESTAMPFORMAT.parse(chunks[TIMESTAMP]);
		Calendar timestamp = MonitorDataRecorder.TIMESTAMPFORMAT.getCalendar();
		timestamp.setTime(datetime);
		
		Device device = new Device();
		device.setId_scanner(scannerId);
		device.setTimestamp(timestamp);
		device.setMac(chunks[SRCMAC]);
		device.setSignal(0);
		device.setSource_device_type(chunks[SRCDEVTYPE]);
		device.setProtocol(PROTOCOL);
		return device;
	}

	@Override
	public String insertDTO() {
		try {
			WebServiceStub service = new WebServiceStub(url);
			
			WebServiceStub.InsertSingleDeviceDTO req = new WebServiceStub.InsertSingleDeviceDTO();
			req.setDevice(buildDTO(data.toString().split(FRAMESEPARATOR,2)[0]));
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT SINGLE DEVICE DTO: "+data);
			data = new StringBuilder();
			
			return ("SINGLE DEVICE DTO "+service.insertSingleDeviceDTO(req).get_return());
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getPackage().getName()).warning("Service error: "+ e.getMessage());
			return("Service error");
		}
	}

	@Override
	public String insert() {
		try {
			WebServiceStub service = new WebServiceStub(url);
			
			WebServiceStub.InsertSingleDevice req = new WebServiceStub.InsertSingleDevice();
			req.setData(data.toString().split(FRAMESEPARATOR, 2)[0]);
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT SINGLE DEVICE: "+data);
			data = new StringBuilder();
			
			return ("SINGLE DEVICE "+service.insertSingleDevice(req).get_return());
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getPackage().getName()).warning("Service error: "+ e.getMessage());
			return("Service error");
		}
	}

	@Override
	public String insertMultiple() {
		try {
			WebServiceStub service = new WebServiceStub(url);
			
			WebServiceStub.InsertMultipleDevices req = new WebServiceStub.InsertMultipleDevices();
			req.setData(data.toString());
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT MULTIPLE DEVICE: "+data+"\n");
			data = new StringBuilder();
			
			return ("MULTIPLE DEVICE "+service.insertMultipleDevices(req).get_return());
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getPackage().getName()).warning("Service error: "+ e.getMessage());
			return("Service error: "+data.toString());
		}	
	}

	@Override
	public String insertMultipleDTO() {
		try {
			WebServiceStub service = new WebServiceStub(url);
			
			WebServiceStub.InsertMultipleDeviceDTO req = new WebServiceStub.InsertMultipleDeviceDTO();
			
			String[] tuplas = data.toString().split(FRAMESEPARATOR);
			for(String tupla:tuplas){
				req.addDevices(buildDTO(tupla));
			}			
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT MULTIPLE DEVICE DTO: "+data);
			data = new StringBuilder();
			
			return ("MULTIPLE DEVICE DTO "+service.insertMultipleDeviceDTO(req).get_return());
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getPackage().getName()).warning("Service error: "+ e.getMessage());
			return ("SERVICE ERROR");
		}
	}
	
	@Override
	public String toString() {
		return "Device"; //to show in GUI
	}
}
