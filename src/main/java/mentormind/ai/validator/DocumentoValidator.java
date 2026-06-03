package mentormind.ai.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import mentormind.ai.annotations.ValidDocumento;
import org.apache.tika.Tika;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public class DocumentoValidator implements ConstraintValidator<ValidDocumento, MultipartFile> {

    private static final Set<String> EXTENSOES = Set.of("md", "txt", "pdf");
    private final Tika tika = new Tika();

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext ctx) {
        if (file == null || file.isEmpty()) return false;

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || !EXTENSOES.contains(ext.toLowerCase())) {
            return false;
        }

        try {
            String tipoReal = tika.detect(file.getInputStream());
            return switch (ext.toLowerCase()) {
                case "pdf"        -> "application/pdf".equals(tipoReal);
                case "md", "txt"  -> tipoReal.startsWith("text/"); // ambos = text/plain
                default           -> false;
            };
        } catch (IOException e) {
            return false;
        }
    }
}
