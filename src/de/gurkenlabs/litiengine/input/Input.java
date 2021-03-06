package de.gurkenlabs.litiengine.input;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.IGameLoop;

public final class Input {
  private static IGamepadManager gamePadManager;
  private static List<IGamepad> gamePads;
  private static IKeyboard keyboard;
  private static IMouse mouse;

  // we need an own gameloop because otherwise input won't work if the game has
  // been paused
  protected static final GameLoop InputLoop = new GameLoop(Game.getLoop().getUpdateRate());

  private Input() {
  }

  public static void init() {
    keyboard = new KeyBoard();
    mouse = new Mouse();
    if (Game.getConfiguration().input().isGamepadSupport()) {
      gamePads = new CopyOnWriteArrayList<>();
      gamePadManager = new GamepadManager();
    }
  }

  public static void start() {
    InputLoop.start();
    if (gamePadManager != null) {
      gamePadManager.start();
    }
  }

  public static void terminate() {
    InputLoop.terminate();
    if (gamePadManager != null) {
      gamePadManager.terminate();
    }
  }

  public static IGameLoop getLoop() {
    return InputLoop;
  }

  public static IGamepadManager gamepadManager() {
    return gamePadManager;
  }

  public static IKeyboard keyboard() {
    return keyboard;
  }

  public static IMouse mouse() {
    return mouse;
  }

  public static List<IGamepad> gamepads() {
    return gamePads;
  }

  /**
   * Gets the first game pad that is currently available.
   *
   * @return The first available {@link IGamepad} instance
   */
  public static IGamepad getGamepad() {
    if (gamePads.isEmpty()) {
      return null;
    }

    return gamePads.get(0);
  }

  /**
   * Gets the game pad with the specified index if it is still plugged in. After
   * re-plugging a controller while the game is running, its index might change.
   *
   * @param index
   *          The index of the {@link IGamepad}.
   * @return The {@link IGamepad} with the specified index.
   */
  public static IGamepad getGamepad(final int index) {
    if (gamePads.isEmpty()) {
      return null;
    }

    for (final IGamepad gamepad : gamePads) {
      if (gamepad.getIndex() == index) {
        return gamepad;
      }
    }

    return null;
  }
}
