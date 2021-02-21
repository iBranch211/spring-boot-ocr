package com.spring.ocr.bootstrap;


import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class OpenCvInitializerComponent {

    static {
        try {
            OpenCV.loadShared();
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError ignore) {
            // After using spring-dev-tools, the context will be loaded multiple times, so here will throw the exception that the link library has been loaded.
            // If there is this exception, the link library has been loaded, you can directly swallow the exception.
        }
    }

    @PostConstruct
    private void init() {
        log.info("OpenCV Version: " + Core.VERSION);
    }
}
