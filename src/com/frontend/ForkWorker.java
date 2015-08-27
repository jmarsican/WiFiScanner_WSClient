package com.frontend;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import com.wsclient.MonitorDataRecorder;


public class ForkWorker extends SwingWorker<String,String> {
//creates a TShark's child process and manages synchronization and output stream before calling WS insert
	private Logger output;
	private ProcessBuilder builder;
	private Process ps;
	
	private int reads,limit,framesToCommit;
	
	private MonitorDataRecorder mDataRecorder;	
	
	public ForkWorker(Logger output, ProcessBuilder builder,MonitorDataRecorder mdr,int limit) {
		super();
		this.output = output;
		this.builder = builder;
		mDataRecorder= mdr;
		this.limit = limit; 
		reads = 0;
		framesToCommit = 0;
	}	
	
	@Override
	protected void process(List<String> chunks) {
		for(String tupla: chunks){
			Logger.getLogger(this.getClass().getName()).finer(tupla+"\n");
			mDataRecorder.putData(tupla);
			framesToCommit++;		
			
			if(framesToCommit>=limit){			
				output.info(reads +" tuplas leidas, invocando servicio...\n");
				output.info(
						mDataRecorder.insertMultiple() +"\n"); //invoke Web Service
				framesToCommit=0;
			}
		}
	}

	@Override
	protected String doInBackground() throws Exception {		
		ps = builder.start();		
		BufferedReader bs = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		//read TShark output
		String line = null;
		while((line = bs.readLine())!=null){
			reads++;
			publish(line);
			if(isCancelled()){
				ps.destroy();
				return "";
			}
		}			
		return ""; // don't care
	}

	@Override
	protected void done() {
		output.info("\n"+reads+" paquetes capturados \n");
		ps.destroy();
	}	

}
