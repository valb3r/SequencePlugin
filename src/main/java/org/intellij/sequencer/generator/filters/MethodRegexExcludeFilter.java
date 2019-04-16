package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;

import java.util.regex.Pattern;

public class MethodRegexExcludeFilter implements MethodFilter {

    private final Pattern matcher;

    public MethodRegexExcludeFilter(String nameRegex) {
        this.matcher = Pattern.compile(nameRegex);
    }

    public boolean allow(PsiMethod psiMethod) {
        return !matcher.matcher(psiMethod.getName()).matches();
    }
}