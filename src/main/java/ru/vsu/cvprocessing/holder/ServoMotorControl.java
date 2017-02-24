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

    @Override
    public void update(boolean isDetected, int x, int y) {// TODO replace with event
        byte sendingValue = 0;
        try {
            if (getHorizontalAngleMaxValue() > 0) {// TODO send anyway
                if (getHorizontalAngle() != x) {
                    setHorizontalAngle(getHorizontalAngle() +
                            (int) Math.round((double) x / (camera.getWidth() / camera.getFieldOfView()))
                    );
                    sendingValue = mapIntToByteValue(getHorizontalAngle());
                    if (isDetected) {
                        sendingValue = (byte) (sendingValue | (1 << 7));// set unused bit to 1
                    }
                }
                sendInt(sendingValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendingValue = 0;
        try {
            if (getVerticalAngleMaxValue() > 0) {// TODO send anyway
                if (getVerticalAngle() != y) {
                    setVerticalAngle(getVerticalAngle() +
                            (int) Math.round((double) y / (camera.getWidth() / camera.getFieldOfView()))
                    );
                    sendingValue = mapIntToByteValue(getVerticalAngle());
                    if (isDetected) {
                        sendingValue = (byte) (sendingValue | (1 << 7));// set unused bit to 1
                    }
                }
                sendInt(sendingValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
