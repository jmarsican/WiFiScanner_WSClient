package com.wsclient;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import com.monitors.ws.WebServiceStub;
import com.monitors.ws.WebServiceStub.Frame;

public class FrameRecorder extends MonitorDataRecorder {
	
	public FrameRecorder(String url,int scannerId,HashMap<String, String> ouis) {
		super(url,scannerId,ouis);
	}		

	@Override
	public void putData(String tupla) {
		String[] chunks = tupla.split("\\"+CaptureFields.FIELDSEPARATOR,-1);
		formatTimestamp(chunks);
		setSourceDeviceType(chunks);
		chunks[CaptureFields.SRCMAC]=CaptureFields.formatAddressMAC(chunks[CaptureFields.SRCMAC]);
		chunks[CaptureFields.DSTMAC]=CaptureFields.formatAddressMAC(chunks[CaptureFields.DSTMAC]);
		
		//reassemble chunks
		//id_scanner|timestamp|source_mac|destination_mac|signal|frame_type|source_device_type|protocol|ssid
		data.append(scannerId+CaptureFields.FIELDSEPARATOR);
		data.append(chunks[CaptureFields.TIMESTAMP]+CaptureFields.FIELDSEPARATOR);
		data.append(chunks[CaptureFields.SRCMAC]+CaptureFields.FIELDSEPARATOR);
		data.append(chunks[CaptureFields.DSTMAC]+CaptureFields.FIELDSEPARATOR);
		data.append(CaptureFields.DEFAULT_SIGNAL+CaptureFields.FIELDSEPARATOR); //No antenna dbm signal -> 0 default
		data.append(chunks[CaptureFields.FRAMETYPE]+CaptureFields.FIELDSEPARATOR);
		data.append(chunks[CaptureFields.SRCDEVTYPE]+CaptureFields.FIELDSEPARATOR);
		data.append(PROTOCOL+CaptureFields.FIELDSEPARATOR);
		String ssid = chunks[CaptureFields.SSID]; 
		if (ssid.length() == 0) //SSID must not be empty
			ssid = CaptureFields.DEFAULT_SSID;
		data.append(ssid+CaptureFields.FIELDSEPARATOR);	
		
		//data1;data2;...;dataN
		data.append(FRAMESEPARATOR);
	}
	
	public Frame buildDTO(String tupla) throws ParseException{
		String[] chunks = tupla.split("\\"+CaptureFields.FIELDSEPARATOR);
		Date datetime = new Date();
		datetime = MonitorDataRecorder.TIMESTAMPFORMAT.parse(chunks[CaptureFields.TIMESTAMP+1]);
		Calendar timestamp = MonitorDataRecorder.TIMESTAMPFORMAT.getCalendar();
		timestamp.setTime(datetime);
		
		Frame new_frame = new Frame();	
		new_frame.setId_scanner(scannerId);
		new_frame.setTimestamp(timestamp);
		new_frame.setSource_mac(chunks[CaptureFields.SRCMAC+1]);
		new_frame.setDestination_mac(chunks[CaptureFields.DSTMAC+1]);
		new_frame.setSignal(0);
		new_frame.setFrame_type(chunks[CaptureFields.FRAMETYPE+1]);
		new_frame.setSource_device_type(chunks[CaptureFields.SRCDEVTYPE+1]);
		new_frame.setProtocol(PROTOCOL);
		new_frame.setSsid(chunks[CaptureFields.SSID+2]);		
		return new_frame;
	}

	@Override
	public String insertDTO() {		
		try {			
			WebServiceStub service = new WebServiceStub(url);
			WebServiceStub.InsertSingleFrameDTO req = new WebServiceStub.InsertSingleFrameDTO();
			req.setFrame(buildDTO(data.toString().split(FRAMESEPARATOR, 2)[0])); //add only the first one
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT SINGLE FRAME DTO: "+data);
			data = new StringBuilder();
			
			return service.insertSingleFrameDTO(req).get_return();			
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).warning("\n Service error: "+ e.getMessage()+"\n");
			return("Service error");
		}
	}

	@Override
	public String insert() {
		try {
			WebServiceStub service = new WebServiceStub(url);
			
			WebServiceStub.InsertSingleFrame req = new WebServiceStub.InsertSingleFrame();
			req.setData(data.toString().split(FRAMESEPARATOR, 2)[0]);
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT SINGLE FRAME: "+data);
			data = new StringBuilder();
			
			return service.insertSingleFrame(req).get_return();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).warning("\n Service error: "+ e.getMessage()+"\n");
			return("Service error");
		}
	}

	@Override
	public String insertMultiple() {
		try {
			WebServiceStub service = new WebServiceStub(url);
			
			WebServiceStub.InsertMultipleFrames req = new WebServiceStub.InsertMultipleFrames();
			req.setData(data.toString());
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT MULTIPLE FRAME: "+data+"\n");
			data = new StringBuilder();
			
			return service.insertMultipleFrames(req).get_return();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).warning("\n Service error: "+ e.getMessage()+"\n");
			return("Service error: "+data.toString());
		}		
	}

	@Override
	public String insertMultipleDTO() {
		try{
			WebServiceStub service = new WebServiceStub(url);
			
			WebServiceStub.InsertMultipleFrameDTO req = new WebServiceStub.InsertMultipleFrameDTO();
			
			String[] frames = data.toString().split(FRAMESEPARATOR);
			for(String tupla:frames){
				req.addFrames(buildDTO(tupla));
			}
			Logger.getLogger(this.getClass().getPackage().getName()).fine("\n INSERT MULTIPLE FRAME DTO: "+data);
			data = new StringBuilder();
			
			return service.insertMultipleFrameDTO(req).get_return();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).warning("\n Service error: "+ e.getMessage()+"\n");
			return("Service error");
		}	
	}

	@Override
	public String toString() {
		return "Frame"; //to show in GUI
	}
}
