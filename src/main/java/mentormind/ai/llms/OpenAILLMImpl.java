package mentormind.ai.llms;

import mentormind.ai.prompts.ConstantsLLMUtils;
import mentormind.ai.web.AnswerLevel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    public String call(String prompt, AnswerLevel level) {


        String promptToUse = createAnswerPrompt(prompt, level);

        PromptTemplate promptTemplate = new PromptTemplate(promptToUse);
        promptTemplate.render(Map.of(
                "question", prompt,
                "level", level.name()
        ));

        return this.client
                .prompt()
                .user(promptTemplate.getTemplate())
                .call()
                .content();
    }

    private String createAnswerPrompt(String question, AnswerLevel level) {
        return String.format(ConstantsLLMUtils.USER_PROMPT_TEMPLATE, question, level.name());
    }
}
