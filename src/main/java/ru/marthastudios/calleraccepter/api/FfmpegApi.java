package ru.marthastudios.calleraccepter.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class FfmpegApi {
    public File fromAacToMp3(String aacFilePath, String mp3FilePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", aacFilePath,
                    "-c:a", "libmp3lame",
                    "-b:a", "192k",
                    mp3FilePath
            );

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.warn(e.getMessage());
        }

        return new File(mp3FilePath);
    }

    public File fromWavToMp3(String wavFilePath, String mp3FilePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", wavFilePath,
                    "-c:a", "libmp3lame",
                    "-b:a", "192k",
                    mp3FilePath
            );

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.warn(e.getMessage());
        }

        return new File(mp3FilePath);
    }
}
