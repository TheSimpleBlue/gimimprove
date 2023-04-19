# GIM Improve Plugin
A [Runelite](https://github.com/runelite/runelite) plugin that adds Group Iron Man Quality of Life Improvements - puts GIM chat into PMs and GIM collection log visual indicators


## Features
Color coordinated dot indicators for items unlocked by teammates:

![img_1.png](img_1.png)


Tooltip overlay describing which teammates already unlocked the item:

![img_2.png](img_2.png)


## Config

`Split group chat with private` - if you have split private chat enabled (via Runescape default settings), the group
chat messages will also be added to the private split chat.

`Sync GIM collection Logs` - pull data from [collectionlog.net](https://collectionlog.net) to for getting teammate's
collection log data. Use [Collection Log Plugin](https://runelite.net/plugin-hub/show/collection-log) to upload
collection log data. If the user doesn't have data in [collectionlog.net](https://collectionlog.net), this feature will
not work.

`detect unlock unique to group` - in game chat will indicate if a unique drop you receive is unique to the group.

`usernames in group` - a comma-separated list of usernames, maximum of 5, to load instead of users from your GIM. If
left empty, the plugin will pull your GIM group members and load their collection logs if they exist.

Assign `Colors` to teammates for dot indicators:

![img_3.png](img_3.png)
