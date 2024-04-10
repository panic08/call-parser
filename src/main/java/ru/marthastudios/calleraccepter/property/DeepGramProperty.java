package ru.marthastudios.calleraccepter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deepgram")
@Getter
@Setter
public class DeepGramProperty {
    private String apiKey;
}
