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

import java.awt.Color;
import com.sun.jna.*;

class MacosScreenAccessor implements ScreenAccessor {
    MacosScreenAccessor() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Color getPixelColor(int x, int y) {
        CG.CGRect.ByValue bounds = new CG.CGRect.ByValue();
        bounds.origin.x = x;
        bounds.origin.y = y;
        bounds.size.width = 1;
        bounds.size.height = 1;

        CG.CGImage img = CG.CGWindowListCreateImage(bounds, CG.kCGWindowListOptionAll, 0, CG.kCGWindowImageBoundsIgnoreFraming | CG.kCGWindowImageDefault);

        CF.CFData rawData = CG.CGDataProviderCopyData(CG.CGImageGetDataProvider(img));
        int[] intData = CF.CFDataGetBytePtr(rawData).getIntArray(0, 1);
        int value = intData[0];
        CF.CFRelease(rawData);
        CG.CGImageRelease(img);

        return new Color(value >> 16 & 0xFF, value >> 8 & 0xFF, value & 0xFF);
    }

    static class CF {
        static {
            Native.register("CoreFoundation");
        }

        public static native Pointer CFDataGetBytePtr(CFData rawData);
        public static native void CFRelease(CFData data);

        public static class CFData extends PointerType { }
    }

    static class CG {
        static {
            Native.register("CoreGraphics");
        }

        public static native CGDataProvider CGImageGetDataProvider(CGImage img);
        public static native CF.CFData CGDataProviderCopyData(CGDataProvider img);
        public static native CGImage CGWindowListCreateImage(CGRect.ByValue screenBounds, int windowOption, int windowId, int imageOption);
        public static native void CGImageRelease(CGImage img);

        public static final int kCGWindowListOptionAll = 0;
        public static final int kCGWindowListOptionOnScreenOnly = (1 << 0);
        public static final int kCGWindowListOptionOnScreenAboveWindow = (1 << 1);
        public static final int kCGWindowListOptionOnScreenBelowWindow = (1 << 2);
        public static final int kCGWindowListOptionIncludingWindow = (1 << 3);
        public static final int kCGWindowListExcludeDesktopElements = (1 << 4);

        public static final int kCGWindowImageDefault = 0;
        public static final int kCGWindowImageBoundsIgnoreFraming = (1 << 0);
        public static final int kCGWindowImageShouldBeOpaque = (1 << 1);
        public static final int kCGWindowImageOnlyShadows = (1 << 2);
        public static final int kCGWindowImageBestResolution = (1 << 3);
        public static final int kCGWindowImageNominalResolution = (1 << 4);

        @Structure.FieldOrder({ "x", "y" })
        public static class CGPoint extends Structure {
            public static class ByValue extends CGPoint implements Structure.ByValue { }
            public double x;
            public double y;
        }

        @Structure.FieldOrder({ "width", "height" })
        public static class CGSize extends Structure {
            public static class ByValue extends CGSize implements Structure.ByValue { }

            public double width;
            public double height;
        }

        @Structure.FieldOrder({ "origin", "size" })
        public static class CGRect extends Structure {
            public static class ByValue extends CGRect implements Structure.ByValue { }

            public CGPoint origin;
            public CGSize size;
        }

        public static class CGImage extends PointerType { }
        public static class CGDataProvider extends PointerType { }
    }
}

