# 构建命令

## 开发环境搭建

### 开发环境依赖

* JDK: 17
* Maven: 3.5+
* Docker

## 构建 EasyPsi

EasyPsi 提供了 Makefile 来构建项目，你可以通过`make help`命令查看命令帮助，其中 Development 部分提供了构建能力：

```shell
Usage:
  make <target>

General
  help              Display this help.

Development
  test              Run tests.
  build             Build scretpad binary.
  image             Build docker image with the manager.
  docs              Build docs.
  pack              Build pack all in one with tar.gz.
```

### 测试

在 EasyPsi 项目根目录下：

执行`make test`命令，该命令将会执行项目中所有的测试

### 构建可执行JAR文件

在 EasyPsi 项目根目录下：

执行`make build`命令，该命令将会构建出 EasyPsi 的可执行JAR，构建产物会生成在 ./target/ 目录下。

### 构建 EasyPsi Image

在 EasyPsi 项目根目录下：

执行`make image`命令，该命令将会使用 Docker 命令构建出 EasyPsi 镜像。目前 EasyPsi 暂时仅支持构建 linux/amd64 的 Anolis 镜像。

### 编译文档

在 EasyPsi 项目根目录下：

执行`make docs`命令，该命令会生成 EasyPsi 文档，生成的文档会放在 `docs/_build/html` 目录，用浏览器打开 `docs/_build/html/index.html` 就可以查看文档。

该命令依赖于 python 环境，并且已经安装了 pip 工具；编译文档前请提前安装，否则会执行错误。

### 构建 allinone-package

在 EasyPsi 项目根目录下：

执行`make pack`命令，该命令会生成 secretflow-allinone-package-{github-tag}.tar.gz
包含kuscia镜像、secretflow镜像、easypsi镜像、一键安装脚本、一键卸载脚本。

该命令执行结果依赖 环境变量配置，不配置默认使用最新的镜像

```shell
KUSCIA_IMAGE=""
EASYPSI_IMAGE=""
SECRETFLOW_IMAGE=""

# 默认
KUSCIA_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:latest
EASYPSI_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/easypsi:latest
SECRETFLOW_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:latest
```
