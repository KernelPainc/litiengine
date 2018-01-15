package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.components.JCheckBoxList;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;

public class MapSelectionPanel extends JSplitPane {
  JList<String> mapList;
  JCheckBoxList listObjectLayers;
  DefaultListModel<String> model;
  DefaultListModel<JCheckBox> layerModel;
  JScrollPane mapScrollPane;
  JScrollPane layerScrollPane;
  JScrollPane entityScrollPane;
  private JPopupMenu popupMenu;
  private JMenuItem mntmExportMap;
  private JMenuItem mntmDeleteMap;
  private JTree tree;
  DefaultTreeModel entitiesTreeModel;
  DefaultMutableTreeNode nodeRoot;
  DefaultMutableTreeNode nodeProps;
  DefaultMutableTreeNode nodeLights;
  DefaultMutableTreeNode nodeTriggers;
  DefaultMutableTreeNode nodeSpawnpoints;
  DefaultMutableTreeNode nodeCollisionBoxes;

  /**
   * Create the panel.
   */
  public MapSelectionPanel() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.userPreferences.setMapPanelSplitter(this.getDividerLocation()));
    if (Program.userPreferences.getMapPanelSplitter() != 0) {
      this.setDividerLocation(Program.userPreferences.getMapPanelSplitter());
    }

    this.setMaximumSize(new Dimension(0, 250));
    setContinuousLayout(true);
    mapScrollPane = new JScrollPane();
    mapScrollPane.setMinimumSize(new Dimension(80, 0));
    mapScrollPane.setMaximumSize(new Dimension(0, 250));
    this.setLeftComponent(mapScrollPane);

    model = new DefaultListModel<>();
    layerModel = new DefaultListModel<>();
    mapList = new JList<>();
    mapList.setModel(model);
    mapList.setVisibleRowCount(8);
    mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mapList.setMaximumSize(new Dimension(0, 250));

    mapList.getSelectionModel().addListSelectionListener(e -> {
      if (mapList.getSelectedIndex() < EditorScreen.instance().getMapComponent().getMaps().size() && mapList.getSelectedIndex() >= 0) {
        if (Game.getEnvironment() != null && Game.getEnvironment().getMap().equals(EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex()))) {
          return;
        }

        EditorScreen.instance().getMapComponent().loadEnvironment(EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex()));
        Game.getEnvironment().onEntityAdded(ent -> this.populateMapObjectTree());
        Game.getEnvironment().onEntityRemoved(ent -> this.populateMapObjectTree());
        initLayerControl();
        populateMapObjectTree();
      }
    });

    mapScrollPane.setViewportView(mapList);

    popupMenu = new JPopupMenu();
    addPopup(mapList, popupMenu);

    mntmExportMap = new JMenuItem(Resources.get("hud_exportMap"));
    mntmExportMap.setIcon(new ImageIcon(RenderEngine.getImage("button-map-exportx16.png")));
    mntmExportMap.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    popupMenu.add(mntmExportMap);

    mntmDeleteMap = new JMenuItem(Resources.get("hud_deleteMap"));
    mntmDeleteMap.setIcon(new ImageIcon(RenderEngine.getImage("button-deletex16.png")));
    mntmDeleteMap.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());
    popupMenu.add(mntmDeleteMap);
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_maps"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(11f));
    mapScrollPane.setViewportBorder(border);

    layerScrollPane = new JScrollPane();
    layerScrollPane.setViewportBorder(null);
    layerScrollPane.setMinimumSize(new Dimension(150, 0));
    layerScrollPane.setMaximumSize(new Dimension(0, 250));

    entityScrollPane = new JScrollPane();
    entityScrollPane.setViewportBorder(null);
    entityScrollPane.setMinimumSize(new Dimension(150, 0));
    entityScrollPane.setMaximumSize(new Dimension(0, 250));

    JTabbedPane tabPane = new JTabbedPane();
    tabPane.add(Resources.get("panel_mapObjectLayers"), layerScrollPane);
    tabPane.add(Resources.get("panel_entities"), entityScrollPane);
    tabPane.setMaximumSize(new Dimension(0, 150));

    this.tree = new JTree();
    this.entitiesTreeModel = new DefaultTreeModel(
        this.nodeRoot = new DefaultMutableTreeNode(Resources.get("panel_mapselection_entities")) {
          {
            nodeProps = new DefaultMutableTreeNode(Resources.get("panel_mapselection_props"));
            add(nodeProps);
            nodeLights = new DefaultMutableTreeNode(Resources.get("panel_mapselection_lights"));
            add(nodeLights);
            nodeTriggers = new DefaultMutableTreeNode(Resources.get("panel_mapselection_triggers"));
            add(nodeTriggers);
            nodeSpawnpoints = new DefaultMutableTreeNode(Resources.get("panel_mapselection_spawnpoints"));
            add(nodeSpawnpoints);
            nodeCollisionBoxes = new DefaultMutableTreeNode(Resources.get("panel_mapselection_collboxes"));
            add(nodeCollisionBoxes);
          }
        });

    tree.setModel(entitiesTreeModel);
    tree.setCellRenderer(new CustomTreeCellRenderer());
    tree.setMaximumSize(new Dimension(0, 250));
    MouseListener ml = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (selRow != -1 && e.getClickCount() == 2) {
          EditorScreen.instance().getMapComponent().centerCameraOnFocus();
        }
      }
    };

    tree.addMouseListener(ml);
    tree.addTreeSelectionListener(e -> {
      if (e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
        if (node.getUserObject() instanceof IEntity) {
          IEntity ent = (IEntity) node.getUserObject();
          IMapObject obj = Game.getEnvironment().getMap().getMapObject(ent.getMapId());
          if (obj != null) {
            EditorScreen.instance().getMapComponent().setFocus(obj);
          }
        }
      }
    });

    entityScrollPane.setViewportView(tree);
    tabPane.setIconAt(0, new ImageIcon(RenderEngine.getImage("layer.png")));
    tabPane.setIconAt(1, new ImageIcon(RenderEngine.getImage("object_cube-10x10.png")));
    this.setRightComponent(tabPane);

    listObjectLayers = new JCheckBoxList();
    listObjectLayers.setModel(layerModel);
    listObjectLayers.setMaximumSize(new Dimension(0, 250));
    layerScrollPane.setViewportView(listObjectLayers);

  }

  public void bind(List<Map> maps) {
    model.clear();
    for (int i = 0; i < maps.size(); i++) {
      model.addElement(maps.get(i).getFileName());
    }
    mapList.setVisible(false);
    mapList.setVisible(true);
  }

  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      mapList.clearSelection();
      return;
    }

    if (model.contains(mapName)) {
      mapList.setSelectedValue(mapName, true);
    }
    this.initLayerControl();
    this.populateMapObjectTree();
  }

  public boolean isSelectedMapObjectLayer(String name) {

    // Get all the selected items using the indices
    for (int i = 0; i < listObjectLayers.getModel().getSize(); i++) {
      if (i >= listObjectLayers.getModel().getSize()) {
        return false;
      }
      Object sel = listObjectLayers.getModel().getElementAt(i);
      JCheckBox check = (JCheckBox) sel;
      if (check.getText().startsWith(name) && check.isSelected()) {
        return true;
      }
    }
    return false;
  }

  private void initLayerControl() {
    Map map = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
    layerModel.clear();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      JCheckBox newBox = new JCheckBox(layer.getName() + " (" + layer.getMapObjects().size() + ")");
      if (layer.getColor() != null) {
        BufferedImage img = ImageProcessing.getCompatibleImage(10, 10);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(layer.getColor());
        g.fillRect(0, 0, 9, 9);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 9, 9);
        g.dispose();
        newBox.setIcon(new ImageIcon(img));
      }
      newBox.setSelected(true);
      layerModel.addElement(newBox);
    }

    int start = 0;
    int end = mapList.getModel().getSize() - 1;
    if (end >= 0) {
      listObjectLayers.setSelectionInterval(start, end);
    }
  }

  public int getSelectedLayerIndex() {
    return listObjectLayers.getSelectedIndex();
  }

  private static void addPopup(Component component, final JPopupMenu popup) {
    component.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showMenu(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showMenu(e);
        }
      }

      private void showMenu(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }

  private void populateMapObjectTree() {
    this.nodeRoot.setUserObject(Game.getEnvironment().getEntities().size() + " " + Resources.get("panel_mapselection_entities"));
    this.nodeLights.removeAllChildren();
    this.nodeProps.removeAllChildren();
    this.nodeTriggers.removeAllChildren();
    this.nodeSpawnpoints.removeAllChildren();
    this.nodeCollisionBoxes.removeAllChildren();

    this.nodeLights.setUserObject(Game.getEnvironment().getLightSources().size() + " " + Resources.get("panel_mapselection_lights"));
    this.nodeProps.setUserObject(Game.getEnvironment().getProps().size() + " " + Resources.get("panel_mapselection_props"));
    this.nodeTriggers.setUserObject(Game.getEnvironment().getTriggers().size() + " " + Resources.get("panel_mapselection_triggers"));
    this.nodeSpawnpoints.setUserObject(Game.getEnvironment().getSpawnPoints().size() + " " + Resources.get("panel_mapselection_spawnpoints"));
    this.nodeCollisionBoxes.setUserObject(Game.getEnvironment().getCollisionBoxes().size() + " " + Resources.get("panel_mapselection_collboxes"));

    for (LightSource light : Game.getEnvironment().getLightSources()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(light);
      this.nodeLights.add(node);
    }

    for (Prop prop : Game.getEnvironment().getProps()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(prop);
      this.nodeProps.add(node);
    }

    for (Trigger trigger : Game.getEnvironment().getTriggers()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(trigger);
      this.nodeTriggers.add(node);
    }

    for (Spawnpoint spawn : Game.getEnvironment().getSpawnPoints()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(spawn);
      this.nodeSpawnpoints.add(node);
    }

    for (CollisionBox coll : Game.getEnvironment().getCollisionBoxes()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(coll);
      this.nodeCollisionBoxes.add(node);
    }

    entitiesTreeModel.reload();
  }

  private class CustomTreeCellRenderer implements TreeCellRenderer {
    private final Icon PROP_ICON = new ImageIcon(RenderEngine.getImage("entity.png"));
    private final Icon FOLDER_ICON = new ImageIcon(RenderEngine.getImage("object_cube-10x10.png"));
    private final Icon LIGHT_ICON = new ImageIcon(RenderEngine.getImage("bulb.png"));
    private final Icon TRIGGER_ICON = new ImageIcon(RenderEngine.getImage("trigger.png"));
    private final Icon SPAWMPOINT_ICON = new ImageIcon(RenderEngine.getImage("spawnpoint.png"));
    private final Icon COLLISIONBOX_ICON = new ImageIcon(RenderEngine.getImage("collisionbox.png"));
    private final Icon DEFAULT_ICON = new ImageIcon(RenderEngine.getImage("bullet.png"));

    private final JLabel label = new JLabel();
    private final Border normalBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    private final Border focusBorder = BorderFactory.createDashedBorder(UIManager.getDefaults().getColor("Tree.selectionBorderColor"));

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      if (value.equals(nodeProps)) {
        label.setIcon(PROP_ICON);
      } else if (value.equals(nodeLights)) {
        label.setIcon(LIGHT_ICON);
      } else if (value.equals(nodeRoot)) {
        label.setIcon(FOLDER_ICON);
      } else if (value.equals(nodeTriggers)) {
        label.setIcon(TRIGGER_ICON);
      } else if (value.equals(nodeSpawnpoints)) {
        label.setIcon(SPAWMPOINT_ICON);
      } else if (value.equals(nodeCollisionBoxes)) {
        label.setIcon(COLLISIONBOX_ICON);
      } else if (value instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof Prop) {
          Prop prop = (Prop) node.getUserObject();
          String cacheKey = Game.getEnvironment().getMap().getName() + "-" + prop.getName() + "-" + prop.getMapId() + "-tree";
          BufferedImage propImag;
          if (ImageCache.IMAGES.containsKey(cacheKey)) {
            propImag = ImageCache.IMAGES.get(cacheKey);
          } else {

            final String name = "prop-" + prop.getSpritesheetName().toLowerCase() + "-" + PropState.INTACT.toString().toLowerCase() + ".png";
            final String fallbackName = "prop-" + prop.getSpritesheetName().toLowerCase() + ".png";
            Spritesheet sprite = Spritesheet.find(name);
            if (sprite == null) {
              sprite = Spritesheet.find(fallbackName);
            }
            propImag = ImageProcessing.scaleImage(sprite.getSprite(0), 16, 16, true);
            ImageCache.IMAGES.put(cacheKey, propImag);
          }

          label.setIcon(new ImageIcon(propImag));
        } else {
          label.setIcon(DEFAULT_ICON);
        }
      }

      UIDefaults defaults = UIManager.getDefaults();
      label.setOpaque(true);
      label.setBackground(hasFocus ? defaults.getColor("Tree.selectionBackground") : defaults.getColor("Tree.background"));
      label.setForeground(hasFocus ? defaults.getColor("Tree.selectionForeground") : defaults.getColor("Tree.foreground"));
      label.setBorder(hasFocus ? this.focusBorder : this.normalBorder);
      label.setText(value.toString());

      return label;
    }
  }
}
