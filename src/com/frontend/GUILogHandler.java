package com.frontend;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.JTextArea;

public class GUILogHandler extends Handler {
	
	private JTextArea output;

	public GUILogHandler(JTextArea output) {
		this.output = output;
	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public void publish(LogRecord record) {
		if(this.isLoggable(record))
			output.append(record.getMessage());
	}

}
