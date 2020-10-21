package fr.uem.efluid.generation;

import fr.uem.efluid.ParameterInheritance;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

abstract class PossibleItem {

    private final Class<?> sourceClazz;

    // Detected on method
    protected PossibleItem(Class<?> sourceClazz) {

        this.sourceClazz = sourceClazz;
    }

    Class<?> getSourceClazz() {
        return this.sourceClazz;
    }

    abstract String getValidName();


    boolean isExcluded(Class<?> currentType, Map<ParameterInheritance, Class<?>> inheritances) {

        if (inheritances.size() > 0) {
            return inheritances.entrySet().stream().anyMatch(t -> {
                        boolean valueIsFromAParentOfWhereExcludeIsSpecified = isAParentOfB(getSourceClazz(), t.getValue());
                        return (t.getKey().of() == ParameterInheritance.ALL && valueIsFromAParentOfWhereExcludeIsSpecified)
                                || (t.getKey().of() == getSourceClazz() && (t.getKey().fields().length == 0 || Arrays.asList(t.getKey().fields()).contains(getValidName())));
                    }
            );
        }

        return false;
    }

    private static boolean isAParentOfB(Class<?> A, Class<?> B) {
        return A != B && A.isAssignableFrom(B);
    }
}
