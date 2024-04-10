package ru.marthastudios.calleraccepter.api.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeepGramTranscribeResponse {
    @JsonProperty("results")
    private Result result;
    @Getter
    @Setter
    public static class Result {
        private Channel[] channels;
        @Getter
        @Setter
        public static class Channel {
            private Alternative[] alternatives;
            @Getter
            @Setter
            public static class Alternative {
                private String transcript;
                private double confidence;
            }
        }
    }
}
