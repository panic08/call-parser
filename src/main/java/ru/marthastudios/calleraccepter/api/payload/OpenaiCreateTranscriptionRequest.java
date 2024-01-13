package ru.marthastudios.calleraccepter.api.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
@Builder
public class OpenaiCreateTranscriptionRequest {
    private File file;
    private String model;
}
