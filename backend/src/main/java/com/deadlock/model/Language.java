package com.deadlock.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    JAVA("java", "deadlock-sandbox-java"),
    PYTHON("py", "deadlock-sandbox-python"),
    CPP("cpp", "deadlock-sandbox-cpp");

    private final String fileExtension;
    private final String dockerImage;
}
