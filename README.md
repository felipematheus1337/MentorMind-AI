# MentorMind-AI

An AI-powered technical mentor built with **Spring Boot** and **Spring AI**, using **RAG (Retrieval-Augmented Generation)**, vector search and embeddings.

Upload your own PDFs, notes and documentation, then chat with an assistant that answers based on **your** knowledge base instead of generic LLM responses. The model is grounded on the documents you ingest: it retrieves the most semantically relevant chunks from a vector store and uses them as context to produce didactic, source-aware answers.

---

## Table of Contents

- [How It Works](#how-it-works)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running the Project](#running-the-project)
- [API Reference](#api-reference)
- [Document Validation](#document-validation)
- [Error Handling](#error-handling)
- [Roadmap](#roadmap)

---

## How It Works

MentorMind-AI follows a classic RAG pipeline:

1. **Ingestion** — A document (`.pdf`, `.txt` or `.md`) is uploaded. The file type is validated against its real MIME type (not just its extension) and parsed with Apache Tika.
2. **Embedding & storage** — The extracted content is converted into embeddings (1536 dimensions) and persisted in a **PostgreSQL + pgvector** vector store.
3. **Retrieval** — When a question is asked, the application runs a similarity search and pulls the top 5 most relevant chunks.
4. **Generation** — The retrieved chunks are injected into a prompt template alongside the question and the requested answer level, and sent to the OpenAI chat model.
5. **Response** — The model returns a grounded answer. If the information is not present in the documents, it explicitly states so before offering any general knowledge.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| AI | Spring AI 2.0.0-M8 |
| LLM provider | OpenAI |
| Vector store | PostgreSQL 16 + pgvector |
| Document parsing | Apache Tika + Spring AI PDF Document Reader |
| Validation | Spring Boot Starter Validation (Jakarta Bean Validation) |
| Boilerplate reduction | Lombok |
| Build | Maven (with wrapper) |
| Containerization | Docker Compose |

---

## Architecture

```
                ┌──────────────────────────────────────────────┐
                │            MentorMindController               │
                │   POST /api/mentor-mind        (upload)        │
                │   GET  /api/mentor-mind        (text answer)   │
                │   GET  /api/mentor-mind/json   (JSON answer)   │
                └───────────────┬──────────────────────────────┘
                                │
          ┌─────────────────────┴─────────────────────┐
          │                                             │
          ▼                                             ▼
┌────────────────────┐                      ┌────────────────────────┐
│ MentorMindRagService│  ingest             │     OpenAILLMImpl        │
│  - Tika parsing     │ ───────────────►    │  - similarity search     │
│  - vector store add │                      │  - prompt assembly       │
└─────────┬──────────┘                      │  - OpenAI chat call      │
          │                                  └────────────┬───────────┘
          ▼                                               │
┌────────────────────────────────────────────────────────┴───────────┐
│                  PgVectorStore (PostgreSQL + pgvector)               │
└──────────────────────────────────────────────────────────────────────┘
```

The LLM access is abstracted behind a generic interface (`LLMGenericInterface<T>`), making it straightforward to swap or add other providers in the future.

---

## Project Structure

```
src/main/java/mentormind/ai/
├── AiApplication.java                 # Spring Boot entry point
├── annotations/
│   └── ValidDocumento.java            # Custom validation annotation
├── exception/
│   └── UploadExceptionHandler.java    # Global REST exception handler
├── llms/
│   ├── LLMGenericInterface.java       # Provider-agnostic LLM contract
│   └── OpenAILLMImpl.java             # OpenAI implementation + RAG retrieval
├── prompts/
│   └── ConstantsLLMUtils.java         # System/user prompt templates & model config
├── rag/
│   └── MentorMindRagService.java      # Document ingestion into the vector store
├── validator/
│   └── DocumentoValidator.java        # MIME-based file validation logic
└── web/
    ├── AnswerDTO.java                 # Structured JSON response record
    ├── AnswerLevel.java               # EASY | HARD | DEFAULT
    └── MentorMindController.java      # REST endpoints

src/main/resources/
└── application.yaml                   # App, OpenAI, datasource & vector store config
```

---

## Prerequisites

- **Java 21**
- **Maven** (or use the bundled `./mvnw` wrapper)
- **Docker** and **Docker Compose**
- An **OpenAI API key**

---

## Configuration

### Environment variables

The application reads the OpenAI key from an environment variable:

```bash
export OPENAI_API_KEY=your-openai-api-key
```

### `application.yaml`

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
      timeout: 30s
    vectorstore:
      pgvector:
        initialize-schema: true
        dimensions: 1536
  datasource:
    url: jdbc:postgresql://localhost:5432/mentormindb
    username: postgres
    password: postgres
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

Model parameters live in `ConstantsLLMUtils` (model name, temperature and top-p) and can be tuned there.

> **Heads-up — database name mismatch:** the bundled `docker-compose.yml` creates a database named `ragdb`, while `application.yaml` connects to `mentormindb`. Before running, align the two — either change the datasource URL to `.../ragdb`, or set `POSTGRES_DB: mentormindb` in the Compose file. Otherwise the application will fail to connect.

---

## Running the Project

### 1. Start the vector database

```bash
docker compose up -d
```

This spins up a `pgvector/pgvector:pg16` container exposing PostgreSQL on port `5432`. The vector schema is initialized automatically on startup (`initialize-schema: true`).

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Reference

Base path: `/api/mentor-mind`

### Upload a document

Ingests a document into the knowledge base.

```http
POST /api/mentor-mind
Content-Type: multipart/form-data
```

| Param | Type | Description |
|-------|------|-------------|
| `file` | file | A `.pdf`, `.txt` or `.md` file (max 10MB) |

```bash
curl -X POST http://localhost:8080/api/mentor-mind \
  -F "file=@./my-notes.pdf"
```

### Ask a question (plain text)

```http
GET /api/mentor-mind?question={question}&level={level}
```

| Param | Type | Description |
|-------|------|-------------|
| `question` | string | The question to ask (required, non-blank) |
| `level` | enum | `EASY`, `HARD` or `DEFAULT` (required) |

```bash
curl "http://localhost:8080/api/mentor-mind?question=What%20is%20a%20vector%20store%3F&level=EASY"
```

### Ask a question (structured JSON)

Returns the answer wrapped in a structured payload.

```http
GET /api/mentor-mind/json?question={question}&level={level}
```

**Response — `AnswerDTO`**

```json
{
  "date": "2026-06-02T10:30:00",
  "resume": "A vector store is a database optimized for similarity search over embeddings...",
  "answerLevel": "EASY"
}
```

### Answer levels

The `level` parameter controls how the answer is framed:

- **`EASY`** — beginner-friendly, simplified explanations
- **`HARD`** — deeper, more technical answers
- **`DEFAULT`** — balanced default behavior

---

## Document Validation

Uploads are validated by the custom `@ValidDocumento` constraint backed by `DocumentoValidator`. Validation is **content-aware**, not extension-only:

- The file extension must be one of `md`, `txt`, `pdf`.
- The **real MIME type** is detected with Apache Tika and cross-checked against the extension:
  - `pdf` → must resolve to `application/pdf`
  - `md` / `txt` → must resolve to a `text/*` type

This prevents spoofed or mislabeled files from being ingested.

---

## Error Handling

A global `@RestControllerAdvice` (`UploadExceptionHandler`) maps failures to clean HTTP responses:

| Exception | HTTP Status | Meaning |
|-----------|-------------|---------|
| `ConstraintViolationException` | `400 Bad Request` | Invalid file type or invalid request params |
| `MaxUploadSizeExceededException` | `413 Payload Too Large` | File exceeds the configured size limit |
| `EmptyFileNameException` | `400 Bad Request` | Uploaded file has no name |
| `IngestionException` | `500 Internal Server Error` | Failure while reading/processing the file |

---

## Roadmap

Potential next steps for the project:

- Conversational memory across multiple turns
- Document metadata and per-source filtering
- Authentication and per-user knowledge bases
- Observability (structured logging, Micrometer metrics, distributed tracing)
- Support for additional LLM and embedding providers via the existing generic interface

---

## License

This project is currently unlicensed. Add a license file before distribution.
