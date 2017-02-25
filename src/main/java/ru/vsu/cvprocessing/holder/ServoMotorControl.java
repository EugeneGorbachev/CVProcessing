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

    private Camera camera;

    private SerialPort commPort;
    private OutputStream outputStream;

    public ServoMotorControl(Camera camera) {
        super(0, 180, 0, 180);
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

    public void sendInt(int intValue) {
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
