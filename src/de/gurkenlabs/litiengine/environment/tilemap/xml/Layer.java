package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.LayerProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;

/**
 * The Class Layer.
 */
public abstract class Layer extends CustomPropertyProvider implements ILayer, Serializable {
  private static final long serialVersionUID = -5136089511774411328L;

  /** The name. */
  @XmlAttribute
  private String name;

  /** The width. */
  @XmlAttribute
  private Integer width;

  /** The height. */
  @XmlAttribute
  private Integer height;

  /** The opacity. */
  @XmlAttribute
  private Float opacity;

  /** The visible. */
  @XmlAttribute
  private Integer visible;

  /** The x. */
  @XmlAttribute
  private Integer offsetx;

  /** The y. */
  @XmlAttribute
  private Integer offsety;

  @XmlAttribute
  private int order = -1;

  /**
   * Gets the height.
   *
   * @return the height
   */
  public int getHeight() {
    if (this.height == null) {
      return 0;
    }

    return this.height;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public float getOpacity() {
    if (this.opacity == null) {
      return 1.0f;
    }
    return this.opacity;
  }

  @Override
  public Point getPosition() {
    return new Point(this.getOffsetX(), this.getOffsetY());
  }

  @Override
  public int getOffsetX() {
    if (this.offsetx == null) {
      return 0;
    }

    return this.offsetx;
  }

  @Override
  public int getOffsetY() {
    if (this.offsety == null) {
      return 0;
    }

    return offsety;
  }

  @Override
  public RenderType getRenderType() {
    final String renderTypeProp = this.getCustomProperty(LayerProperty.LAYER_RENDER_TYPE);
    if (renderTypeProp != null && !renderTypeProp.isEmpty()) {
      return RenderType.valueOf(renderTypeProp);
    }

    return RenderType.NORMAL;
  }

  @Override
  public Dimension getSizeInTiles() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  public int getWidth() {
    if (this.width == null) {
      return 0;
    }

    return this.width;
  }

  @Override
  public int getOrder() {
    return this.order;
  }

  @Override
  public boolean isVisible() {
    if (this.visible == null) {
      return true;
    }

    return this.visible > 0;
  }

  @Override
  @XmlTransient
  public void setName(String name) {
    this.name = name;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (order == -1 && parent instanceof Map) {
      Map map = (Map) parent;
      int layerCnt = map.getTileLayers().size();
      layerCnt += map.getImageLayers().size();
      layerCnt += map.getTileLayers().size();
      this.order = layerCnt;
    }

    if (this.offsetx != null && this.offsetx.intValue() == 0) {
      this.offsetx = null;
    }

    if (this.offsety != null && this.offsety.intValue() == 0) {
      this.offsety = null;
    }

    if (this.width != null && this.width.intValue() == 0) {
      this.width = null;
    }

    if (this.height != null && this.height.intValue() == 0) {
      this.height = null;
    }

    if (this.opacity != null && this.opacity.floatValue() == 1.0f) {
      this.opacity = null;
    }

    if (this.visible != null && this.visible.intValue() == 1) {
      this.visible = null;
    }
  }
}
