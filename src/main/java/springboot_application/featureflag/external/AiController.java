package springboot_application.featureflag.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

/**
 * REST controller that proxies AI service capabilities to the frontend.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiServiceClient aiServiceClient;

    public AiController(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @PostMapping("/process-document")
    public ResponseEntity<String> processDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "use_llm", defaultValue = "false") boolean useLlm) {
        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String result = aiServiceClient.processDocument(base64, useLlm);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to process document", e);
            return ResponseEntity.internalServerError().body("{\"error\":\"processing failed\"}");
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generate(
            @RequestBody Map<String, Object> entities,
            @RequestParam(value = "use_llm", defaultValue = "false") boolean useLlm) {
        String result = aiServiceClient.generateConvocatoria(entities, useLlm);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> payload) {
        String sessionId = (String) payload.getOrDefault("session_id", "default");
        String message = (String) payload.getOrDefault("message", "");
        boolean useLlm = Boolean.parseBoolean(payload.getOrDefault("use_llm", "false").toString());
        String result = aiServiceClient.chat(sessionId, message, useLlm);
        return ResponseEntity.ok(result);
    }
}