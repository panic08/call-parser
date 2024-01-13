package ru.marthastudios.calleraccepter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.marthastudios.calleraccepter.api.FfmpegApi;
import ru.marthastudios.calleraccepter.api.OpenaiApi;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateChatCompletionRequest;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateChatCompletionResponse;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateTranscriptionRequest;
import ru.marthastudios.calleraccepter.enums.CallType;
import ru.marthastudios.calleraccepter.payload.CreateCallResponse;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallService {
    private final FfmpegApi ffmpegApi;
    private final OpenaiApi openaiApi;

    public CreateCallResponse create(CallType type, MultipartFile audioFile) {
        switch (type) {
            case TELEGRAM -> {
                File newAacFile = null;
                File newMp3File = null;

                try {
                    newAacFile = File.createTempFile("audioAac", ".aac");

                    audioFile.transferTo(newAacFile);
                } catch (IOException e) {
                    log.warn(e.getMessage());
                }

                String mp3FilePath = newAacFile.getPath().replaceAll(".aac", ".mp3");

                newMp3File = ffmpegApi.fromAacToMp3(newAacFile.getPath(), mp3FilePath);

                newAacFile.delete();

                log.info("Created audioMp3 path: {} on type TELEGRAM", newMp3File.getPath());

                String transcriptionAudioText = openaiApi.createTranscription(
                        OpenaiCreateTranscriptionRequest.builder()
                                .file(newMp3File)
                                .model("whisper-1")
                                .build()
                ).getText();

                newMp3File.delete();

                String codeString = openaiApi.createChatCompletion(OpenaiCreateChatCompletionRequest.builder()
                                .model("gpt-3.5-turbo")
                                .messages(new OpenaiCreateChatCompletionRequest.Message[] {
                                        OpenaiCreateChatCompletionRequest.Message.builder()
                                                .role("system")
                                                .content("Ты - конвертер кодов. Из текста ты должен вернуть только цифровой код, только цифровой, это важно. Нужны только цифры")
                                                .build(),
                                        OpenaiCreateChatCompletionRequest.Message.builder()
                                                .role("user")
                                                .content("Текст с кодом: " + transcriptionAudioText)
                                                .build()
                                })
                        .build()).getChoices()[0].getMessage().getContent();

                return CreateCallResponse.builder()
                        .code(Integer.parseInt(codeString))
                        .build();
            }
        }

        return null;

    }
}
