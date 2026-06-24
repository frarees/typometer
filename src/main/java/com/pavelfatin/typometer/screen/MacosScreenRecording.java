/*
 * Copyright 2020 Francisco Requena, https://frarees.github.io
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

import java.io.IOException;
import com.sun.jna.*;

class MacosScreenRecording {
    // Deep link to System Settings ▸ Privacy & Security ▸ Screen Recording.
    private static final String SCREEN_RECORDING_SETTINGS_URL =
            "x-apple.systempreferences:com.apple.preference.security?Privacy_ScreenCapture";

    private MacosScreenRecording() {
    }

    // Without Screen Recording permission screen capture returns wallpaper/own-window
    // pixels instead of failing, so it must be checked up front. The symbol is absent
    // before macOS 10.15, where the permission does not exist.
    static boolean hasScreenRecordingPermission() {
        try {
            return CG.CGPreflightScreenCaptureAccess();
        } catch (UnsatisfiedLinkError e) {
            return true;
        }
    }

    static boolean requestScreenRecordingPermission() {
        try {
            return CG.CGRequestScreenCaptureAccess();
        } catch (UnsatisfiedLinkError e) {
            return true;
        }
    }

    static void openScreenRecordingSettings() throws IOException {
        Runtime.getRuntime().exec(new String[]{"open", SCREEN_RECORDING_SETTINGS_URL});
    }

    private static class CG {
        static {
            Native.register("CoreGraphics");
        }

        // Screen Recording permission (TCC), available since macOS 10.15.
        public static native boolean CGPreflightScreenCaptureAccess();
        public static native boolean CGRequestScreenCaptureAccess();
    }
}
