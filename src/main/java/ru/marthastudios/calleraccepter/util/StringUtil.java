package ru.marthastudios.calleraccepter.util;

import org.springframework.stereotype.Component;

@Component
public class StringUtil {
    public String extractDigits(String input) {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0; i < input.length() && count < 5; i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
                count++;
            }
        }

        return sb.toString();
    }
}
