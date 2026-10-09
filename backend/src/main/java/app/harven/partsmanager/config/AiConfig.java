package app.harven.partsmanager.config;

import app.harven.partsmanager.service.AppSettingService;
import com.google.genai.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class AiConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public AiConfig() {
    }

    public AiConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static ChatClient.Builder buildChatClientBuilder(String provider, String apiKey) {
        if (provider != null && apiKey != null && !apiKey.trim().isEmpty()) {
            String trimmedKey = apiKey.trim();
            if ("gemini".equalsIgnoreCase(provider) || provider.toLowerCase().startsWith("gemini")) {
                Client genAiClient = Client.builder()
                        .apiKey(trimmedKey)
                        .build();
                String modelName = "gemini".equalsIgnoreCase(provider) ? "gemini-2.5-flash" : provider;
                ChatModel chatModel = GoogleGenAiChatModel.builder()
                        .genAiClient(genAiClient)
                        .options(GoogleGenAiChatOptions.builder().model(modelName).build())
                        .build();
                return ChatClient.builder(chatModel);
            }
            if ("openai".equalsIgnoreCase(provider) || provider.toLowerCase().startsWith("gpt")) {
                String modelName = "openai".equalsIgnoreCase(provider) ? "gpt-4o" : provider;
                ChatModel chatModel = OpenAiChatModel.builder()
                        .options(OpenAiChatOptions.builder()
                                .apiKey(trimmedKey)
                                .model(modelName)
                                .build())
                        .build();
                return ChatClient.builder(chatModel);
            }
        }
        return null;
    }

    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder(AppSettingService settingService) {
        return settingService.getSettings()
                .timeout(Duration.ofSeconds(5))
                .flatMap(dto -> {
                    ChatClient.Builder builder = buildChatClientBuilder(dto.getAiProvider(), dto.getCustomApiKey());
                    return builder != null ? Mono.just(builder) : Mono.empty();
                })
                .onErrorResume(e -> Mono.empty())
                .block();
    }

    @Bean
    @Primary
    public ChatClient chatClient(@Autowired(required = false) ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder != null ? chatClientBuilder.build() : null;
    }

    public ChatClient recreateChatClientBean(String provider, String apiKey) {
        ChatClient.Builder builder = buildChatClientBuilder(provider, apiKey);
        if (builder == null) {
            return null;
        }
        ChatClient chatClient = builder.build();
        if (applicationContext != null && applicationContext.getAutowireCapableBeanFactory() instanceof DefaultListableBeanFactory beanFactory) {
            if (beanFactory.containsSingleton("chatClientBuilder")) {
                beanFactory.destroySingleton("chatClientBuilder");
            }
            beanFactory.registerSingleton("chatClientBuilder", builder);

            if (beanFactory.containsSingleton("chatClient")) {
                beanFactory.destroySingleton("chatClient");
            }
            beanFactory.registerSingleton("chatClient", chatClient);
        }
        return chatClient;
    }
}
