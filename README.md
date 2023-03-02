# Putricide
Good news everyone! I think I have perfected a plague that will destroy all life on Azeroth!

<p style="color:orange">World of Warcraft Team Managing tool and role manager for Discord.</p>
<img src="https://repository-images.githubusercontent.com/571304196/266beb85-36c3-418e-979d-332b066e55ba">
All rights to any images used for this bot is entirely credited to Activision Blizzard, Inc.

## TLDR
This project is a Discord bot that manages RBG and raid teams for World of Warcraft using the battle.net API. It has a configuration script as described below that can be run by `python scripts/init.py`

## File structure
This bot requires a file structure that is defined as follows:

```
Putricide/
├─ files/
│  ├─ config/
|  |  ├─ <server_id1>/
|  |  |  ├─ config.json
|  |  |  ├─ pvp.json
|  |  |  ├─ raid.json
|  |  |  └─ rolepicker.json
|  |  ├─ <server_id2>/
|  |  └─ main.json
│  └─ log.log
├─ ppbot/
│  ├─ src/
│  ├─ target/
│  └─ pom.xml
├─ ppbot.jar
└─ scripts/
   ├─ build.sh
   ├─ config.sh
   └─ init.py
```

## Initialization
running the script `init.py` with `python scripts/init.py` from the parent directory of the project will initialize the project with the neccessary files in the configuration directory. you will have to provide the client id and client secret of your application for the battle.net API, while also providing a bot token to log into discord with.



Bad news, everyone! I don't think I'm going to make it.
