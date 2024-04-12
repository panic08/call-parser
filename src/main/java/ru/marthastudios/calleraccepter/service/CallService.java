package ru.marthastudios.calleraccepter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.marthastudios.calleraccepter.api.DeepGramApi;
import ru.marthastudios.calleraccepter.api.FfmpegApi;
import ru.marthastudios.calleraccepter.api.OpenaiApi;
import ru.marthastudios.calleraccepter.api.payload.DeepGramTranscribeResponse;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateChatCompletionRequest;
import ru.marthastudios.calleraccepter.api.payload.OpenaiCreateTranscriptionRequest;
import ru.marthastudios.calleraccepter.enums.CallType;
import ru.marthastudios.calleraccepter.payload.CreateCallResponse;
import ru.marthastudios.calleraccepter.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallService {
    private final DeepGramApi deepGramApi;
    private final StringUtil stringUtil;
    private final FfmpegApi ffmpegApi;
    private final OpenaiApi openaiApi;

    @Value("${audioBackupCalls.filesPath}")
    private String audioBackupFilesPath;

    public CreateCallResponse create(String model, CallType type, MultipartFile audioFile) {
        switch (model) {
            case "nova-2" -> {
                switch (type) {
                    case TELEGRAM -> {
                        File principalAudioFile = null;
                        String principalAudioFileExtension = audioFile.getResource().getFilename()
                                .substring(audioFile.getResource().getFilename().length() - 4);

                        try {
                            principalAudioFile = File.createTempFile("newPrincipalAudioFile", principalAudioFileExtension);

                            byte[] principalAudioFileByteContent = audioFile.getBytes();

                            FileOutputStream fos = new FileOutputStream(principalAudioFile);

                            fos.write(principalAudioFileByteContent);

                            fos.close();
                        } catch (IOException e) {
                            log.warn(e.getMessage());
                        }

                        DeepGramTranscribeResponse deepGramTranscribeResponse =
                                deepGramApi.transcribe("nova-2", true, "ru", principalAudioFile);

                        String transcriptStringWithCode = deepGramTranscribeResponse.getResult().getChannels()[0]
                                .getAlternatives()[0].getTranscript();

                        String code = stringUtil.extractDigits(transcriptStringWithCode);

                        File newAudioBackupFile;

                        Date currentDate = new Date(System.currentTimeMillis());

                        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss dd.MM.yyyy");

                        String newAudioBackupFileName = sdf.format(currentDate) + " {code " + code + "}" + principalAudioFileExtension;
                        newAudioBackupFileName = newAudioBackupFileName.replaceAll(":", " ");

                        try {
                            newAudioBackupFile = new File(audioBackupFilesPath + newAudioBackupFileName);

                            FileSystemUtils.copyRecursively(principalAudioFile, newAudioBackupFile);
                        } catch (IOException e) {
                            log.warn(e.getMessage());
                        }

                        principalAudioFile.delete();

                        return CreateCallResponse.builder()
                                .code(code)
                                .build();
                    }
                }
            }
            case "whisper-1" -> {
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

                        String transcriptionAudioText = openaiApi.createTranscription(
                                OpenaiCreateTranscriptionRequest.builder()
                                        .file(newMp3File)
                                        .model("whisper-1")
                                        .language("ru")
                                        .temperature(0.5f)
                                        .build()
                        ).getText();

                        String codeString = stringUtil.extractDigits(transcriptionAudioText);

                        log.info("Created transcription with {} model, transcriptionAudioText: {} codeString: {}",
                                model, transcriptionAudioText, codeString);

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
            }
        }
        return null;
    }

//    public CreateCallResponse create(CallType type, MultipartFile audioFile) {
//        switch (type) {
//            case TELEGRAM -> {
//                File newWavFile = null;
//                File newMp3File = null;
//
//                try {
//                    newWavFile = File.createTempFile("audioWav", ".wav");
//
//                    audioFile.transferTo(newWavFile);
//                } catch (IOException e) {
//                    log.warn(e.getMessage());
//                }
//
//                String mp3FilePath = newWavFile.getPath().replaceAll(".wav", ".mp3");
//
//                newMp3File = ffmpegApi.fromWavToMp3(newWavFile.getPath(), mp3FilePath);
//
//                newWavFile.delete();
//
//                log.info("Created audioMp3 path: {} on type TELEGRAM", newMp3File.getPath());
//
//                String transcriptionAudioText = openaiApi.createTranscription(
//                        OpenaiCreateTranscriptionRequest.builder()
//                                .file(newMp3File)
//                                .model("whisper-1")
//                                .language("ru")
//                                .temperature(0.3f)
//                                .build()
//                ).getText();
//
//                String codeString = openaiApi.createChatCompletion(OpenaiCreateChatCompletionRequest.builder()
//                                .model("gpt-4")
//                                .temperature(0.3f)
//                                .messages(new OpenaiCreateChatCompletionRequest.Message[] {
//                                        OpenaiCreateChatCompletionRequest.Message.builder()
//                                                .role("system")
//                                                .content("Ты - конвертер кодов. Из текста ты должен вернуть только цифровой код, только цифровой, это важно. Нужны только цифры, без текста и спецсимволов")
//                                                .build(),
//                                        OpenaiCreateChatCompletionRequest.Message.builder()
//                                                .role("user")
//                                                .content("Из текста ты должен вернуть только цифровой код, только цифровой, это важно. Нужны только цифры, без текста и спецсимволов. Текст с кодом: \"" + transcriptionAudioText + "\"")
//                                                .build()
//                                })
//                        .build()).getChoices()[0].getMessage().getContent();
//
//                File newAudioBackupFile;
//
//                Date currentDate = new Date(System.currentTimeMillis());
//
//                SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss dd.MM.yyyy");
//
//                String newAudioBackupFileName = sdf.format(currentDate) + " {code " + codeString + "}.mp3";
//
//                newAudioBackupFileName = newAudioBackupFileName.replaceAll(":", " ");
//
//                try {
//                    newAudioBackupFile = new File(audioBackupFilesPath + newAudioBackupFileName);
//
//                    FileSystemUtils.copyRecursively(newMp3File, newAudioBackupFile);
//                } catch (IOException e) {
//                    log.warn(e.getMessage());
//                }
//
//                newMp3File.delete();
//
//                return CreateCallResponse.builder()
//                        .code(codeString)
//                        .build();
//            }
//        }
//
//        return null;
//
//    }
}
