package mentormind.ai.llms;

import mentormind.ai.prompts.ConstantsLLMUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;

@Component

public class OpenAILLMImpl implements LLMGenericInterface<String> {

    private static final ChatOptions options = ChatOptions
            .builder()
            .model(ConstantsLLMUtils.OPENAI_LLM_MODEL)
            .temperature(ConstantsLLMUtils.OPENAI_LLM_TEMPERATURE)
            .topP(ConstantsLLMUtils.OPENAI_LLM_TOP_P)
            .build();
    private final ChatClient client;

    public OpenAILLMImpl(ChatClient.Builder builder) {
        this.client = builder
                .defaultSystem(ConstantsLLMUtils.SYSTEM_PROMPT)
                .defaultOptions(options.mutate())
                .build();
    }

    @Override
    public String call(String prompt) {
        return this.client
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
