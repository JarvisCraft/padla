package ru.progrm_jarvis.reflector.wrapper;

import lombok.experimental.UtilityClass;

/**
 * Helper utilities related to reflector wrappers.
 */
@UtilityClass
public class ReflectorWrappers {

    /**
     * Validates that actual count of the parameters is equal to the expected count.
     *
     * @param expectedCount expected count of the parameters
     * @param actualCount actual count of the parameters
     *
     * @throws IllegalArgumentException if actual count is not equal to expected count
     */
    public void validateParameterCount(final int expectedCount, final int actualCount) {
        if (expectedCount != actualCount) throw new IllegalArgumentException(
                "Expected " + expectedCount + " parameters but got " + actualCount
        );
    }


    /**
     * Validates that actual count of the parameters is equal to the expected count.
     *
     * @param expectedCount expected count of the parameters
     * @param actualParameters parameters whose count should be checked
     *
     * @throws IllegalArgumentException if count of actual parameters is not equal to expected count
     */
    public void validateParameterCount(final int expectedCount, final Object[] actualParameters) {
        validateParameterCount(expectedCount, actualParameters.length);
    }
}
