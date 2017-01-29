package ru.vsu.cvprocessing.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class IRMethodPublisher{
    private final ApplicationEventPublisher publisher;

    @Autowired
    public IRMethodPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(ChangeIRMethodEvent event) {
        publisher.publishEvent(event);
    }
}
