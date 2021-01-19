
#!/bin/bash
line=`curl https://card.niconi.co.ni/ | grep 'series_info'`
line=${line:22}
echo $line | awk -F 'var' '{print $1}' > data.txt
