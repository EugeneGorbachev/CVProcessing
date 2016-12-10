package interfaces;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

public class ServoMotorControl extends CameraHolder {
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    private SerialPort serialPort;

    private InputStream inputStream;
    private OutputStream outputStream;


    public ServoMotorControl() {
    }

    @Override
    public boolean setUpConnection(Map<String, Object> parameters) throws Exception {
        connected = false;

        String portName = (String) parameters.get("portName");
        if (portName == null) {
            throw new Exception("There is no 'portName' value in parameters.");
        }

        // check does a port with obtained name exist
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
            throw new Exception("COM port not found.");
        }

        // open serial port, and use class name for the appName.
        serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
        // set port parameters TODO control setting up
        serialPort.setSerialPortParams(DATA_RATE,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        // open the streams
        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
        // add event listeners
        serialPort.addEventListener(this::serialEvent);
        serialPort.notifyOnDataAvailable(true);

        connected = true;
        return true;
    }

    @Override
    public void closeConnection() throws Exception {
        serialPort.removeEventListener();// TODO check is this require
        serialPort.close();
    }

    public void sendSingleByte(byte myByte) {
        try {
            outputStream.write(myByte);
//            outputStream.flush();// TODO discover why it lead to fatal error
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    // TODO check if this require
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
