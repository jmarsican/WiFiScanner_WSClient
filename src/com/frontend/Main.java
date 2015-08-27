package com.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.*;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import com.wsclient.*;


public class Main implements ActionListener {
	protected JTextArea output = new JTextArea(10,30);
	protected JButton start = new JButton("Start");
	protected JButton stop = new JButton("Stop");
	protected JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(10,3,20,1));
	protected JComboBox<MonitorDataRecorder> cboRecorders = new JComboBox<MonitorDataRecorder>();
	
	private ForkWorker fw; 
	private ProcessBuilder pb;
	
	protected String url;
	protected HashMap<String,String> ouis;
	protected int scannerId;
	
	public Main(){		
		StringBuilder tsharkCmd = new StringBuilder("tshark -i kistap0 -V -l -T fields");
		for(int i=0;i<CaptureFields.TOTALFIELDS;i++)			
			tsharkCmd.append(" "+CaptureFields.PARAMS[i]);
		tsharkCmd.append(" -E separator="+CaptureFields.FIELDSEPARATOR);
		
		pb = new ProcessBuilder(tsharkCmd.toString().split(" "));
		
		ouis = new HashMap<String,String>();
	}
	
	protected void loadFiles() throws IOException{	
			//load settings file
			BufferedReader br = new BufferedReader(new FileReader("WiFiScanner.conf"));
			url = br.readLine().replace("url=","");
			scannerId = Integer.parseInt(br.readLine().replace("scannerid=", ""));
			String wiresharkpath = br.readLine().replace("wiresharkpath=","");
			br.close();
			
			//Load OUI's MAC Ethernet vendor codes from wireshark's manuf file
			br = new BufferedReader(new FileReader(wiresharkpath+"/manuf"));
			String line = br.readLine();			
			//ignore comments
			while ((line != null)&&(line.length()>0)&&(line.toCharArray()[0]=='#'))	line= br.readLine();
			
			line = br.readLine(); //read white line before table begins
			//stop reading before well-known addresses or end of file
	        while ((line != null)&&(line.length()>=8)) {
	        	String[] chunks = line.split("\t");
	        	//populate key=MAC value=VENDOR'S NAME
	        	ouis.put(chunks[0].toLowerCase(), chunks[1].split(" ")[0]);
	            line = br.readLine();
	        }
	        br.close();			
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource()==start){
			output.append("\n --- STARTING CAPTURE --- \n");
			System.out.println("\n --- STARTING CAPTURE --- \n");
			
			MonitorDataRecorder mDataRecorder = cboRecorders.getItemAt(cboRecorders.getSelectedIndex());
			int limit = ((SpinnerNumberModel)limitSpinner.getModel()).getNumber().intValue();
			fw = new ForkWorker(Logger.getGlobal(),pb,mDataRecorder,limit);
			
			fw.execute();			
		}
		if(event.getSource()==stop){
			if (fw!=null) fw.cancel(true);
		}
	}
	
	public static void setLogging(String[] args,Handler customHandler){
		Level loggerLevel=Level.WARNING;
		for(String arg : args){                 
            switch (arg){
                    case "-dc":
                            loggerLevel= Level.FINER;
                            break;
                    case "-di":
                    		loggerLevel= Level.FINE;
                            break;
                    default:
                            break;
            }
		}			
		Handler logHandler = new ConsoleHandler();
		logHandler.setFormatter(new Formatter(){
			public String format(LogRecord record) {
				return record.getMessage();
			}			
		});
		logHandler.setLevel(Level.ALL);
		Logger log = Logger.getLogger(Main.class.getPackage().getName());
		log.setLevel(loggerLevel);
		log.addHandler(logHandler);
		log = Logger.getLogger(MonitorDataRecorder.class.getPackage().getName());
		log.setLevel(loggerLevel);
		log.addHandler(logHandler);
				
		for(Handler h:Logger.getGlobal().getHandlers())Logger.getGlobal().removeHandler(h);
		Logger.getGlobal().addHandler(customHandler);
	}
	
	public static void main(String[] args){			
		//initialize & setup GUI		
		Main main = new Main();
		main.start.addActionListener(main);
		main.stop.addActionListener(main);
		
		JPanel content = new JPanel();
		content.add(new JScrollPane(main.output));
		
		setLogging(args, new GUILogHandler(main.output));
				
		 
		//load settings
		try {
			main.loadFiles();
			ProcessBuilder kismetServer = new ProcessBuilder("kismet_server");
			kismetServer.start();
		} catch (Exception e) {
			Logger.getGlobal().severe("\n File error: "+e.getMessage());
		}	
		
		main.cboRecorders.addItem(new DeviceRecorder(main.url, main.scannerId, main.ouis));
		main.cboRecorders.addItem(new FrameRecorder(main.url, main.scannerId, main.ouis));
		
		JPanel controls = new JPanel();
		controls.add(main.start);
		controls.add(main.stop);
		controls.add(main.limitSpinner);
		controls.add(main.cboRecorders);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(content);
		mainPanel.add(controls);
		
		JFrame frame = new JFrame();
		frame.add(mainPanel);
		frame.pack();
		frame.setVisible(true);		
	}
}
