# 火车售票记录与查看系统

该项目实现了 1 个服务端和 4 个客户端的示例，用于演示购票记录的采集、存储（本地文件与数据库）及查看。

## 功能概述
1. **通信功能**：服务端基于 `ServerSocket`，为每个连接的客户端创建独立线程，可同时响应 4 个客户端。
2. **记录功能**：客户端从本地文本文件读取购票信息（姓名、身份证号、出发城市、到达城市），发送到服务端；服务端将数据追加写入 `server_storage/tickets.log`。
3. **数据库功能**：服务端使用嵌入式 SQLite 数据库（依赖 `sqlite-jdbc`）将每条记录同步写入 `server_storage/tickets.db`。
4. **显示功能**：`ServerDataViewer` 可从服务端本地文件与数据库读取并打印所有记录。

## 运行步骤
1. **构建项目**
   ```bash
   mvn -q -DskipTests package
   ```

2. **启动服务端**
   ```bash
   java -cp target/train-ticket-system-1.0-SNAPSHOT.jar:~/.m2/repository/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar \
        com.example.tickets.TicketServer
   ```
   默认监听端口为 `5000`，可在命令末尾追加端口号修改。

3. **启动 4 个客户端（示例）**
   - 示例购票文件已放在 `client_data/client1.txt` ~ `client4.txt`。
   - 在新的终端运行：
     ```bash
     java -cp target/train-ticket-system-1.0-SNAPSHOT.jar com.example.tickets.SampleClientLauncher
     ```
   - 或单独运行某个客户端：
     ```bash
     java -cp target/train-ticket-system-1.0-SNAPSHOT.jar com.example.tickets.TicketClient client_data/client1.txt
     ```

4. **查看服务端存储的数据**
   ```bash
   java -cp target/train-ticket-system-1.0-SNAPSHOT.jar:~/.m2/repository/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar \
        com.example.tickets.ServerDataViewer
   ```

> 提示：首次运行会自动创建 `server_storage` 目录、日志文件与 SQLite 数据库。
