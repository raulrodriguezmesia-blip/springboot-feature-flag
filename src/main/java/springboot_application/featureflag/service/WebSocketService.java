package springboot_application.featureflag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import springboot_application.featureflag.FeatureFlag;

@Service
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyFlagChange(String operation, String key, FeatureFlag flag) {
        log.info("Sending WebSocket notification: {} for key={}", operation, key);
        messagingTemplate.convertAndSend("/topic/flags",
            new FlagUpdateMessage(operation, key, flag));
    }

    public record FlagUpdateMessage(String operation, String key, FeatureFlag flag) {}
}