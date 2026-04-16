package org.example.view;

import org.example.controller.GameController;
import org.example.model.GameContext;
import org.example.model.GameModel;
import org.example.model.GameState;
import org.example.model.entity.GameObject;
import org.example.model.entity.GameObjectTag;
import org.example.model.entity.Player;
import org.example.util.StatsManager;
import org.example.util.Vector2D;

import javax.imageio.ImageIO;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class GamePanel extends JPanel {

    private final GameModel model;
    private final GameController controller;
    private final Camera camera;
    private long lastTime;


    private BufferedImage menuBg, level1Bg, level2Bg, level2LBg, level3Bg, level3LBg;
    private BufferedImage gameBg1, gameBg2, gameBg3, loseBg, winBg;
    private BufferedImage playerImage, playerHitImage, playerEatImage;
    private BufferedImage enemyImage, bossImage, fruitImage, simpleEnemy, wallImage, heartImg;

    public GamePanel(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.camera = new Camera();

        loadImages();


        setFocusable(true);
        addKeyListener(controller.getKeyboardHandler());


        lastTime = System.currentTimeMillis();
        Timer timer = new Timer(16, e -> {
            long now = System.currentTimeMillis();
            double delta = (now - lastTime) / 1000.0;
            lastTime = now;

            controller.update(delta);
            repaint();
        });
        timer.start();
    }

    private void loadImages() {
        try {

            menuBg = loadImage("/menu_bg.png");
            level1Bg = loadImage("/level1_bg.png");
            level2Bg = loadImage("/level2_bg.png");
            level2LBg = loadImage("/level2L_bg.png");
            level3Bg = loadImage("/level3_bg.png");
            level3LBg = loadImage("/level3L_bg.png");
            gameBg1 = loadImage("/game1.png");
            gameBg2 = loadImage("/game2.jpg");
            gameBg3 = loadImage("/game3.png");
            loseBg = loadImage("/lose_bg.png");
            winBg = loadImage("/win_bg.png");
            playerImage = loadImage("/player.png");
            playerHitImage = loadImage("/player_hit.png");
            playerEatImage = loadImage("/player_eat.png");
            enemyImage = loadImage("/enemy.png");
            bossImage = loadImage("/boss.png");
            fruitImage = loadImage("/fruit.png");
            simpleEnemy = loadImage("/simple_enemy.png");
            wallImage = loadImage("/wall.png");
            heartImg = loadImage("/heart.png");
        } catch (Exception ignored) {}
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;


        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        camera.updateScreenSize(getWidth(), getHeight());
        GameState state = model.getGameState();

        switch (state) {
            case MENU -> drawBackground(g2d, menuBg, "Меню (Нажмите 1 для игры, 2 для статистики)");
            case PLANET_SELECT -> drawPlanetSelect(g2d);
            case STATISTIC -> drawStatistics(g2d);
            case PLAYING -> drawPlaying(g2d);
            case PAUSED -> drawBackground(g2d, null, "ПАУЗА (ESC - продолжить, Q - в меню)");
            case WIN -> drawBackground(g2d, winBg, "ПОБЕДА! Уровень пройден!");
            case LOSE -> drawBackground(g2d, loseBg, "ВЫ ПРОИГРАЛИ!");
        }
    }

    private void drawBackground(Graphics2D g2d, BufferedImage img, String defaultText) {
        if (img != null) {
            g2d.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(defaultText, (getWidth() - fm.stringWidth(defaultText)) / 2, getHeight() / 2);
        }
    }

    private void drawPlanetSelect(Graphics2D g2d) {
        int lvlId = model.getCurrentLevelId();

        if (lvlId == 1) {
            drawBackground(g2d, level1Bg, "Уровень 1 (Нажмите ENTER)");
        } else if (lvlId == 2) {
            drawBackground(g2d, model.getUnlockedLevels().contains(2) ? level2Bg : level2LBg, "Уровень 2 (Заблокирован)");
        } else if (lvlId == 3) {
            drawBackground(g2d, model.getUnlockedLevels().contains(3) ? level3Bg : level3LBg, "Уровень 3 (Заблокирован)");
        }
    }

    private void drawPlaying(Graphics2D g2d) {
        BufferedImage bg = switch (model.getCurrentLevelId()) {
            case 1 -> gameBg1;
            case 2 -> gameBg2;
            case 3 -> gameBg3;
            default -> null;
        };

        if (bg != null) {
            g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        GameContext context = model.getCurrentContext();
        if (context == null) return;

        Player player = null;

        for (GameObject obj : context.getAllObjects()) {
            var pos = context.getNewPosition(obj);
            if (pos == null) continue;

            int drawX = camera.worldToScreenX(pos.x());
            int drawY = camera.worldToScreenY(pos.y());
            int width = camera.scaleSize(obj.getWidth());
            int height = camera.scaleSize(obj.getHeight());

            if (obj.getTag() == GameObjectTag.PLAYER) {
                player = (Player) obj;
            }
            if (obj instanceof Player && player.isAttacking()) {
                drawAttackZone(g2d, player);
            }



            drawEntity(g2d, obj, drawX, drawY, width, height);


            if (obj.getTag() == GameObjectTag.ENEMY || obj.getTag() == GameObjectTag.BOSS_ENEMY ||
                    obj.getTag() == GameObjectTag.BUG_ENEMY || obj.getTag() == GameObjectTag.SIMPLE_ENEMY) {
                drawEnemyHearts(g2d, obj, drawX, drawY, width);
            }
        }

        if (player != null) {
            drawPlayerHUD(g2d, player);
        }
    }

    private void drawEntity(Graphics2D g2d, GameObject obj, int x, int y, int w, int h) {
        BufferedImage img = getEntityImage(obj);

        if (img != null) {

            g2d.drawImage(img, x - w / 2, y - h / 2, w, h, null);
        } else {

            g2d.setColor(getEntityColor(obj));
            if ("ellipse".equals(obj.getShapeType())) {
                g2d.fillOval(x - w / 2, y - h / 2, w, h);
            } else {
                g2d.fillRect(x - w / 2, y - h / 2, w, h);
            }
        }
    }
    private void drawAttackZone(Graphics2D g2d, Player p) {
        int x = camera.worldToScreenX(model.getCurrentContext().getNewPosition(p).x());
        int y = camera.worldToScreenY(model.getCurrentContext().getNewPosition(p).y());
        int range = camera.scaleSize(60);

        g2d.setColor(new Color(255, 0, 0, 100));

        Vector2D dir = p.getLastDirection();
        int offsetX = (int)(dir.x() * range);
        int offsetY = (int)(dir.y() * range);

        g2d.fillOval(x + offsetX - range/2, y + offsetY - range/2, range, range);
        g2d.setColor(Color.RED);
        g2d.drawOval(x + offsetX - range/2, y + offsetY - range/2, range, range);
    }

    private BufferedImage getEntityImage(GameObject obj) {
        return switch (obj.getTag()) {
            case PLAYER -> (obj.getCombat() != null && obj.getCombat().isHitVisualActive()) ? playerHitImage : playerImage;
            case SIMPLE_ENEMY -> simpleEnemy;
            case BUG_ENEMY -> enemyImage;
            case BOSS_ENEMY -> bossImage;
            case FRUIT -> fruitImage;
            case WALL -> wallImage;
            default -> null;
        };
    }

    private Color getEntityColor(GameObject obj) {
        return switch (obj.getTag()) {
            case PLAYER -> (obj.getCombat().isHitVisualActive()) ? Color.RED : Color.BLUE;
            case FRUIT -> Color.YELLOW;
            case WALL -> Color.DARK_GRAY;
            case BOSS_ENEMY -> new Color(139, 0, 0);
            default -> Color.RED;
        };
    }

    private void drawEnemyHearts(Graphics2D g2d, GameObject enemy, int x, int y, int width) {
        int hp = enemy.getCombat().health;
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(hp + " HP", x - width / 2, y - width / 2 - 10);
    }
    private void drawStatistics(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24)); // Установим шрифт покрупнее для заголовка
        g2d.drawString("TOP PLAYERS", 400, 50);

        g2d.setFont(new Font("Arial", Font.PLAIN, 18)); // Шрифт для списка
        List<StatsManager.PlayerRecord> tops = StatsManager.getTopPlayers();

        int y = 100;
        for (StatsManager.PlayerRecord r : tops) {

            String levelsInfo = java.util.stream.IntStream.range(0, r.levelWins().length)
                    .mapToObj(i -> "L" + (i + 1) + ": " + r.levelWins()[i])
                    .collect(java.util.stream.Collectors.joining(", "));

            String text = String.format("%s - Total Wins: %d (%s)",
                    r.name(),
                    r.getTotalWins(),
                    levelsInfo);

            g2d.drawString(text, 100, y);
            y += 30;
        }

        g2d.setColor(Color.YELLOW);
        g2d.drawString("Press ESC or C to return", 100, y + 50);
    }
    private void drawPlayerHUD(Graphics2D g2d, Player player) {
        int hp = player.getCombat().health;
        int maxHp = player.getCombat().maxHealth;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(20, 20, 200, 25);

        g2d.setColor(Color.RED);
        g2d.fillRect(20, 20, (int) (200 * ((double) hp / maxHp)), 25);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(20, 20, 200, 25);
        g2d.drawString("Player HP: " + hp + " / " + maxHp, 30, 38);
    }
}