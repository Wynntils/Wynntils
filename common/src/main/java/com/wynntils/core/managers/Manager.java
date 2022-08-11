/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

/**
 * Managers can have two static methods:
 * <p>
 * init: Called when manager is enabled
 * <p>
 * disable: Called when manager is disabled
 * <p>
 * Managers are automatically registered to event bus, use static event methods.
 * */
public abstract class Manager {}
