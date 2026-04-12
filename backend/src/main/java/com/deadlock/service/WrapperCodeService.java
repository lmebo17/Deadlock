package com.deadlock.service;

import com.deadlock.model.Language;
import com.deadlock.model.Problem;

public interface WrapperCodeService {
    String wrapCode(String userCode, Problem problem, Language language);
}
