package controllers;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class ServoControl implements SerialPortEventListener {
    private SerialPort serialPort;
    private InputStream inputStream;
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
            System.out.println("COM port not found.");
            throw new Exception("COM port not found.");
        } else {
            System.out.println("Found your Port");
        }
        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            // open the streams
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }


    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
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


    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int myByte = inputStream.read();
                int value = myByte & 0xff;//byte to int conversion:0...127,-127...0 -> 0...255
                if (value >= 0 && value < 256) {
                    System.out.println(value);

                }
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }
}
