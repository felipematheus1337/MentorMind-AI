package mentormind.ai.llms;

public interface LLMGenericInterface<T> {

    T call(String prompt);
}
