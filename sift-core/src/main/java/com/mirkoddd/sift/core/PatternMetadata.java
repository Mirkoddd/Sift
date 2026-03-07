package com.mirkoddd.sift.core;

import java.util.Set;

/**
 * Internal interface used to extract metadata (like groups and backreferences)
 * from Sift patterns, bypassing wrappers without polluting the public API.
 */
interface PatternMetadata {
    Set<String> getInternalRegisteredGroups();
    Set<String> getInternalRequiredBackreferences();
}