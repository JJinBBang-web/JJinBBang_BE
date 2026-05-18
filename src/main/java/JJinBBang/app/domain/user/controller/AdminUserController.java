package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.slack.service.SlackRequestVerifier;
import JJinBBang.app.global.slack.service.SlackService;
import JJinBBang.app.global.template.ResTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UsersService usersService;
    private final SlackService slackService;
    private final SlackRequestVerifier slackRequestVerifier;

    @PostMapping(
            value = "/slack/verification",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<String> handleInteraction(
            @RequestBody String body,
            @RequestHeader(value = "X-Slack-Request-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Slack-Signature", required = false) String signature
    ) throws JsonProcessingException {
        if (!slackRequestVerifier.isValid(timestamp, signature, body)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Slack signature");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(slackService.handleInteractivity(extractPayload(body)));
    }

    private String extractPayload(String body) {
        for (String pair : body.split("&")) {
            int separatorIndex = pair.indexOf('=');
            if (separatorIndex < 0) {
                continue;
            }

            String key = URLDecoder.decode(pair.substring(0, separatorIndex), StandardCharsets.UTF_8);
            if ("payload".equals(key)) {
                return URLDecoder.decode(pair.substring(separatorIndex + 1), StandardCharsets.UTF_8);
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Slack payload");
    }

    @DeleteMapping("/deletedUsers/executeDeletion")
    public ResTemplate<?> executeUserDeletion() {
        usersService.forceDeleteExecute();
        return new ResTemplate<>(
                HttpStatus.OK,
                "탈퇴한 유저의 데이터 영구 삭제 완료",
                null
        );
    }


}
