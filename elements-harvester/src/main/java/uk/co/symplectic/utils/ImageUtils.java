/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class ImageUtils {
    private ImageUtils() {}

    public static BufferedImage readFile(File inputFile) {
        try {
            BufferedImage image = ImageIO.read(inputFile);

            // Strip any alpha channel from the image
            BufferedImage newImage = new BufferedImage( image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage( image, 0, 0, Color.BLACK, null);

            return newImage;
        } catch (IOException e) {
        }

        return null;
    }

    public static boolean writeFile(BufferedImage image, File outputFile, String format) {
        try {
            ImageIO.write(image, format, outputFile);
            return true;
        } catch (IOException e) {
        }

        return false;
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           boolean higherQuality)
    {
        Object hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int currentWidth, currentHeight;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            currentWidth = img.getWidth();
            currentHeight = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            currentWidth = targetWidth;
            currentHeight = targetHeight;
        }

        do {
            if (higherQuality && currentWidth > targetWidth) {
                currentWidth /= 2;
                if (currentWidth < targetWidth) {
                    currentWidth = targetWidth;
                }
            } else if (currentWidth < targetWidth) {
                currentWidth = targetWidth;
            }

            if (higherQuality && currentHeight > targetHeight) {
                currentHeight /= 2;
                if (currentHeight < targetHeight) {
                    currentHeight = targetHeight;
                }
            } else if (currentHeight < targetHeight) {
                currentHeight = targetHeight;
            }

            BufferedImage tmp = new BufferedImage(currentWidth, currentHeight, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, currentWidth, currentHeight, null);
            g2.dispose();

            ret = tmp;
        } while (currentWidth != targetWidth || currentHeight != targetHeight);

        return ret;
    }
}
