package mentormind.ai.web;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import mentormind.ai.llms.OpenAILLMImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentor-mind")
@RequiredArgsConstructor
public class MentorMindController {

    private final OpenAILLMImpl openAILLM;

    public String getAnswer(@NotBlank String question) {

        return openAILLM.call(question);
    }
}
