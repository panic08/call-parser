package ru.marthastudios.calleraccepter.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateCallResponse {
    private String code;
}
