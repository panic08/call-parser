package ru.marthastudios.calleraccepter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.marthastudios.calleraccepter.api.FfmpegApi;
import ru.marthastudios.calleraccepter.api.OpenaiApi;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateChatCompletionRequest;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateTranscriptionRequest;
import ru.marthastudios.calleraccepter.enums.CallType;
import ru.marthastudios.calleraccepter.payload.CreateCallResponse;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallService {
    private final FfmpegApi ffmpegApi;
    private final OpenaiApi openaiApi;
    @Value("${audioBackupCalls.filesPath}")
    private String audioBackupFilesPath;

    public CreateCallResponse create(CallType type, MultipartFile audioFile) {
        switch (type) {
            case TELEGRAM -> {
                File newWavFile = null;
                File newMp3File = null;

                try {
                    newWavFile = File.createTempFile("audioWav", ".wav");

                    audioFile.transferTo(newWavFile);
                } catch (IOException e) {
                    log.warn(e.getMessage());
                }

                String mp3FilePath = newWavFile.getPath().replaceAll(".wav", ".mp3");

                newMp3File = ffmpegApi.fromWavToMp3(newWavFile.getPath(), mp3FilePath);

                newWavFile.delete();

                log.info("Created audioMp3 path: {} on type TELEGRAM", newMp3File.getPath());

                String transcriptionAudioText = openaiApi.createTranscription(
                        OpenaiCreateTranscriptionRequest.builder()
                                .file(newMp3File)
                                .model("whisper-1")
                                .language("ru")
                                .temperature(0.3f)
                                .build()
                ).getText();

                String codeString = openaiApi.createChatCompletion(OpenaiCreateChatCompletionRequest.builder()
                                .model("gpt-4")
                                .temperature(0.3f)
                                .messages(new OpenaiCreateChatCompletionRequest.Message[] {
                                        OpenaiCreateChatCompletionRequest.Message.builder()
                                                .role("system")
                                                .content("Ты - конвертер кодов. Из текста ты должен вернуть только цифровой код, только цифровой, это важно. Нужны только цифры, без текста и спецсимволов")
                                                .build(),
                                        OpenaiCreateChatCompletionRequest.Message.builder()
                                                .role("user")
                                                .content("Из текста ты должен вернуть только цифровой код, только цифровой, это важно. Нужны только цифры, без текста и спецсимволов. Текст с кодом: \"" + transcriptionAudioText + "\"")
                                                .build()
                                })
                        .build()).getChoices()[0].getMessage().getContent();

                File newAudioBackupFile;

                Date currentDate = new Date(System.currentTimeMillis());

                SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss dd.MM.yyyy");

                String newAudioBackupFileName = sdf.format(currentDate) + " {code " + codeString + "}.mp3";

                newAudioBackupFileName = newAudioBackupFileName.replaceAll(":", " ");

                try {
                    newAudioBackupFile = new File(audioBackupFilesPath + newAudioBackupFileName);

                    FileSystemUtils.copyRecursively(newMp3File, newAudioBackupFile);
                } catch (IOException e) {
                    log.warn(e.getMessage());
                }
    
                newMp3File.delete();

                return CreateCallResponse.builder()
                        .code(codeString)
                        .build();
            }
        }

        return null;

    }
}
