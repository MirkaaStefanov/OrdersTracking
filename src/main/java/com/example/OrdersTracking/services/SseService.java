package com.example.OrdersTracking.services;

import com.example.OrdersTracking.models.User;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    // Use a thread-safe list to store all connected clients
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Called by the controller to add a new client
    public SseEmitter subscribe() {
        // Set a long timeout
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // When the connection completes, remove it from the list
        emitter.onCompletion(() -> emitters.remove(emitter));
        // When it times out, remove it
        emitter.onTimeout(() -> emitters.remove(emitter));
        // Add to the list
        emitters.add(emitter);

        return emitter;
    }

    // Called by your UserController when a new user is saved
    public void sendNewUserEvent(User user) {
        // Send the new user event to all connected clients
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-user") // Event name
                        .data(user));     // Event data (Spring will serialize User to JSON)
            } catch (IOException e) {
                // If this client is disconnected, remove them
                emitters.remove(emitter);
            }
        }
    }
}