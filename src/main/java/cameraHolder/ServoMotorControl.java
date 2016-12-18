package cameraHolder;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

public class ServoMotorControl extends CameraHolder {
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    private SerialPort serialPort;

    private OutputStream outputStream;

    public ServoMotorControl() {
        super(0, 180, 0, 0);
    }

    @Override
    public void setUpConnection(Map<String, Object> parameters) throws Exception {
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
            throw new Exception("COM port \"" + portName + "\" not found.");
        }

        // open serial port, and use class name for the appName.
        serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
        // set port parameters TODO control setting up
        serialPort.setSerialPortParams(DATA_RATE,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        // open the streams
        outputStream = serialPort.getOutputStream();
        // add event listeners
        serialPort.notifyOnDataAvailable(true);

        connected = true;
    }

    @Override
    public void closeConnection() {
        if (serialPort != null) {
            connected = false;
            serialPort.close();
        }
    }

    @Override
    public void setHorizontalAngle(int horizontalAngle) {
        super.setHorizontalAngle(horizontalAngle);
        sendSingleByte(mapIntToByteValue(horizontalAngle));
    }

    private byte mapIntToByteValue(int value) {
        if (value == 0)
            return 0;
        return (byte) Math.round((value * 127d) / 180d);
    }

    private void sendSingleByte(byte myByte) {
        try {
            outputStream.write(myByte);
//            outputStream.flush();// TODO discover why this method calling lead to fatal error
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void update(int x, int y) {
        if (horizontalAngle != x) {
            System.out.println("Get new x value = " + x);
            // TODO move on diff
        }
        if (verticalAngle != y) {
            System.out.println("Get new y value = " + y);
            // TODO move on diff
        }
    }
}
