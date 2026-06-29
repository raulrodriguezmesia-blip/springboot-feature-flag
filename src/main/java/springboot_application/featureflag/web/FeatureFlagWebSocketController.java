package springboot_application.featureflag.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import springboot_application.featureflag.service.WebSocketService;

@Controller
public class FeatureFlagWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagWebSocketController.class);

    private final WebSocketService webSocketService;

    public FeatureFlagWebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @MessageMapping("/flag-updates")
    @SendTo("/topic/flags")
    public String handleFlagUpdate(String message) {
        log.info("Received WebSocket message: {}", message);
        return message;
    }
}