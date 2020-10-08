/*
 * Copyright 2017 Francisco Requena, https://frarees.github.io
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

package com.pavelfatin.typometer.metrics;

import java.awt.Color;

public class ColorUtil {
    private ColorUtil() {
    }

    public static boolean equals(Color c1, Color c2, int tolerance) {
        return (Math.abs(c1.getRed() - c2.getRed()) <= tolerance) &&
            (Math.abs(c1.getGreen() - c2.getGreen()) <= tolerance) &&
            (Math.abs(c1.getBlue() - c2.getBlue()) <= tolerance);
    }

    public static boolean equals(Color c1, Color c2) {
        return equals(c1, c2, 3);
    }
}
