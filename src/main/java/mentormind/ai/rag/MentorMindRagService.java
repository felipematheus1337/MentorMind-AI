package mentormind.ai.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorMindRagService {

    private final PgVectorStore vectorStore;

    public void ingest(MultipartFile file) {
        try {
            List<Document> documents = parseFile(file);
            vectorStore.add(documents);
        } catch (IOException io) {
            throw new IngestionException("Falha ao ler o conteúdo do arquivo", io);
        } catch (RuntimeException e) {
            if (e instanceof IngestionException) throw e;
            throw new IngestionException("Falha ao processar o arquivo", e);
        }
    }

    private List<Document> parseFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new EmptyFileNameException();
        }

        Resource resource = toNamedResource(file.getBytes(), fileName);

        return new TikaDocumentReader(resource).get();
    }

    private Resource toNamedResource(byte[] bytes, String fileName) {
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }
    public static class IngestionException extends RuntimeException {
        public IngestionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EmptyFileNameException extends IngestionException {
        public EmptyFileNameException() {
            super("Falha ao ingerir: o nome do arquivo está vazio", null);
        }
    }
}