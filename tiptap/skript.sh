browserify main.js --standalone editor | uglifyjs > tiptapPlain.js
javascript-obfuscator tiptapPlain.js --output tiptapObf.js --compact true --dead-code-injection true --dead-code-injection-threshold 0.71 --disable-console-output true 
style=`cat style.css`
skript=`cat tiptapObf.js`
cp indexBase.html output.html
output=`output.htmp`
sed -i "s/S49XTBoiUmreqgBcmpFD44GV9/$style/" $output
sed -i "s/yWBKpDZHyRg3ukdEa5gpZC8EK/$skript/" $output
