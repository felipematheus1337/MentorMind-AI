package mentormind.ai.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import mentormind.ai.llms.OpenAILLMImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentor-mind")
@RequiredArgsConstructor
public class MentorMindController {

    private final OpenAILLMImpl openAILLM;

    @GetMapping
    public String getAnswer(@RequestParam(name = "question", required = true) @NotBlank String question,
                            @RequestParam(name = "level", required = true) @NotEmpty AnswerLevel level) {

        return openAILLM.call(question, level);
    }

    @GetMapping("/json")
    public ResponseEntity<AnswerDTO> getAnswerWithJSONResponse(@RequestParam(name = "question",
                                                                           required = true) @NotBlank String question,
                                                               @RequestParam(name = "level",
                                                                       required = true) @NotEmpty AnswerLevel level) {
        return ResponseEntity.ok(openAILLM.callJSON(question, level));

    }
}
