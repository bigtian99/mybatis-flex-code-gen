package com.mybatisflex.plugin.core.validator;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.util.NlsSafe;

public class InputValidatorImpl implements InputValidator {
    @Override
    public boolean checkInput(@NlsSafe String inputString) {
        return StrUtil.isNotEmpty(inputString);
    }

    @Override
    public boolean canClose(@NlsSafe String inputString) {
        return StrUtil.isNotEmpty(inputString);
    }
}
