/*
 * Copyright 2026 Francisco Requena, https://frarees.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pavelfatin.typometer.screen;

import com.sun.jna.*;

class MacosAccessibility {
    private MacosAccessibility() {
    }

    static boolean hasAccessibilityPermission() {
        try {
            return AX.AXIsProcessTrusted();
        } catch (UnsatisfiedLinkError e) {
            return true;
        }
    }

    static boolean requestAccessibilityPermission() {
        try {
            // AXIsProcessTrustedWithOptions needs a { kAXTrustedCheckOptionPrompt: true }
            // CFDictionary to show the prompt, built here from framework globals.
            Pointer key = global("ApplicationServices", "kAXTrustedCheckOptionPrompt");
            Pointer value = global("CoreFoundation", "kCFBooleanTrue");
            Pointer keyCallBacks = address("CoreFoundation", "kCFTypeDictionaryKeyCallBacks");
            Pointer valueCallBacks = address("CoreFoundation", "kCFTypeDictionaryValueCallBacks");

            Memory keys = new Memory(Native.POINTER_SIZE);
            keys.setPointer(0, key);
            Memory values = new Memory(Native.POINTER_SIZE);
            values.setPointer(0, value);

            Pointer options = CF.CFDictionaryCreate(null, keys, values, 1, keyCallBacks, valueCallBacks);
            try {
                return AX.AXIsProcessTrustedWithOptions(options);
            } finally {
                if (options != null) {
                    CF.CFRelease(options);
                }
            }
        } catch (UnsatisfiedLinkError e) {
            return true;
        }
    }

    // Dereferences a framework global that holds a pointer (e.g. a CFStringRef).
    private static Pointer global(String library, String name) {
        return NativeLibrary.getInstance(library).getGlobalVariableAddress(name).getPointer(0);
    }

    private static Pointer address(String library, String name) {
        return NativeLibrary.getInstance(library).getGlobalVariableAddress(name);
    }

    private static class CF {
        static {
            Native.register("CoreFoundation");
        }

        public static native Pointer CFDictionaryCreate(Pointer allocator, Pointer keys, Pointer values,
                long numValues, Pointer keyCallBacks, Pointer valueCallBacks);
        public static native void CFRelease(Pointer ref);
    }

    private static class AX {
        static {
            Native.register("ApplicationServices");
        }

        public static native boolean AXIsProcessTrusted();
        public static native boolean AXIsProcessTrustedWithOptions(Pointer options);
    }
}
