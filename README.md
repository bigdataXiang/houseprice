# houseprice
# houseprice

## 项目结构说明

+ 1.服务端

mongdb存储
```
{
	"x":
	"y"
	"time"
	"price" //颜色深度
	...
}
```
get index


api提供json形式的数据

1.get api/gridmap?level=2112?time=2016/2/12

{
	"matrix":[
	[,,,,]
	[,,,,]
	]
	"rect":[{},{},{},{}]
}

2.post api/info
{
	"x":
	"y"
	"level"
	"time"
}
返回
{
房价信息
	...
}

level:1m/px


# java http核心
Request{
url
body
}
public handler（Request req）
｛
	Response  res=this.process(req)
	
	...处理数据:比如 process类  result(json格式)
	
	res.write(result)
	关闭http连接 
｝
public Process{
}
