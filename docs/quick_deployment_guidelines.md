# Easy PSI 快速部署指引（仅适用于0.3.0及之后版本）

# Docker 镜像启动命令
注：

- 在 docker 版本 < 19.03 或 linux 内核版本 < 4.8 的情况下需要在 docker 启动时
   - 增加 --cap-add=SYS_PTRACE参数
   - 添加环境变量参数 -e PROOT_NO_SECCOMP=1

```
docker run -itd --init --name=ezpsi \
      --volume=${volume_data_path}:/app/data \
      --volume=${volume_data_path}:/home/kuscia/var/storage/data \
      --volume=${volume_log_path}/pods:/home/kuscia/var/stdout/pods \
      --volume=${volume_log_path}/kuscia:/home/kuscia/var/logs \
      --volume=${volume_log_path}/easypsi:/app/log/easypsi \
      --volume=${volume_log_path}/pods:/app/log/pods \
      --volume=${volume_pad_config_path}:/app/config \
      --volume=${volume_pad_db_path}:/app/db \
      --volume=${volume_pad_script_path}:/app/tmp/scripts \
      --workdir=/home/kuscia \
      -p ${web_port}:8080 \
      -p ${kuscia_port}:1080 \
      -e NODE_ID=${node_id} \
      -e HOST_PATH=${volume_data_path} \
      ${EASYPSI_IMAGE}
```
| volume_data_path       | 安装数据目录                  |
|------------------------|-------------------------|
| volume_log_path        | 日志目录                    |
| volume_pad_config_path | 配置目录                    |
| volume_pad_db_path     | 内置数据库目录                 |
| volume_pad_script_path | 脚本目录，产生脚本便于黑屏操作调用       |
| web_port               | 用户界面web端口               |
| kuscia_port            | PSI 任务端口                |
| node_id                | 本方名称, 须为小写字母或者小写字母+数字组合 |
| EASYPSI_IMAGE          | Easy PSI 镜像，请阅读下文       |

## Docker 镜像
请访问：[https://hub.docker.com/r/secretflow/easy-psi/tags](https://hub.docker.com/r/secretflow/easy-psi/tags) <br /> 我们同时提供了阿里云mirror： secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/easy-psi
# 查看镜像并获取初始密码
docker 首次启动时会产生随机初始密码，请按照下列步骤获取。<br />用户名为admin，且暂不支持更改。

```
# 查看运行容器
docker ps
# 获取初始密码
docker logs ${ezpsi container name/id} | grep pwd
```
首次登陆之后，必须更改密码才能继续操作。
