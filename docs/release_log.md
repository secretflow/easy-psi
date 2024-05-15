# 发版日志

# v0.3.0beta - 2024/03/20
## 下载链接
请访问：[https://hub.docker.com/r/secretflow/easy-psi/tags](https://hub.docker.com/r/secretflow/easy-psi/tags) <br />阿里云mirror：secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/easy-psi
## 子模块版本
引擎(secretflow/psi): 0.3.0beta <br/> 调度框架(secretflow/kusica): 0.6.0b0
## 更新点

1. 容器化部署：平台与kuscia在一个容器中以runp的方式运行引擎
2. 新增信任节点，支持跳过功能审核
3. 适配0.3.0新版本的psi引擎
4. 支持公钥以粘贴的形式添加合作节点
# v0.2.0beta - 2024/01/31
## 下载链接
[https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.2.0beta.tar.gz](https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.2.0beta.tar.gz) <br /> MD5：12695bdd54a891bbecae9831ce40c49c
## 子模块版本
引擎(secretflow/psi): 0.2.0.dev240123<br />调度框架(secretflow/kusica): 0.5.0.dev240119
## 更新点

1. 用户登陆安全加固：密码错误超过一定次数时需要等待一定时间后才能重试。
2. 隐私求交任务提供更多配置项：
   1. ECDH协议增加曲线选择
   2. KKRT协议增加分桶数量配置
   3. RR22协议增加分桶数量配置，低数据模式
   4. 增加重复值检查选项
   5. 增加节点通信超时配置
3. 隐私求交任务安全加固：发起任务前进行双方数据集数量范围提示。
4. 支持日志hash信息上链（需要额外配置，仅支持Hyperledger Fabric）。
# v0.1.2beta - 2024/01/08
## 下载链接
[https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.1.2beta.tar.gz](https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.1.2beta.tar.gz) <br /> MD5：669cdb4e2d3dfa3858ecfdecaa2cdbba
## 子模块版本
引擎(secretflow/psi): v0.1.0beta<br />调度框架(secretflow/kusica): v0.5.0.dev231201
## 更新点

1. 添加了证书文件名、日志表明、任务id校验
2. 添加了发起任务数据表名和结果表名校验

# v0.1.1beta - 2023/12/11
## 下载链接
[https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.1.1beta.tar.gz](https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.1.1beta.tar.gz) <br /> MD5：2f76a9603b95c801b9452804f5f28f14
## 子模块版本
引擎(secretflow/psi): v0.1.0beta<br />调度框架(secretflow/kusica): v0.5.0.dev231201
## 更新点

1. 更新调度框架镜像，修复了一些可用性问题。
2. 增加了任务失败用户手动重试的功能。
3. 修复了大文件无法下载的bug。
4. 修复了一些UI缺陷。
# v0.1.0beta - 2023/11/24
## 下载链接
[https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.1.0beta.tar.gz](https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/easy-psi/packages/Easy-PSI-0.1.0beta.tar.gz) <br /> MD5: ba0c9e5e7ecd690e382305ce8f988e54
## 子模块版本
引擎(secretflow/psi): v0.1.0beta<br />调度框架(secretflow/kusica): v0.5.0.dev231122
## 更新点
Easy PSI 首次发布：

1. 开放 ECDH / KKRT / RR22 协议。
2. 新增断点续传。
3. 新增Inner Join。
