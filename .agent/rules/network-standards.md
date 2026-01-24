---
trigger: model_decision
description: Network Standards (Ktor 3.0)
---

# Network Standards (Ktor 3.0)
- **Engine Configuration:** Always use the `CIO` or `OkHttp` engine for Android and `Darwin` for iOS, but define the `HttpClient` in `commonMain` using the multiplatform `HttpClient` factory.
- **Serialization:** Use `kotlinx.serialization` with `ContentNegotiation`.
- **Error Handling:** - Every network call must be wrapped in a `Result` or a custom `NetworkResponse` wrapper.
  - Implement a global `HttpRequestRetry` configuration with exponential backoff.
- **Agent Instruction:** "When generating API services, always create a `BaseUrl` constant in a `NetworkConstants.kt` file. Ensure all JSON models are annotated with `@Serializable`."