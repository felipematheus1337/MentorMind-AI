package mentormind.ai.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import mentormind.ai.annotations.ValidDocumento;
import mentormind.ai.llms.OpenAILLMImpl;
import mentormind.ai.rag.MentorMindRagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mentor-mind")
@RequiredArgsConstructor
@Validated
public class MentorMindController {

    private final OpenAILLMImpl openAILLM;
    private final MentorMindRagService ragService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestParam("file") @ValidDocumento MultipartFile file) {

        ragService.ingest(file);

    }

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
