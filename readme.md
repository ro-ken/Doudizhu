#斗地主系统

##一、运行

###1.编译Server.java文件和Reserver.java文件

### 2.运行Server

###3.运行三个Client

##二、大概流程

###1.waiting(等待三个玩家上线)

​	玩家可自由发言，服务器对其进行广播转发
	服务器监听玩家数量，若到达三人，立即进入preparation阶段

###2.preparation(等待三个玩家确认开始游戏)

​	服务器发送同步序列码，告诉客户端进入preparation阶段
	客户端只能发送yes进行确认
	服务器对每个收到的yes进行统计，并广播发送谁确认了
	若收到玩家的yes，则不在对玩家发来的内容进行理会
	若收到了3个yes，游戏进入selecting状态，并广播提示

###3.selecting(发牌并选择地主)

​	服务器发送同步序列码，告诉客户端进入selecting阶段
	服务器告诉每个玩家的序号0，1，2
	服务器给每个玩家发牌转为String类型发送
	客户端对收到的数据转为list类型并打印到屏幕上
	并随机一个序号，turn，发给客户端进行同步，
	客户端判断，turn==myturn？，是输入yes或no
	否则处于静默状态，若输入，有系统提示
	服务器对玩家发的yes或no进行判断，是否为turn玩家，若是，判断数据
	yes: 地主选定，并标记序号，发广播提示，把底三张广播，并把地主序号广播给客户端
		  客户端接收信号，并标记地主序号。若为地主，则接收底三张牌加到list，并重新打印
		  服务器进入running阶段，
	no(或超时):++turn并发送同步，下一个玩家进行选择
	若轮一圈无人选地主，重新洗牌，重复操作

###4.running(开始斗地主)

​	先写一个while循环，再写一个turn轮到谁出牌
	记录上一次出牌者序号和牌
	若记录编号等于自己编号，则说明无人接，可再出牌，若在规定时间没有打出牌，
	自动打出一张最小的，并发给客户端同步
	若记录编号不等于自己编号，这出的牌要比上一次牌大，并更新map，否则进行提示
	若超出时间没有打出，自动过
	需编写两个方法
		一个检查牌的语义是否正确
		一个比较对两组牌比较大小
	若地主先打完，则地主获胜
	若农民先打完，则农民获胜

###5.finish(完结撒花)

​	随意发言，
	赢者可对输着进行嘲讽 -.-

##三、具体细节

​	请参考pdf文档