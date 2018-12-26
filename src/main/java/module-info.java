module fr.landel.calc {
    // swing UI
    requires transitive java.desktop;

    // embedded images
    opens images;

    // embedded resource bundles
    opens i18n;

    // project packages
    exports fr.landel.calc;
}