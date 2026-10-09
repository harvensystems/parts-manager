package app.harven.partsmanager.config;

import app.harven.partsmanager.dto.AppSettingDto;
import app.harven.partsmanager.service.AppSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.support.GenericApplicationContext;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiConfigTest {

    @Mock
    private AppSettingService settingService;

    private final AiConfig aiConfig = new AiConfig();

    @Test
    void chatClientBuilder_withGeminiProvider_createsBuilder() {
        AppSettingDto dto = new AppSettingDto();
        dto.setAiProvider("gemini");
        dto.setCustomApiKey("AIzaSyFakeGeminiKey123");

        when(settingService.getSettings()).thenReturn(Mono.just(dto));

        ChatClient.Builder builder = aiConfig.chatClientBuilder(settingService);

        assertNotNull(builder);
    }

    @Test
    void chatClientBuilder_withOpenAiProvider_createsBuilder() {
        AppSettingDto dto = new AppSettingDto();
        dto.setAiProvider("openai");
        dto.setCustomApiKey("sk-fake-openai-key-12345");

        when(settingService.getSettings()).thenReturn(Mono.just(dto));

        ChatClient.Builder builder = aiConfig.chatClientBuilder(settingService);

        assertNotNull(builder);
    }

    @Test
    void chatClientBuilder_withBlankApiKey_returnsNull() {
        AppSettingDto dto = new AppSettingDto();
        dto.setAiProvider("gemini");
        dto.setCustomApiKey("   ");

        when(settingService.getSettings()).thenReturn(Mono.just(dto));

        ChatClient.Builder builder = aiConfig.chatClientBuilder(settingService);

        assertNull(builder);
    }

    @Test
    void chatClientBuilder_withNullApiKey_returnsNull() {
        AppSettingDto dto = new AppSettingDto();
        dto.setAiProvider("gemini");
        dto.setCustomApiKey(null);

        when(settingService.getSettings()).thenReturn(Mono.just(dto));

        ChatClient.Builder builder = aiConfig.chatClientBuilder(settingService);

        assertNull(builder);
    }

    @Test
    void chatClientBuilder_withUnsupportedProvider_returnsNull() {
        AppSettingDto dto = new AppSettingDto();
        dto.setAiProvider("unsupported_provider");
        dto.setCustomApiKey("some-key");

        when(settingService.getSettings()).thenReturn(Mono.just(dto));

        ChatClient.Builder builder = aiConfig.chatClientBuilder(settingService);

        assertNull(builder);
    }

    @Test
    void chatClientBuilder_withErrorInSettingService_returnsNull() {
        when(settingService.getSettings()).thenReturn(Mono.error(new RuntimeException("DB error")));

        ChatClient.Builder builder = aiConfig.chatClientBuilder(settingService);

        assertNull(builder);
    }

    @Test
    void chatClient_withNonNullBuilder_returnsChatClient() {
        ChatClient.Builder builder = AiConfig.buildChatClientBuilder("gemini", "AIzaSyFakeGeminiKey123");
        assertNotNull(builder);
        ChatClient chatClient = aiConfig.chatClient(builder);
        assertNotNull(chatClient);
    }

    @Test
    void chatClient_withNullBuilder_returnsNull() {
        ChatClient chatClient = aiConfig.chatClient(null);
        assertNull(chatClient);
    }

    @Test
    void recreateChatClientBean_registersBeanInApplicationContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();

        AiConfig config = new AiConfig(context);
        ChatClient chatClient = config.recreateChatClientBean("gemini", "AIzaSyFakeGeminiKey123");

        assertNotNull(chatClient);
        assertTrue(context.containsBean("chatClient"));
        assertTrue(context.containsBean("chatClientBuilder"));
        assertSame(chatClient, context.getBean("chatClient"));
    }

    @Test
    void recreateChatClientBean_withInvalidProvider_returnsNull() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();

        AiConfig config = new AiConfig(context);
        ChatClient chatClient = config.recreateChatClientBean("unsupported", "key");

        assertNull(chatClient);
        assertFalse(context.containsBean("chatClient"));
    }
}
