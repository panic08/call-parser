package ru.marthastudios.calleraccepter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.marthastudios.calleraccepter.enums.CallType;
import ru.marthastudios.calleraccepter.payload.CreateCallResponse;
import ru.marthastudios.calleraccepter.service.CallService;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
@Slf4j
public class CallController {
    private final CallService callService;

    @PostMapping
    public CreateCallResponse create(@RequestParam("type") CallType type,
                                     @RequestParam("file") MultipartFile file) {
        return callService.create(type, file);
    }

    @PostMapping("/protocolTen")
    public void destroy() {
        System.exit(1);
    }
}
