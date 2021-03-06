package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;

public interface IRenderEngine {

  public boolean canRender(IEntity entity);

  public void entityRenderingCondition(Predicate<IEntity> predicate);

  public IMapRenderer getMapRenderer(MapOrientation orientation);

  public void onEntityRendered(Consumer<RenderEvent<IEntity>> entity);

  public void onEntityRendering(Consumer<RenderEvent<IEntity>> entity);

  public void onMapRendered(Consumer<RenderEvent<IMap>> map);

  public void render(Graphics2D g, Collection<? extends IRenderable> renderables);

  public void render(Graphics2D g, Collection<? extends IRenderable> renderables, Shape clip);

  public void render(Graphics2D g, IRenderable renderable);

  public void renderEntities(Graphics2D g, Collection<? extends IEntity> entities);

  public void renderEntities(Graphics2D g, Collection<? extends IEntity> entities, boolean sort);

  public void renderEntities(Graphics2D g, Collection<? extends IEntity> entities, boolean sort, Shape clip);

  public void renderEntities(Graphics2D g, Collection<? extends IEntity> entities, Shape clip);

  public void renderEntity(Graphics2D g, IEntity entity);

  public void renderLayers(Graphics2D g, final IMap map, RenderType type);

  public void renderMap(Graphics2D g, final IMap map);
  
  public void renderShape(final Graphics2D g, final Shape shape);
  
  public void renderOutline(Graphics2D g, final Shape shape);
  
  public void renderOutline(Graphics2D g, final Shape shape, Stroke stroke);

  public void renderText(final Graphics2D g, final String text, final double x, final double y);

  public void renderText(final Graphics2D g, final String text, final Point2D location);
}