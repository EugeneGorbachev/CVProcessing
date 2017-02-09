package ru.vsu.cvprocessing.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SendingDataPublisher {
    private final ApplicationEventPublisher publisher;

    @Autowired
    public SendingDataPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(SendingDataEvent event) {
        publisher.publishEvent(event);
    }
}
