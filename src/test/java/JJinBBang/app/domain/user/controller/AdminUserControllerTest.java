package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.slack.service.SlackRequestVerifier;
import JJinBBang.app.global.slack.service.SlackService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest {

    private final UsersService usersService = mock(UsersService.class);
    private final SlackService slackService = mock(SlackService.class);
    private final SlackRequestVerifier slackRequestVerifier = mock(SlackRequestVerifier.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AdminUserController(usersService, slackService, slackRequestVerifier))
            .build();

    @Test
    void handleInteraction_returnsRawSlackResponseJson() throws Exception {
        String payload = "{\"type\":\"block_actions\"}";
        String body = "payload=%7B%22type%22%3A%22block_actions%22%7D";
        String slackResponse = "{\"replace_original\":true,\"text\":\"승인 처리되었습니다.\"}";

        when(slackRequestVerifier.isValid(eq("1234567890"), eq("v0=signature"), eq(body))).thenReturn(true);
        when(slackService.handleInteractivity(eq(payload))).thenReturn(slackResponse);

        mockMvc.perform(post("/api/admin/slack/verification")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Slack-Request-Timestamp", "1234567890")
                        .header("X-Slack-Signature", "v0=signature")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.replace_original").value(true))
                .andExpect(jsonPath("$.text").value("승인 처리되었습니다."))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void handleInteraction_rejectsInvalidSlackSignature() throws Exception {
        String body = "payload=%7B%22type%22%3A%22block_actions%22%7D";

        when(slackRequestVerifier.isValid(eq("1234567890"), eq("v0=invalid"), eq(body))).thenReturn(false);

        mockMvc.perform(post("/api/admin/slack/verification")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Slack-Request-Timestamp", "1234567890")
                        .header("X-Slack-Signature", "v0=invalid")
                        .content(body))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(slackService);
    }

    @Test
    void handleInteraction_rejectsMissingPayload() throws Exception {
        String body = "not_payload=value";

        when(slackRequestVerifier.isValid(eq("1234567890"), eq("v0=signature"), eq(body))).thenReturn(true);

        mockMvc.perform(post("/api/admin/slack/verification")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Slack-Request-Timestamp", "1234567890")
                        .header("X-Slack-Signature", "v0=signature")
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(slackService);
    }
}
