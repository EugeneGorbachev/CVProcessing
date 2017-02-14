package ru.vsu.cvprocessing.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ColorChangedPublisher {
    private final ApplicationEventPublisher publisher;

    @Autowired
    public ColorChangedPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(ColorChangedEvent event) {
        publisher.publishEvent(event);
    }
}
