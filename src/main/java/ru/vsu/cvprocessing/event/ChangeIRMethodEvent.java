package ru.vsu.cvprocessing.event;

import org.springframework.context.ApplicationEvent;
import ru.vsu.cvprocessing.recognition.ImageRecognitionMethod;

public class ChangeIRMethodEvent extends ApplicationEvent {
    private ImageRecognitionMethod oldValue;
    private ImageRecognitionMethod newValue;

    public ChangeIRMethodEvent(Object source, ImageRecognitionMethod oldValue, ImageRecognitionMethod newValue) {
        super(source);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ImageRecognitionMethod getOldValue() {
        return oldValue;
    }

    public ImageRecognitionMethod getNewValue() {
        return newValue;
    }
}
