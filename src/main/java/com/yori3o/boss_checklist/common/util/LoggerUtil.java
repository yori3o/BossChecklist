package com.yori3o.boss_checklist.common.util;


import com.yori3o.boss_checklist.impl.PlatformUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;



public class LoggerUtil {

    public static final Logger LOGGER = LogManager.getLogger("boss_checklist"); // LoggerUtil.LOGGER.info("example");


    /// ==================
    /// These methods are needed to add [boss_checklist] to logs on Fabric, while NeoForge does it itself.
    /// ==================

    public static final void info(String message) {
        if (PlatformUtil.isFabric()) {
            LOGGER.info("[boss_checklist] " + message);
        } else {
            LOGGER.info(message);
        }
    }

    public static final void warn(String message) {
        if (PlatformUtil.isFabric()) {
            LOGGER.warn("[boss_checklist] " + message);
        } else {
            LOGGER.warn(message);
        }
    }

    public static final void error(String message) {
        if (PlatformUtil.isFabric()) {
            LOGGER.error("[boss_checklist] " + message);
        } else {
            LOGGER.error(message);
        }
    }

}