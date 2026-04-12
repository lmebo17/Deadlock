package com.deadlock.service;

import com.deadlock.model.Language;
import com.deadlock.model.Problem;

public interface StarterCodeService {
    String generateStarter(Problem problem, Language language);
}
