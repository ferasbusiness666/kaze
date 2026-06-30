# R8 runs in full mode. Compose, Room, and AndroidX ship their own keep rules
# via consumer ProGuard files, so we only need app-specific keeps here.

# Keep enum values (SearchEngine is referenced by name in SharedPreferences).
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# WebView has no @JavascriptInterface bridge in this app, so nothing to keep there.
# Line numbers help if a release crash is ever shared (no crash-reporting lib bundled).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
