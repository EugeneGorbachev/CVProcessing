package interfaces;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

// todo delete implementing interface
public class ServoMotorControl extends CameraHolder implements SerialPortEventListener {
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    private SerialPort serialPort;

    private InputStream inputStream;
    private OutputStream outputStream;


    public ServoMotorControl() {
        super(0, 179, 0, 0);
    }

    @Override
    public boolean setUpConnection(Map<String, Object> parameters) throws Exception {
        connected = false;

        String portName = (String) parameters.get("portName");
        if (portName == null) {
            throw new Exception("There is no 'portName' value in parameters.");
        }

        // check does a port with received name exist
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
        serialPort.addEventListener(this::serialEvent);// TODO check is this require
        serialPort.notifyOnDataAvailable(true);

        connected = true;
        return true;
    }

    @Override
    public void closeConnection() {
        if (serialPort != null) {
            serialPort.removeEventListener();// TODO check is this require
            serialPort.close();
        }
    }

    @Override
    public void setHorizontalAngle(int horizontalAngle) {
        super.setHorizontalAngle(horizontalAngle);
        sendSingleByte(mapIntToByteValue(horizontalAngle));
    }

    // todo check correct working
    private byte mapIntToByteValue(int value) {
        double convertedValue = ((double) (value + 1) * 128d) / 180d;
        return (byte) (Math.round(convertedValue / 128) - 1);
    }

    private void sendSingleByte(byte myByte) {
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
