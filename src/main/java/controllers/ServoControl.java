package controllers;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.OutputStream;
import java.util.Enumeration;

public class ServoControl {
    private SerialPort serialPort;
    private OutputStream outputStream;
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    public void initialize(String portName) throws Exception {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(portName)) {
                portId = currPortId;
                break;
            }
        }

        if (portId == null) {
            throw new Exception("COM port '" + portName +"' not found.");
        }
        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            // open the stream
            outputStream = serialPort.getOutputStream();
            // add event listeners
//            serialPort.addEventListener(this);
//            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }


    public synchronized void close() {
        if (serialPort != null) {
//            serialPort.removeEventListener();
            serialPort.close();
        }
    }


    public void sendSingleByte(byte myByte) {
        try {
            outputStream.write(myByte);
//            outputStream.flush();// fuck up everything
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
