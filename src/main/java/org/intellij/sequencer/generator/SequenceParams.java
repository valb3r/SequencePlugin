package org.intellij.sequencer.generator;

import com.google.common.collect.ImmutableMap;
import org.intellij.sequencer.config.Configuration;
import org.intellij.sequencer.config.ExcludeEntry;
import org.intellij.sequencer.generator.filters.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SequenceParams {
    private static final String PACKAGE_INDICATOR = ".*";
    private static final String RECURSIVE_PACKAGE_INDICATOR = ".**";

    private static final Map<String, BiConsumer<String, CompositeMethodFilter>> EXTRAS = ImmutableMap.of(
            "-c:", (regex, filters) -> filters.addFilter(new ClassRegexExcludeFilter(regex)),
            "-m:", (regex, filters) -> filters.addFilter(new MethodRegexExcludeFilter(regex)),
            "+c:", (regex, filters) -> filters.addFilter(new ClassRegexIncludeFilter(regex)),
            "+m:", (regex, filters) -> filters.addFilter(new MethodRegexIncludeFilter(regex))
    );

    private int _maxDepth = 3;
    private boolean _allowRecursion = true;
    private boolean smartInterface = true;
    private CompositeMethodFilter _methodFilter = new CompositeMethodFilter();
    private InterfaceImplFilter _implFilter = new InterfaceImplFilter();

    public SequenceParams() {
        List excludeList = Configuration.getInstance().getExcludeList();
        for(Iterator iterator = excludeList.iterator(); iterator.hasNext();) {
            ExcludeEntry excludeEntry = (ExcludeEntry)iterator.next();
            if(!excludeEntry.isEnabled())
                continue;
            String excludeName = excludeEntry.getExcludeName();

            if (tryToAddAsExtras(excludeName, _methodFilter)) {
                continue;
            }

            if(excludeName.endsWith(PACKAGE_INDICATOR)) {
                int index = excludeName.lastIndexOf(PACKAGE_INDICATOR);
                _methodFilter.addFilter(new PackageFilter(excludeName.substring(0, index)));
            }
            else if(excludeName.endsWith(RECURSIVE_PACKAGE_INDICATOR)) {
                int index = excludeName.lastIndexOf(RECURSIVE_PACKAGE_INDICATOR);
                _methodFilter.addFilter(new PackageFilter(excludeName.substring(0, index), true));
            }
            else {
                _methodFilter.addFilter(new SingleClassFilter(excludeName));
            }
        }
    }

    public int getMaxDepth() {
        return _maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this._maxDepth = maxDepth;
    }

    public boolean isAllowRecursion() {
        return _allowRecursion;
    }

    public void setAllowRecursion(boolean allowRecursion) {
        this._allowRecursion = allowRecursion;
    }

    public boolean isSmartInterface() {
        return smartInterface;
    }

    public void setSmartInterface(boolean smartInterface) {
        this.smartInterface = smartInterface;
    }

    public CompositeMethodFilter getMethodFilter() {
        return _methodFilter;
    }

    public InterfaceImplFilter getInterfaceImplFilter() {
        return _implFilter;
    }

    private boolean tryToAddAsExtras(String excludeName, CompositeMethodFilter filters) {

        Optional<String> prefix = EXTRAS.keySet().stream()
                .filter(excludeName::startsWith)
                .findFirst();

        if (!prefix.isPresent()) {
            return false;
        }

        doHandle(excludeName, prefix.get(), filters);
        return true;
    }

    private void doHandle(String excludeName, String prefix, CompositeMethodFilter filters) {
        int index = excludeName.lastIndexOf(prefix);
        String regex = excludeName.substring(index + prefix.length());
        EXTRAS.get(prefix).accept(regex, filters);
    }
}

