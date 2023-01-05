from os import system, listdir
from json import dumps, loads

if not "files" in listdir("."): system("mkdir files")
else: print("files directory already exists, skipping")

if not "log.log" in listdir("files"): system("touch log.log")
else: print("log file already exists, skipping")

if not "config" in listdir("files"): system("mkdir files/config")
else: print("config directory already exists, skipping")

if not "main.json" in listdir("files/config"): system("touch files/config/main.json")
else: print("main config already exists, skipping")

with open("files/config/main.json", "r") as file:
    current = file.read()
with open("files/config/main.json", "w") as file:
    if len(current) == 0:
        jsonData = {
            "token":input("Specify discord bot token: "),
            "clientId": input("Specify Battle.net client ID: "),
            "clientSecret": input("Specify Battle.net client Secret: "),
            "status": input("Specify the status message to be displayed in discord: ")
        }
        file.write(dumps(jsonData, indent=4))
    else:
        jsonData:dict = loads(current)
        if not "token" in jsonData: jsonData["token"] = input("Specify discord bot token: ")
        if not "clientId" in jsonData: jsonData["clientId"] = input("Specify Battle.net client ID: ")
        if not "clientSecret" in jsonData: jsonData["clientSecret"] = input("Specify Battle.net client Secret: ")
        if not "status" in jsonData: jsonData["status"] = input("Specify the status message to be displayed in discord: ")
        file.write(dumps(jsonData, indent=4))