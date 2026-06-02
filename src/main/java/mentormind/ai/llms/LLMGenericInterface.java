package mentormind.ai.llms;

import mentormind.ai.web.AnswerLevel;

public interface LLMGenericInterface<T> {

    T call(String prompt, AnswerLevel level);
}
