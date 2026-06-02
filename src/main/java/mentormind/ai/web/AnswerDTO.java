package mentormind.ai.web;

import java.time.LocalDateTime;

public record AnswerDTO(LocalDateTime date, String resume, AnswerLevel answerLevel) {
}
