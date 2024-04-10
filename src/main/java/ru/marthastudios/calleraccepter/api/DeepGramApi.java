package ru.marthastudios.calleraccepter.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.marthastudios.calleraccepter.api.payload.DeepGramTranscribeResponse;
import ru.marthastudios.calleraccepter.property.DeepGramProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeepGramApi {
    private final DeepGramProperty deepGramProperty;
    private final RestTemplate restTemplate;

    private final String DEEP_GRAM_API_URL = "https://api.deepgram.com";

    public DeepGramTranscribeResponse transcribe(String model, boolean smartFormat, String language,
                                                 File audioFile) {
        byte[] audioBytes = new byte[0];
        try {
            audioBytes = Files.readAllBytes(audioFile.toPath());
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.set("Authorization", "Token " + deepGramProperty.getApiKey());

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioBytes, headers);

        ResponseEntity<DeepGramTranscribeResponse> deepGramTranscribeResponseResponseEntity =
                restTemplate.exchange(
                        DEEP_GRAM_API_URL + "/v1/listen?model=" + model
                                + "&smart_format=" + smartFormat
                                + "&language=" + language,
                        HttpMethod.POST,
                        requestEntity,
                        DeepGramTranscribeResponse.class);

        return deepGramTranscribeResponseResponseEntity.getBody();
    }
}
