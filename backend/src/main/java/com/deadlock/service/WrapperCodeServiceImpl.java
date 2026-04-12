package com.deadlock.service;

import com.deadlock.exception.InvalidInputException;
import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import com.deadlock.service.codegen.LanguageWrapperStrategy;
import com.deadlock.service.codegen.ParamInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WrapperCodeServiceImpl implements WrapperCodeService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<LanguageWrapperStrategy> strategies;

    private LanguageWrapperStrategy getStrategy(Language language) {
        return strategies.stream()
                .filter(s -> s.getLanguage() == language)
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("No wrapper for: " + language));
    }

    @Override
    public String wrapCode(String userCode, Problem problem, Language language) {
        List<ParamInfo> params = parseParams(problem.getParams());
        return getStrategy(language).wrapCode(userCode, problem.getFunctionName(), params, problem.getReturnType());
    }

    static List<ParamInfo> parseParams(String paramsJson) {
        try {
            List<Map<String, String>> raw = MAPPER.readValue(paramsJson, new TypeReference<>() {});
            return raw.stream()
                    .map(m -> new ParamInfo(m.get("name"), m.get("type")))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid params JSON: " + paramsJson, e);
        }
    }
}
