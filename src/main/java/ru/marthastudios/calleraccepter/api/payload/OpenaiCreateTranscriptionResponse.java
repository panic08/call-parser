package ru.marthastudios.calleraccepter.api.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenaiCreateTranscriptionResponse {
    private String text;
}
