/*
 * Copyright 2017 Pavel Fatin, https://pavelfatin.com
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


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static java.lang.Math.floor;
import static java.lang.Math.round;

class MetricsDetector {
    // Image recognition
    private static final int SMOOTHING_RADIUS = 2;
    private static final double LUMINESCENCE_THRESHOLD = 0.157D;
    private static final double FILL_THRESHOLD = 0.7D;

    // Area filtering
    private static final int MIN_AREA = 1;
    private static final int MAX_AREA = 100;
    private static final double MAX_ASPECT_RATIO = 2.0D;

    // Sequence detection
    private static final int MIN_STEP = 4;
    private static final double MAX_SIZE_DEVIATION = 2.0D;
    private static final double MAX_DISTANCE_DEVIATION = 2.0D;


    private MetricsDetector() {
    }

    static Optional<Metrics> detect(BufferedImage image1, BufferedImage image2, int count) {
        Queue queue = new Queue(256);

        boolean inverted = GreyscaleImage.isInverted(image1);

        GreyscaleImage greyscaleImage1 = GreyscaleImage.createFrom(image1);
        GreyscaleImage greyscaleImage2 = GreyscaleImage.createFrom(image2);

        return or(newSequenceIn(greyscaleImage1, greyscaleImage2, inverted, count, queue),
            newSequenceIn(greyscaleImage1, greyscaleImage2, !inverted, count, queue))
                .map(rectangles -> metricsFrom(rectangles, image2, count + 3));
    }

    private static <T> Optional<T> or(Optional<T> o1, Optional<T> o2) {
        return o1.isPresent() ? o1 : o2;
    }

    static Collection<Rectangle> areasIn(GreyscaleImage image, boolean invert, Queue queue) {
        int luminescenceThreshold = (int) round(LUMINESCENCE_THRESHOLD * GreyscaleImage.RANGE);
        BinaryImage binaryImage = BinaryImage.createFrom(image, SMOOTHING_RADIUS, luminescenceThreshold, invert);
        Collection<Rectangle> rectangles = AreaRecognizer.areasIn(binaryImage, FILL_THRESHOLD, queue);
        return AreaFilter.filter(rectangles, MIN_AREA, MAX_AREA, MAX_ASPECT_RATIO);
    }

    private static Optional<List<Rectangle>> newSequenceIn(GreyscaleImage image1, GreyscaleImage image2,
                                                           boolean inverted, int count, Queue queue) {

        Collection<Rectangle> areas1 = areasIn(image1, inverted, queue);
        Collection<Rectangle> areas2 = areasIn(image2, inverted, queue);

        Collection<Rectangle> newAreas = sorted(difference(areas1, areas2));

        List<List<Rectangle>> sequences = sequencesIn(newAreas, count);

        Collections.sort(sequences, (s1, s2) -> s1.get(0).y - s2.get(0).y);

        return lastIn(sequences);
    }

    static List<List<Rectangle>> sequencesIn(Collection<Rectangle> rectangles, int length) {
        return SequenceRecognizer.findSequencesIn(rectangles, length,
                MIN_STEP, MAX_SIZE_DEVIATION, MAX_DISTANCE_DEVIATION);
    }

    private static Metrics metricsFrom(List<Rectangle> rectangles, BufferedImage image2, int offset) {
        Rectangle first = rectangles.get(0);
        Rectangle last = rectangles.get(rectangles.size() - 1);

        double x1 = first.x + 0.5D * first.width - 1;
        double x2 = last.x + 0.5D * last.width - 1;
        double y1 = first.y + 0.5D * first.height - 1;
        double y2 = last.y + 0.5D * last.height - 1;

        double step = (x2 - x1) / (rectangles.size() - 1);

        Point point = new Point((int) round(x1), (int) round(y1));

        int background = image2.getRGB((int) round(x2 + step * 2), (int) round(y2));

        boolean blockCursor = image2.getRGB((int) round(x2 + step), (int) round(y2)) != background;

        int availableLength = uniformLengthFrom(image2, new Point(point.x + (int) round(step * offset), point.y), background);

        int length = (int) (floor(availableLength / step)) - (blockCursor ? 1 : 0);

        return new Metrics(point, step, length + offset - 1, new Color(background), blockCursor);
    }

    private static int uniformLengthFrom(BufferedImage image, Point point, int color) {
        int width = image.getWidth();

        int x;

        for (x = point.x; x < width; x++) {
            if (image.getRGB(x, point.y) != color) break;
        }

        return x - point.x;
    }

    private static Collection<Rectangle> sorted(Collection<Rectangle> rectangles) {
        List<Rectangle> list = new ArrayList<>(rectangles);
        Collections.sort(list, (r1, r2) -> r1.y == r2.y ? r1.x - r2.x : r1.y - r2.y);
        return list;
    }

    private static Collection<Rectangle> difference(Collection<Rectangle> rectangles1, Collection<Rectangle> rectangles2) {
        Set<Rectangle> result = new HashSet<>(rectangles2);
        result.removeAll(rectangles1);
        return result;
    }

    private static <T> Optional<T> lastIn(List<T> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }
}
