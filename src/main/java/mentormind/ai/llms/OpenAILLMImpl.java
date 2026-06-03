package mentormind.ai.llms;

import mentormind.ai.prompts.ConstantsLLMUtils;
import mentormind.ai.web.AnswerDTO;
import mentormind.ai.web.AnswerLevel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
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
    private final PgVectorStore vectorStore;

    public OpenAILLMImpl(ChatClient.Builder builder, PgVectorStore vectorStore) {
        this.client = builder
                .defaultSystem(ConstantsLLMUtils.SYSTEM_PROMPT)
                .defaultOptions(options.mutate())
                .build();
        this.vectorStore = vectorStore;
    }

    @Override
    public String call(String prompt, AnswerLevel level) {


        var client = generateClient(prompt, level);

        return client.prompt().call().content();
    }

    @Override
    public AnswerDTO callJSON(String question, AnswerLevel level) {

        BeanOutputConverter<AnswerDTO> converter = new BeanOutputConverter<>(AnswerDTO.class);
        var client = generateClient(question, level);

        return client.prompt()
                .call()
                .entity(converter);
    }

    private ChatClient generateClient( String prompt, AnswerLevel level) {

        var context = vectorStore.similaritySearch(SearchRequest
                .builder()
                        .query(prompt)
                        .topK(5)
                .build());

        var fullRagContext = context.stream().map(Document::getText)
                .reduce("", (a, b) -> a + "\n---\n" + b);

        String promptToUse = createAnswerPrompt(prompt, level, fullRagContext);

        PromptTemplate promptTemplate = new PromptTemplate(promptToUse);
        promptTemplate.render(Map.of(
                "question", prompt,
                "level", level.name(),
                "context", fullRagContext
        ));

        return this.client
                .prompt()
                .user(promptTemplate.getTemplate())
                .mutate().build();
    }

    private String createAnswerPrompt(String question, AnswerLevel level, String context) {
        return String.format(ConstantsLLMUtils.USER_PROMPT_TEMPLATE, question, level.name(), context);
    }

}
