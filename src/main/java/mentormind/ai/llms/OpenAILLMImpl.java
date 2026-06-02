package mentormind.ai.llms;

import org.springframework.stereotype.Component;

@Component

public class OpenAILLMImpl implements LLMGenericInterface<String> {
    @Override
    public String call(String prompt) {
        return "";
    }
}
