echo "===========进入git项目happymmall目录============="
cd /developer/git-repository/mmall
echo "==========git切换分之到mmall-v1.0==============="
git checkout v1.0

echo "==================git fetch======================"
git fetch

echo "==================git pull======================"
git pull

echo "===========编译并跳过单元测试===================="
mvn clean package -Dmaven.test.skip=true

echo "======拷贝编译出来的war包到tomcat下-ROOT.war======="
cp /developer/git-repository/mmall/target/mmall.war /home/apache-tomcat-8.5.46/webapps/mmall.war

echo "====================关闭tomcat====================="
/home/apache-tomcat-8.5.46/bin/shutdown.sh

echo "================sleep 10s========================="
for i in {1..10}
do
echo $i"s"
sleep 1s
done

echo "====================启动tomcat====================="
/home/apache-tomcat-8.5.46/bin/startup.sh 
