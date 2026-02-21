package org.example.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @file openrouterclient.java
 * @brief client http pentru apelarea openrouter chat completions
 *
 * construieste un request json cu mesaje (system + user) trimite cererea cu api key din env
 * si returneaza textul raspunsului
 */
public final class OpenRouterClient {

    /** endpoint-ul openrouter pentru chat completions. */
    private static final String URL = "https://openrouter.ai/api/v1/chat/completions";

    /** client http  folosit pentru trimiterea cererilor. */
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    /** mapper json (jackson) pentru construire json. */
    private final ObjectMapper om = new ObjectMapper();

    /** cheia api citita din variabila de mediu openrouter_api_key. */
    private final String key = System.getenv("OPENROUTER_API_KEY");

    /**
     * constructor: verifica existenta cheii api in variabilele de mediu.
     *
     * @throws illegalstateexception daca openrouter_api_key lipseste sau este goala.
     */
    public OpenRouterClient() {
        if (key == null || key.isBlank()) throw new IllegalStateException("OPENROUTER_API_KEY lipsa");
    }

    /**
     * trimite un mesaj catre model si intoarce raspunsul generat.
     *
     * @param model numele modelului folosit
     * @param system mesajul system instructiuni pentru model
     * @param user mesajul userullui
     * @param temperature temperatura pentru variatie (0 = determinist mai mare = mai creativ).
     * @param maxtokens numarul maxim de tokeni generati in raspuns.
     * @return continutul raspunsului
     * @throws runtimeexception daca request-ul esueaza
     */
    public String chat(String model, String system, String user, double temperature, int maxTokens) {
        try {
            var reqJson = om.createObjectNode();
            reqJson.put("model", model);

            var messages = om.createArrayNode();
            messages.add(om.createObjectNode().put("role", "system").put("content", system));
            messages.add(om.createObjectNode().put("role", "user").put("content", user));

            reqJson.set("messages", messages);
            reqJson.put("temperature", temperature);
            reqJson.put("max_tokens", maxTokens);

            String body = reqJson.toString();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("Eroare OpenRouter " + resp.statusCode() + ": " + resp.body());
            }

            JsonNode json = om.readTree(resp.body());
            return json.at("/choices/0/message/content").asText("");
        } catch (Exception e) {
            throw new RuntimeException("Apel OpenRouter esuat: " + e.getMessage(), e);
        }
    }
}
