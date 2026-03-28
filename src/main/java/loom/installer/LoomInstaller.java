package loom.installer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import javax.imageio.ImageIO;

public class LoomInstaller {

    public static void main(String[] args) {
        System.out.println("Loom Installer starting...");

        String minecraftPath = findMinecraftFolder();
        if (minecraftPath == null) {
            System.out.println("Could not find .minecraft folder!");
            return;
        }

        System.out.println("Found .minecraft at: " + minecraftPath);

        File versionsFolder = new File(minecraftPath, "versions");
        File loomVersionFolder = new File(versionsFolder, "loom-1.21.4");
        loomVersionFolder.mkdirs();

        writeVersionJson(loomVersionFolder);
        writeLauncherProfile(minecraftPath);

        System.out.println("\nLoom installed successfully!");
        System.out.println("Open the Minecraft Launcher and look for Loom in your Installations!");
    }

    private static String findMinecraftFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return System.getenv("APPDATA") + "\\.minecraft";
        else if (os.contains("mac")) return System.getProperty("user.home") + "/Library/Application Support/minecraft";
        else return System.getProperty("user.home") + "/.minecraft";
    }

    private static String generateLogoBase64() {
        int size = 128;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(26, 26, 46));
        g.fillRoundRect(0, 0, size, size, 20, 20);

        g.setColor(new Color(200, 169, 110));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(10, 10, 108, 108, 10, 10);

        g.setColor(new Color(124, 111, 205));
        g.setStroke(new BasicStroke(2));
        for (int x = 25; x <= 103; x += 13) {
            g.drawLine(x, 15, x, 113);
        }

        for (int i = 0; i < 8; i++) {
            int y = 25 + i * 12;
            for (int x = 25; x <= 103; x += 13) {
                if ((i + (x / 13)) % 2 == 0) {
                    g.setColor(new Color(232, 201, 126));
                } else {
                    g.setColor(new Color(124, 111, 205));
                }
                g.drawLine(x - 6, y, x + 6, y);
            }
        }

        g.setColor(new Color(200, 169, 110));
        for (int x = 25; x <= 103; x += 13) {
            g.fillOval(x - 3, 10, 6, 6);
            g.fillOval(x - 3, 112, 6, 6);
        }

        g.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            System.out.println("Failed to generate logo: " + e.getMessage());
            return "Grass";
        }
    }

    private static void writeVersionJson(File versionFolder) {
        String json = "{\n" +
                "    \"id\": \"loom-1.21.4\",\n" +
                "    \"type\": \"release\",\n" +
                "    \"mainClass\": \"loom.loader.LoomLoader\",\n" +
                "    \"inheritsFrom\": \"1.21.4\",\n" +
                "    \"releaseTime\": \"2026-01-01T00:00:00+00:00\",\n" +
                "    \"time\": \"2026-01-01T00:00:00+00:00\",\n" +
                "    \"minimumLauncherVersion\": 21,\n" +
                "    \"libraries\": []\n" +
                "}";

        try {
            File jsonFile = new File(versionFolder, "loom-1.21.4.json");
            Files.writeString(jsonFile.toPath(), json);
            System.out.println("Written: loom-1.21.4.json");
        } catch (IOException e) {
            System.out.println("Failed to write version JSON: " + e.getMessage());
        }
    }

    private static void writeLauncherProfile(String minecraftPath) {
        File profilesFile = new File(minecraftPath, "launcher_profiles.json");

        try {
            String content = Files.readString(profilesFile.toPath());
            String logo = generateLogoBase64();
            System.out.println("Generated Loom logo!");

            // Build the profile entry
            String loomProfile =
                    "    \"loom-1.21.4\" : {\n" +
                            "      \"created\" : \"2026-01-01T00:00:00.000Z\",\n" +
                            "      \"icon\" : \"" + logo + "\",\n" +
                            "      \"lastUsed\" : \"2026-01-01T00:00:00.000Z\",\n" +
                            "      \"lastVersionId\" : \"loom-1.21.4\",\n" +
                            "      \"name\" : \"Loom 1.21.4\",\n" +
                            "      \"type\" : \"custom\"\n" +
                            "    },\n";

            // Add after the opening of profiles
            content = content.replace(
                    "\"profiles\" : {",
                    "\"profiles\" : {\n" + loomProfile
            );

            Files.writeString(profilesFile.toPath(), content);
            System.out.println("Written: launcher_profiles.json with logo!");

        } catch (IOException e) {
            System.out.println("Failed to write launcher profile: " + e.getMessage());
        }
    }
}