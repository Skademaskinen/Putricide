mkdir -v files
if test -f "files/config.conf"; then
    echo "file 'files/config.conf' already exists"
else
    echo "initializing file 'files/config.conf'"
    echo "token=" >> files/config.conf
fi
echo "compiling program..."
mvn clean compile package -q -f nutbot