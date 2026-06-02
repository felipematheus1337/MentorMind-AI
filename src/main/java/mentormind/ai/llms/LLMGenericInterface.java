package mentormind.ai.llms;

import mentormind.ai.web.AnswerDTO;
import mentormind.ai.web.AnswerLevel;

public interface LLMGenericInterface<T> {

    T call(String prompt, AnswerLevel level);

    AnswerDTO callJSON(String question, AnswerLevel level);
}
