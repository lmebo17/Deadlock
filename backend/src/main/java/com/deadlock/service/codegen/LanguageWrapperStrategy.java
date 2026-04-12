package com.deadlock.service.codegen;

import com.deadlock.model.Language;

import java.util.List;

public interface LanguageWrapperStrategy {
    Language getLanguage();
    String wrapCode(String userCode, String functionName, List<ParamInfo> params, String returnType);
    String generateStarter(String functionName, List<ParamInfo> params, String returnType);
}
