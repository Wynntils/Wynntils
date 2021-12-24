package com.wynntils.framework.feature;

/**
 * How much performance strain the feature causes. For example, cached chunks would have a higher performance impact
 * than a Wynncraft Button.
 */
public enum PerformanceImpact {
    Small, Medium, Large, Extreme
}
