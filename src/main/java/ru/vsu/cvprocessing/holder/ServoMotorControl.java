package ru.vsu.cvprocessing.holder;

import com.fazecast.jSerialComm.SerialPort;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

public class ServoMotorControl extends CameraHolder {
    private static final Logger log = Logger.getLogger(ServoMotorControl.class);

    private Camera camera;

    private SerialPort commPort;
    private OutputStream outputStream;

    public ServoMotorControl(Camera camera) {
        super(0, 180, 80, 140);
        this.camera = camera;
    }

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

    public void moveHorizontal(boolean detected, int value) {
        int preferences = convertToInt(detected, false);
        sendInt(preferences);
        log.info(String.format("Sent preferences detected = %s, horizontal to Arduino as %8s",
                Boolean.toString(detected),
                Integer.toBinaryString((preferences & 0xFF) + 0x100).substring(1)
        ));

        try {
            setHorizontalAngle(value);
        } catch (Exception e) {
            log.error(e);
        }
        sendInt(getHorizontalAngle());
        log.info(String.format("Sent %d horizontal angle to Arduino as %8s",
                getHorizontalAngle(),
                Integer.toBinaryString((getHorizontalAngle() & 0xFF) + 0x100).substring(1)
        ));
    }

    public void moveVertical(boolean detected, int value) {
        int preferences = convertToInt(detected, true);
        sendInt(preferences);
        log.info(String.format("Sent preferences detected = %s, vertical to Arduino as %8s",
                Boolean.toString(detected),
                Integer.toBinaryString((preferences & 0xFF) + 0x100).substring(1)
        ));

        try {
            setVerticalAngle(value);
        } catch (Exception e) {
            log.error(e);
        }
        sendInt(getVerticalAngle());
        log.info(String.format("Sent %d vertical angle to Arduino as %8s",
                getVerticalAngle(),
                Integer.toBinaryString((getVerticalAngle() & 0xFF) + 0x100).substring(1)
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

    /* Getters and setters */
    public Camera getCamera() {
        return camera;
    }
    /* Getters and setters */

    /* Static methods */
    public static byte mapIntToByteValue(int value) {
        if (value == 0)
            return 0;
        return (byte) Math.round((value * 127d) / 180d);
    }
    /* Static methods */
}
