package de.gurkenlabs.util.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

public class ImageProcessing {

  /**
   * Adds a shadow effect by executing the following steps: 1. Transform visible
   * pixels to a semi-transparent black 2. Flip the image vertically 3. Scale it
   * down 4. Render original image and shadow on a buffered image
   *
   * @param image
   *          the image
   * @param xOffset
   *          the x offset
   * @param yOffset
   *          the y offset
   * @param border
   *          the border
   * @return the buffered image
   */
  public static BufferedImage addShadow(final BufferedImage image, final int xOffset, final int yOffset) {
    if (image == null) {
      return image;
    }

    final BufferedImage shadow = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    final Graphics2D graphics = shadow.createGraphics();

    // Transform visible pixels to a semi-transparent black
    final BufferedImage shadowImage = flashVisiblePixels(image, new Color(0, 0, 0, 40));
    final AffineTransform tx = new AffineTransform();

    // Flip the image vertically
    tx.concatenate(AffineTransform.getScaleInstance(1, -1));
    tx.concatenate(AffineTransform.getTranslateInstance(0, -shadowImage.getHeight()));
    final AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    final BufferedImage rotatedImage = op.filter(shadowImage, null);
    // Drawing the rotated image at the required drawing locations

    final BufferedImage scaledImage = scaleImage(rotatedImage, rotatedImage.getWidth(), rotatedImage.getHeight() / 3);

    // since the lower quarter of the image is empty space for the shadow, we
    // need to draw the shadow with respect to this empty space
    graphics.drawImage(scaledImage, xOffset, yOffset - (scaledImage.getHeight() / 4 + 1), null);

    graphics.dispose();

    return shadow;
  }

  /**
   * All pixels that have the specified color are rendered transparent.
   *
   * @param img
   *          the img
   * @param color
   *          the color
   * @return the image
   */
  public static Image applyAlphaChannel(final Image img, final Color color) {
    if (color == null) {
      return img;
    }

    final ImageFilter filter = new RGBImageFilter() {

      // the color we are looking for... Alpha bits are set to opaque
      public int markerRGB = color.getRGB() | 0xFF000000;

      @Override
      public final int filterRGB(final int x, final int y, final int rgb) {
        if ((rgb | 0xFF000000) == this.markerRGB) {
          // Mark the alpha bits as zero - transparent
          return 0x00FFFFFF & rgb;
        } else {
          // nothing to do
          return rgb;
        }
      }
    };

    final ImageProducer ip = new FilteredImageSource(img.getSource(), filter);
    return Toolkit.getDefaultToolkit().createImage(ip);
  }

  /**
   * Border alpha.
   *
   * @param image
   *          the image
   * @param strokeColor
   *          the stroke color
   * @return the buffered image
   */
  public static BufferedImage borderAlpha(final BufferedImage image, final Color strokeColor) {
    final BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(image, 0, 0, null);
    bGr.dispose();

    for (int y = 0; y < bimage.getHeight(); y++) {
      for (int x = 0; x < bimage.getWidth(); x++) {
        if (needsBorder(image, x, y)) {
          bimage.setRGB(x, y, strokeColor.getRGB());
        }
      }
    }

    return bimage;
  }

  /**
   * All pixels that are not transparent are replaced by a pixel of the
   * specified flashColor.
   *
   * @param playerImage
   *          the player image
   * @param flashColor
   *          the flash color
   * @return the buffered image
   */
  public static BufferedImage flashVisiblePixels(final Image playerImage, final Color flashColor) {
    final BufferedImage bimage = new BufferedImage(playerImage.getWidth(null), playerImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(playerImage, 0, 0, null);
    bGr.dispose();

    for (int y = 0; y < bimage.getHeight(); y++) {
      for (int x = 0; x < bimage.getWidth(); x++) {
        final int pixel = bimage.getRGB(x, y);
        if (pixel >> 24 != 0x00) {
          bimage.setRGB(x, y, flashColor.getRGB());
        }
      }
    }

    return bimage;
  }

  /**
   * Horizontalflip.
   *
   * @param img
   *          the img
   * @return the buffered image
   */
  public static BufferedImage horizontalflip(final BufferedImage img) {
    final int w = img.getWidth();
    final int h = img.getHeight();
    final BufferedImage dimg = new BufferedImage(w, h, img.getType());
    final Graphics2D g = dimg.createGraphics();
    g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
    g.dispose();
    return dimg;
  }

  /**
   * The specified image is scaled to a new dimension with the specified width
   * and height. This method doesn't use anti aliasing for this process to keep
   * the indy look.
   *
   * @param image
   *          the image
   * @param width
   *          the width
   * @param height
   *          the height
   * @return the buffered image
   */
  public static BufferedImage scaleImage(final BufferedImage image, final int width, final int height) {
    final int imageWidth = image.getWidth();
    final int imageHeight = image.getHeight();

    final double scaleX = (double) width / imageWidth;
    final double scaleY = (double) height / imageHeight;
    final AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
    final AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

    return bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
  }

  /**
   * Needs border.
   *
   * @param image
   *          the image
   * @param x
   *          the x
   * @param y
   *          the y
   * @return true, if successful
   */
  private static boolean needsBorder(final BufferedImage image, final int x, final int y) {
    if (y < 0 || y >= image.getHeight()) {
      return false;
    }

    if (x < 0 || x >= image.getWidth()) {
      return false;
    }

    // if the current pixel is not transparent, we cannot stroke it
    if (image.getRGB(x, y) >> 24 != 0x00) {
      return false;
    }

    // check pixel above the current one
    if (y > 0) {
      if (image.getRGB(x, y - 1) >> 24 != 0x00) {
        return true;
      }
    }

    // check below pixel
    if (y < image.getHeight() - 1) {
      if (image.getRGB(x, y + 1) >> 24 != 0x00) {
        return true;
      }
    }

    // check left pixel
    if (x > 0) {
      if (image.getRGB(x - 1, y) >> 24 != 0x00) {
        return true;
      }
    }

    // check right pixel
    if (x < image.getWidth() - 1) {
      if (image.getRGB(x + 1, y) >> 24 != 0x00) {
        return true;
      }
    }

    return false;
  }
}