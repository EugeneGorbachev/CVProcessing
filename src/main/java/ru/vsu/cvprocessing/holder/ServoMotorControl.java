package ru.vsu.cvprocessing.holder;

import com.fazecast.jSerialComm.SerialPort;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServoMotorControl extends CameraHolder {
    private static final Logger log = Logger.getLogger(ServoMotorControl.class);

    private SerialPort commPort;
    private OutputStream outputStream;

    public ServoMotorControl() {
        super(50, 180, 90, 110);// TODO hardcode
    }

    /* Open/Close connection methods */
    @Override
    public void setUpConnection(Map<String, Object> parameters) throws Exception {
        connected = false;

        String portName = (String) parameters.get("portName");
        checkNotNull(portName, "Port name required");
        ArrayList<String> portNames = new ArrayList<>();
        for (SerialPort serialPort : SerialPort.getCommPorts()) {
            portNames.add(serialPort.getSystemPortName());
        }
        if (!portNames.contains(portName)) {
            throw new Exception("Can't find port with name \"" + portName + "\"");
        }
        commPort = SerialPort.getCommPort(portName);
        commPort.openPort();

        commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        outputStream = commPort.getOutputStream();

        connected = true;
    }

    @Override
    public boolean closeConnection() {
        if (commPort != null) {
            connected = false;
            commPort.closePort();
        }
        return !isConnected();
    }

    /* Send moving signal and accessory methods */
    public void moveServo(boolean detected, boolean vertical, int value) {
        int preferences = convertToInt(detected, vertical);
        sendInt(preferences);
        log.info(String.format("Preferences sent detected = %s, %s to COM port as %8s",
                Boolean.toString(detected),
                vertical ? "vertical" : "horizontal",
                Integer.toBinaryString((preferences & 0xFF) + 0x100).substring(1)
        ));

        try {
            if (vertical) {
                setVerticalAngle(value);
            } else {
                setHorizontalAngle(value);
            }
        } catch (IndexOutOfBoundsException e) {
            log.warn(e);
        }

        sendInt(vertical ? getVerticalAngle() : getHorizontalAngle());
        log.info(String.format("%s angle %d sent to COM port as %8s",
                vertical ? "Vertical" : "Horizontal",
                value,
                Integer.toBinaryString((value & 0xFF) + 0x100).substring(1)
        ));
    }

    private int convertToInt(boolean detected, boolean vertical) {
        int preferences = 0;
        if (detected) {
            preferences = preferences | (1 << 0);
        }
        if (vertical) {
            preferences = preferences | (1 << 1);
        }
        return preferences;
    }

    private void sendInt(int intValue) {
        try {
            outputStream.write(intValue);
            outputStream.flush();
        } catch (IOException e) {
            log.error(e);
        }
    }
}
