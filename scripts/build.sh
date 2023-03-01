verbose=""
if [ "$1" == "--verbose" ]; then
	verbose="-v"
fi

mkdir -p $verbose files
if test -f "files/config.conf"; then
    if [ "$verbose" == "-v" ]; then
    	echo "file 'files/config.conf' already exists"
    fi
else
    if [ "$verbose" == "-v" ]; then
    	echo "initializing file 'files/config.conf'"
    fi
    echo "token=" >> files/config.conf
fi

if [ "$verbose" == "-v" ]; then
    echo "compiling program..."
fi

rm *.jar -f $verbose
if [ "$verbose" == "-v" ]; then
    mvn compile package -f nutbot
    mvn clean -f nutbot
else
    mvn compile package -q -f nutbot
    mvn clean -q -f nutbot
fi

mv *.jar nutbot.jar $verbose

if [ "$1" = "--remote" ]; then
	screen -L -dmS nutbot java -jar nutbot.jar
else
	java -jar nutbot.jar
fi

