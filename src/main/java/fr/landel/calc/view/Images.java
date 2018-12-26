package fr.landel.calc.view;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Optional;

import javax.swing.ImageIcon;

public enum Images {
    CALCULATOR("calculator.png"),
    CALCULATOR_16("calculator16.png"),
    CALCULATOR_32("calculator32.png"),

    ABOUT("about.png"),
    CLEAR("clear.png"),
    COPY("copy.png"),
    CUT("cut.png"),
    DELETE("delete.png"),
    EXIT("exit.png"),
    HELP("help.png"),
    INSERT("insert.png"),
    NULL("null.png"),
    PASTE("paste.png"),
    PREFERENCES("preferences.png");

    private static final String DIR = "images/";

    private final String path;
    private final Optional<URL> url;
    private final Optional<Image> icon;
    private final Optional<ImageIcon> image;

    private Images(final String path) {
        this.path = DIR + path;
        URL url;
        Image icon;
        ImageIcon image;
        try {
            url = this.getClass().getClassLoader().getResource(this.path);
            image = new ImageIcon(url);
            icon = Toolkit.getDefaultToolkit().getImage(url);
        } catch (NullPointerException e) {
            url = null;
            image = null;
            icon = null;
        }
        this.url = Optional.ofNullable(url);
        this.icon = Optional.ofNullable(icon);
        this.image = Optional.ofNullable(image);
    }

    public Optional<ImageIcon> getImage() {
        return this.image;
    }

    public String getPath() {
        return this.path;
    }

    public Optional<URL> getUrl() {
        return this.url;
    }

    public Optional<Image> getIcon() {
        return this.icon;
    }

    @Override
    public String toString() {
        return this.path;
    }
}
