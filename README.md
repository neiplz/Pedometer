# Pedometer
Pedometer App Client

###配置Gradle
* 根据实际情况配置sdk相关版本

###配置连接AppServer的相关参数
*	文件位置：
		app\src\main\java\com\github\neiplz\pedometer\utils\Constants.java
*	参数清单

		URL_LOGIN_CHECK
		URL_USER_REGISTER
		URL_UPDATE_PASSWORD
		URL_LOAD_INFO
		URL_UPDATE_USER_INFO
		URL_SYNC_PREF

*	更改服务器IP、端口号，即已配置好的AppServer的IP地址和监听的 端口号

###编译并将apk推送至手机


####建议将设置里面的灵敏度调整到3,5左右（支持小数，根据不同手机，参数需进行相应调整）
