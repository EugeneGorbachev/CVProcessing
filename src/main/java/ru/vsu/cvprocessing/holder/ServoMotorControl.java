package ru.vsu.cvprocessing.holder;

import com.fazecast.jSerialComm.SerialPort;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

public class ServoMotorControl extends CameraHolder {
    private Camera camera;

    private SerialPort commPort;
    private OutputStream outputStream;

    public ServoMotorControl(Camera camera) {
        super(0, 180, 0, 0);
        this.camera = camera;
    }

    @Override
    public void setUpConnection(Map<String, Object> parameters) throws Exception {
        connected = false;

        String portName = (String) parameters.get("portName");
        if (portName == null) {
            throw new Exception("There is no 'portName' value in parameters.");
        }
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
    public void closeConnection() {
        if (commPort != null) {
            connected = false;
            commPort.closePort();
        }
    }

    public void sendSingleByte(byte myByte) {
        try {
            outputStream.write(myByte);
            outputStream.flush();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void update(boolean isDetected, int x, int y) {
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
                sendSingleByte(sendingValue);
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
                sendSingleByte(sendingValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Getters and setters */
    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
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
