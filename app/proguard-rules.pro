# Keep the Accessibility Service — Android loads it by class name
-keep class com.skipper.adskip.AdSkipperService { *; }

# Keep our activities — Android loads them by class name
-keep class com.skipper.adskip.MainActivity { *; }
-keep class com.skipper.adskip.LogActivity { *; }
-keep class com.skipper.adskip.PauseListActivity { *; }

# Keep settings/log/pause-list helper objects
-keep class com.skipper.adskip.Settings { *; }
-keep class com.skipper.adskip.SkipLog { *; }
-keep class com.skipper.adskip.PauseList { *; }

# Don't warn about missing classes
-dontwarn com.google.**
