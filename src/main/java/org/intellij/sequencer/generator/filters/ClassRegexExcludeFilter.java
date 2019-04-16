package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.regex.Pattern;

public class ClassRegexExcludeFilter implements MethodFilter {

    private final Pattern matcher;

    public ClassRegexExcludeFilter(String nameRegex) {
        this.matcher = Pattern.compile(nameRegex);
    }

    public boolean allow(PsiMethod psiMethod) {
        PsiClass clazz = psiMethod.getContainingClass();
        return null != clazz
                && null != clazz.getQualifiedName()
                && !matcher.matcher(clazz.getQualifiedName()).matches();
    }
}