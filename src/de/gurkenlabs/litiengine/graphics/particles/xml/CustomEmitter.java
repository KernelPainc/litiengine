package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.LeftLineParticle;
import de.gurkenlabs.litiengine.graphics.particles.OvalParticle;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleOutlineParticle;
import de.gurkenlabs.litiengine.graphics.particles.RightLineParticle;
import de.gurkenlabs.litiengine.graphics.particles.ShimmerParticle;
import de.gurkenlabs.litiengine.graphics.particles.SpriteParticle;
import de.gurkenlabs.litiengine.graphics.particles.TextParticle;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@EmitterInfo(maxParticles = 0, spawnAmount = 0, activateOnInit = true)
public class CustomEmitter extends Emitter {
  private static final Map<String, EmitterData> loadedCustomEmitters;

  static {
    loadedCustomEmitters = new ConcurrentHashMap<>();
  }

  private final EmitterData emitterData;

  public CustomEmitter(EmitterData emitterData) {
    super();
    this.emitterData = emitterData;
    this.init();
  }

  public CustomEmitter(Point2D location, EmitterData emitterData) {
    super(location);
    this.emitterData = emitterData;
    this.init();
  }

  public CustomEmitter(Point2D location, final String emitterXml) {
    super(location);

    this.emitterData = load(emitterXml);
    if (this.emitterData == null) {
      this.delete();
      return;
    }

    this.init();

  }

  public CustomEmitter(final double x, final double y, EmitterData emitterData) {
    super(x, y);
    this.emitterData = emitterData;
    this.init();
  }

  public CustomEmitter(final double x, final double y, final String emitterXml) {
    super(x, y);

    this.emitterData = load(emitterXml);
    if (this.emitterData == null) {
      this.delete();
      return;
    }

    this.init();

  }

  public static EmitterData load(String emitterXml) {
    final String name = FileUtilities.getFileName(emitterXml);
    if (loadedCustomEmitters.containsKey(name)) {
      return loadedCustomEmitters.get(name);
    }

    final EmitterData loaded = XmlUtilities.readFromFile(EmitterData.class, emitterXml);
    if (loaded == null) {
      return null;
    }

    return load(loaded);
  }

  public static EmitterData load(EmitterData emitterData) {
    if (loadedCustomEmitters.containsKey(emitterData.getName())) {
      return loadedCustomEmitters.get(emitterData.getName());
    }

    loadedCustomEmitters.put(emitterData.getName(), emitterData);
    return emitterData;
  }

  public EmitterData getEmitterData() {
    return this.emitterData;
  }

  @Override
  protected Particle createNewParticle() {
    float x;
    float y;
    float deltaX;
    float deltaY;
    float gravityX;
    float gravityY;
    float width;
    float height;
    float deltaWidth;
    float deltaHeight;

    x = this.getEmitterData().getParticleX().get();
    y = this.getEmitterData().getParticleY().get();
    deltaX = this.getEmitterData().getDeltaX().get();
    deltaY = this.getEmitterData().getDeltaY().get();
    gravityX = this.getEmitterData().getGravityX().get();
    gravityY = this.getEmitterData().getGravityY().get();
    width = this.getEmitterData().getParticleWidth().get();
    height = this.getEmitterData().getParticleHeight().get();
    deltaWidth = this.getEmitterData().getDeltaWidth().get();
    deltaHeight = this.getEmitterData().getDeltaHeight().get();

    Particle particle;
    switch (this.getEmitterData().getParticleType()) {
    case LEFTLINE:
      particle = new LeftLineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case DISC:
      particle = new OvalParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case RECTANGLE:
      particle = new RectangleFillParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case RECTANGLE_OUTLINE:
      particle = new RectangleOutlineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case RIGHTLINE:
      particle = new RightLineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case SHIMMER:
      particle = new ShimmerParticle(new Rectangle2D.Float(x, y, this.getWidth(), this.getHeight()), width, height, this.getRandomParticleColor()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case TEXT:
      particle = new TextParticle(this.getEmitterData().getParticleText(), this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case SPRITE:
      Spritesheet sprite = Spritesheet.find(this.getEmitterData().getSpritesheet());
      if (sprite == null) {
        return null;
      }

      particle = new SpriteParticle(sprite.getSprite(MathUtilities.randomInRange(0, sprite.getTotalNumberOfSprites() - 1)), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth)
          .setDeltaHeight(deltaHeight);
      break;
    default:
      particle = new RectangleFillParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    }

    particle.setDeltaWidth(deltaWidth);
    particle.setDeltaHeight(deltaHeight);
    particle.setCollisionType(this.getEmitterData().getCollisionType());
    return particle;
  }

  private void init() {
    // set emitter parameters
    this.setMaxParticles(this.getEmitterData().getMaxParticles());
    this.setParticleMinTTL(this.getEmitterData().getParticleMinTTL());
    this.setParticleMaxTTL(this.getEmitterData().getParticleMaxTTL());
    this.setTimeToLive(this.getEmitterData().getEmitterTTL());
    this.setSpawnAmount(this.getEmitterData().getSpawnAmount());
    this.setSpawnRate(this.getEmitterData().getSpawnRate());
    this.setParticleUpdateRate(this.getEmitterData().getUpdateRate());
    this.setSize(this.getEmitterData().getWidth(), this.getEmitterData().getHeight());

    for (final ParticleColor color : this.getEmitterData().getColors()) {
      this.addParticleColor(color.toColor());
    }
  }
}
