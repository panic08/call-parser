package ru.marthastudios.calleraccepter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class OpenaiProperty {
    @Value("${openai.apiKey}")
    private String apiKey;
}