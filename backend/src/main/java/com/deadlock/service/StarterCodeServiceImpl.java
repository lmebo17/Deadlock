package com.deadlock.service;

import com.deadlock.exception.InvalidInputException;
import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import com.deadlock.service.codegen.LanguageWrapperStrategy;
import com.deadlock.service.codegen.ParamInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StarterCodeServiceImpl implements StarterCodeService {

    private final List<LanguageWrapperStrategy> strategies;

    private LanguageWrapperStrategy getStrategy(Language language) {
        return strategies.stream()
                .filter(s -> s.getLanguage() == language)
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("No starter for: " + language));
    }

    @Override
    public String generateStarter(Problem problem, Language language) {
        List<ParamInfo> params = WrapperCodeServiceImpl.parseParams(problem.getParams());
        return getStrategy(language).generateStarter(problem.getFunctionName(), params, problem.getReturnType());
    }
}
