mkdir -v files
if test -f "files/config.conf"; then
    echo "file 'files/config.conf' already exists"
else
    echo "initializing file 'files/config.conf'"
    echo "token=" >> files/config.conf
fi
echo "compiling program..."
mvn clean compile package -q -f nutbot
java -jar nutbot-3.1a.jar $(curl -u $(/usr/bin/cat files/config.conf | grep clientId | sed 's:.*=::'):$(/usr/bin/cat files/config.conf | grep clientSecret | sed 's:.*=::') -d grant_type=client_credentials https://oauth.battle.net/token)