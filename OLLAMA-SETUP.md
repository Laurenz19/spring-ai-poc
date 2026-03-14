# Ollama Integration Guide — backend

This project supports two AI providers that can be switched via Spring profiles:
- **`ollama`** — Free, runs locally via Docker (default)
- **`anthropic`** — Claude API (paid, requires API key)

---

## Prerequisites

- Docker Desktop installed and running
- Java 21 + Maven
- PostgreSQL running on port 5432

---

## Step 1 — Start Ollama with Docker

Run the following command to start the Ollama container:

```bash
docker run -d \
  --name ollama \
  -p 11434:11434 \
  -v ollama:/root/.ollama \
  ollama/ollama
```

> **Have an NVIDIA GPU?** Add `--gpus all` for much faster responses:
> ```bash
> docker run -d --gpus all --name ollama -p 11434:11434 -v ollama:/root/.ollama ollama/ollama
> ```

Verify the container is running:

```bash
docker ps
```

---

## Step 2 — Pull the AI Model

Pull `llama3.2` inside the container (supports tool calling):

```bash
docker exec -it ollama ollama pull llama3.2
```

This downloads ~2GB. Wait for it to complete.

Verify the model is available:

```bash
docker exec -it ollama ollama list
```

---

## Step 3 — Start the Spring Boot Backend

The default profile is already set to `ollama`. Simply run:

```bash
cd backend
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`.

---

## Step 4 — Start the Angular Frontend

```bash
cd frontend
npm install
npm start
```

The frontend will be available at `http://localhost:4200`.

---

## Switching Between Providers

### Use Ollama (free, local) — default
In [application.yaml](backend/src/main/resources/application.yaml), set:
```yaml
spring:
  profiles:
    active: ollama
```

### Use Claude API (paid)
1. Set your API key as an environment variable:
   ```bash
   # Windows (PowerShell)
   $env:ANTHROPIC_API_KEY="sk-ant-..."

   # Linux / macOS
   export ANTHROPIC_API_KEY="sk-ant-..."
   ```
2. In [application.yaml](backend/src/main/resources/application.yaml), change:
   ```yaml
   spring:
     profiles:
       active: anthropic
   ```
3. Restart the backend.

### Or activate profile at startup (without editing yaml):
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=anthropic
./mvnw spring-boot:run -Dspring-boot.run.profiles=ollama
```

---

## Stopping Ollama

```bash
docker stop ollama
```

To start it again later:
```bash
docker start ollama
```

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `Connection refused` on port 11434 | Run `docker start ollama` |
| Model not found error | Run `docker exec -it ollama ollama pull llama3.2` |
| Slow responses | Normal for CPU-only — use GPU flag or switch to `anthropic` profile |
| App fails to start with both profiles active | Only one profile must be active at a time |
| Anthropic API key error | Set `ANTHROPIC_API_KEY` environment variable |

---

## Project Structure Reference

```
backend/
├── backend/                        # Spring Boot backend
│   └── src/main/resources/
│       ├── application.yaml           # Main config — set active profile here
│       ├── application-ollama.yaml    # Ollama profile config
│       └── application-anthropic.yaml # Anthropic/Claude profile config
└── frontend/                          # Angular frontend
```
