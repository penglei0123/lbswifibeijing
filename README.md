# LBSShow
# “子午快线”位置可视化系统 v3.0
# 版本说明

- 此版本用于redis解耦，与定位引擎wiwide-v4版本对应
- 支持多商场切换
- 支持轨迹优化（路径约束、禁止区约束）
- 支持到店统计（需要计算好到店数据）
- 支持mac topk实时更新（10分钟粒度，定位引擎开启实时更新功能）
- 支持定位坐标与地图适配（坐标转换由插件实现）

# 部署说明

### 版本要求
- jdk1.8+
- tomcat8.0+
- mysql 5.5+
- redis3.0+

### 部署流程
1. 部署lbs.war到tomcat/webapps目录
2. 编辑META-INF/context.xml,修改MYSQL服务器地址、端口帐号密码等相关配置
3. 拷贝所有商场的地图到WebRoot/static/map目录，地图命名为商场ID_楼层编号.jpg
4. 重启tomcat(若1、2步骤顺序交换，则无需重启)
5. 访问http://host:port/lbs/xxx/home
