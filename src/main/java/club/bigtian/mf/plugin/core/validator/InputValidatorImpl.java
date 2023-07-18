package club.bigtian.mf.plugin.core.validator;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.ui.InputValidator;

public class InputValidatorImpl implements InputValidator {
    @Override
    public boolean checkInput(String inputString) {
        return StrUtil.isNotEmpty(inputString);
    }

    @Override
    public boolean canClose(String inputString) {
        return StrUtil.isNotEmpty(inputString);
    }
}
