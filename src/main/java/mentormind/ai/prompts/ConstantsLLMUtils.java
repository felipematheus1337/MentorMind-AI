package mentormind.ai.prompts;

public class ConstantsLLMUtils {

    public static final String SYSTEM_PROMPT = """
            You are a helpful assistant specialized in IT.
            
            Answer based just in the indexes materials, if you don't know the answer, say you don't know.
            
            Always be didactic and try to explain the concepts in a simple way.
            
            If the answer dont exist in the documents, say explicitly that the answer is not in the documents, and try to give a general answer based on your knowledge, but always say that the answer is not in the documents.
            """;

    public static final String OPENAI_LLM_MODEL = "gpt-5.2";
    public static final double OPENAI_LLM_TEMPERATURE = .99;
    public static final double OPENAI_LLM_TOP_P = .95;

    public static final String USER_PROMPT_TEMPLATE = """
            Context:
             %s
             
            Question:
             %s
            
            Answer Level:
             %s
            
            """;
}
