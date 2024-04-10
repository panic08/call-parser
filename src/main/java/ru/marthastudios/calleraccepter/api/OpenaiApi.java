package ru.marthastudios.calleraccepter.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateChatCompletionRequest;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateChatCompletionResponse;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateTranscriptionRequest;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateTranscriptionResponse;
import ru.marthastudios.calleraccepter.property.OpenaiProperty;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenaiApi {
    private final OpenaiProperty openaiProperty;
    private final RestTemplate restTemplate;
    private final String OPENAI_API_URL = "https://api.openai.com/v1";

    public OpenaiCreateChatCompletionResponse createChatCompletion(OpenaiCreateChatCompletionRequest openaiCreateChatCompletionRequest) {
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + openaiProperty.getApiKey());

        HttpEntity<OpenaiCreateChatCompletionRequest> httpEntity = new HttpEntity<>(openaiCreateChatCompletionRequest, headers);

        ResponseEntity<OpenaiCreateChatCompletionResponse> openaiCreateChatCompletionResponseResponseEntity =
                restTemplate.postForEntity(OPENAI_API_URL + "/chat/completions", httpEntity, OpenaiCreateChatCompletionResponse.class);

        return openaiCreateChatCompletionResponseResponseEntity.getBody();
    }

    public OpenaiCreateTranscriptionResponse createTranscription(OpenaiCreateTranscriptionRequest openaiCreateTranscriptionRequest) {
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + openaiProperty.getApiKey());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("model", openaiCreateTranscriptionRequest.getModel());
        body.add("language", openaiCreateTranscriptionRequest.getLanguage());
        body.add("temperature", openaiCreateTranscriptionRequest.getTemperature());

        try {
            body.add("file", new FileSystemResource(openaiCreateTranscriptionRequest.getFile().getPath()));
        } catch (Exception e){
            log.warn(e.getMessage());
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        ResponseEntity<OpenaiCreateTranscriptionResponse> transcriptionResponseEntity =
                restTemplate.postForEntity(OPENAI_API_URL + "/audio/transcriptions", requestEntity, OpenaiCreateTranscriptionResponse.class);

        return transcriptionResponseEntity.getBody();
    }
}
