# AI Cost Guard - Git 工作流与部署文档

## 一、Git 分支策略

```
main（主分支）
  │
  ├── develop（开发分支，日常开发合入）
  │     │
  │     ├── feature/xxx（功能分支，从 develop 拉出）
  │     ├── feature/invite-code（示例：邀请码注册功能）
  │     └── feature/cache-page（示例：缓存配置页面）
  │
  ├── hotfix/xxx（紧急修复，从 main 拉出，修完合回 main + develop）
  │
  └── release/x.x.x（发布分支，从 develop 拉出，测试通过后合入 main）
```

| 分支 | 来源 | 合入 | 用途 |
|---|---|---|---|
| `main` | - | - | 生产代码，始终可部署 |
| `develop` | main | main (via release) | 开发集成分支 |
| `feature/*` | develop | develop | 新功能开发 |
| `hotfix/*` | main | main + develop | 紧急修复 |
| `release/*` | develop | main + develop | 版本发布 |

## 二、提交规范

采用 Conventional Commits 格式：

```
<type>(<scope>): <subject>

<body>
```

### Type 类型

| Type | 说明 | 示例 |
|---|---|---|
| `feat` | 新功能 | `feat(auth): 添加邀请码注册` |
| `fix` | Bug 修复 | `fix(proxy): 修复缓存命中时重复转发` |
| `docs` | 文档更新 | `docs: 补充接口文档` |
| `refactor` | 重构 | `refactor(router): 优化 SpEL 解析性能` |
| `chore` | 构建/依赖 | `chore: 添加 spring-boot-starter-aop 依赖` |
| `test` | 测试 | `test(user): 添加用户注册单元测试` |
| `style` | 格式调整 | `style: 统一缩进为 4 空格` |

### Scope 范围

`auth`, `user`, `team`, `apikey`, `project`, `pricing`, `proxy`, `router`, `cache`, `ratelimit`, `stats`, `alert`, `audit`, `config`

### 示例

```
feat(proxy): 添加 SimHash 语义缓存过滤器

- 实现 CacheFilter (order=4)
- 缓存命中时跳过 ForwardFilter
- 缓存 miss 时在转发后写入缓存
```

## 三、Code Review 规范

### PR 检查清单

- [ ] 代码符合分层规范（Controller 不写业务逻辑）
- [ ] 异常使用 BusinessException，不吞异常
- [ ] DTO 有 @Valid 校验注解
- [ ] 新接口已在接口文档中补充
- [ ] 敏感信息未硬编码（Key、密码）
- [ ] Redis Key 有 TTL，不会无限膨胀
- [ ] SQL 查询有索引覆盖
- [ ] 无 N+1 查询问题

## 四、多环境配置

### 配置文件结构

```
src/main/resources/
├── application.yml            # 通用配置（端口、MyBatis-Plus、Knife4j）
├── application-dev.yml        # 开发环境（本地 MySQL/Redis）
├── application-test.yml       # 测试环境（待创建）
└── application-prod.yml       # 生产环境（待创建）
```

### 环境差异

| 配置项 | dev | test | prod |
|---|---|---|---|
| MySQL | localhost:3306 / root:root | 测试服 IP | 生产 RDS |
| Redis | localhost:6379 / 无密码 | 测试服 IP | 生产 Redis 集群 |
| JWT Secret | 固定字符串 | 环境变量 | 环境变量 |
| AES Key | 硬编码 | 环境变量 | 环境变量 |
| 日志级别 | debug | info | warn |
| Knife4j | 开启 | 开启 | **关闭** |
| CORS | 允许所有 | 允许所有 | 仅允许前端域名 |

### 生产环境配置示例（application-prod.yml）

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/ai_cost_guard?...
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

logging:
  level:
    com.aicostguard: warn

knife4j:
  enable: false
```

### 启动指定环境
```bash
java -jar ai-cost-guard-1.0.0.jar --spring.profiles.active=prod
```

## 五、构建与打包

### Maven 打包
```bash
# 跳过测试打包
./mvnw clean package -DskipTests

# 包含测试打包
./mvnw clean package

# 生成物
target/ai-cost-guard-1.0.0.jar
```

### 前端打包
```bash
cd frontend
npm run build

# 生成物
frontend/dist/
```

## 六、部署方案

### 方案一：单机部署（适合内部小团队）

```
服务器
├── Nginx (:80/:443)
│   ├── / → frontend/dist/（静态文件）
│   └── /api → proxy_pass http://127.0.0.1:8080
│
├── Spring Boot (:8080)
│   └── java -jar ai-cost-guard-1.0.0.jar
│
├── MySQL (:3306)
└── Redis (:6379)
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name cost-guard.internal.company.com;

    # 前端静态资源
    location / {
        root /opt/ai-cost-guard/frontend/dist;
        try_files $uri $uri/ /index.html;    # Vue Router history 模式
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 120s;
        proxy_read_timeout 120s;     # LLM API 转发可能较慢
    }
}
```

### 后端进程管理（systemd）

```ini
# /etc/systemd/system/ai-cost-guard.service
[Unit]
Description=AI Cost Guard Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/ai-cost-guard
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m \
  ai-cost-guard-1.0.0.jar \
  --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable ai-cost-guard
sudo systemctl start ai-cost-guard
sudo systemctl status ai-cost-guard

# 查看日志
sudo journalctl -u ai-cost-guard -f
```

### 方案二：Docker 部署

**Dockerfile**
```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/ai-cost-guard-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "app.jar"]
```

**docker-compose.yml**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_USER=root
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: ai_cost_guard
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - app

volumes:
  mysql_data:
```

## 七、测试方案

### 7.1 单元测试

依赖：`spring-boot-starter-test`（JUnit 5 + Mockito）

```java
@SpringBootTest
class SysUserServiceTest {
    @Autowired
    private SysUserService userService;

    @Test
    void testCreateUser() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("testuser");
        req.setPassword("123456");
        userService.createUser(req);
        // 验证用户已创建
        SysUser user = userService.getUserById(...);
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }
}
```

### 7.2 接口测试

推荐工具：
- **Knife4j**：http://localhost:8080/doc.html（项目自带）
- **Postman / Apifox**：导入接口文档
- **curl**：命令行快速测试

接口测试流程：
```
1. POST /api/auth/register  → 注册用户
2. POST /api/auth/login     → 获取 token
3. 带 token 访问管理接口
4. POST /api/apikey/pool     → 添加 LLM Key
5. POST /api/apikey/projects → 创建项目
6. POST /api/apikey/projects/{id}/generate-key → 生成代理 Key
7. POST /api/v1/chat/completions → 用代理 Key 调用 LLM
8. GET /api/stats/summary   → 查看统计
```

### 7.3 运行测试
```bash
./mvnw test                    # 运行所有测试
./mvnw test -Dtest=XxxTest     # 运行指定测试类
```

## 八、排障手册

### 常见问题

| 问题 | 原因 | 解决 |
|---|---|---|
| 启动报 `Communications link failure` | MySQL 未启动或配置错误 | 检查 MySQL 服务和 application-dev.yml |
| 启动报 `Unable to connect to Redis` | Redis 未启动 | 启动 Redis 服务 |
| 登录返回 500 | 数据库未初始化 | 执行 `sql/init.sql` |
| 登录返回"用户不存在" | 未注册用户 | curl 调用注册接口 |
| 代理转发返回 401 | 上游 API Key 无效 | 检查 Key 池中的 Key 是否正确 |
| 代理转发返回 4001 | 上游 API 调用失败 | 检查网络、Key 余额、模型名是否正确 |
| 前端 /api 请求 404 | 后端未启动 | 启动后端 8080 |
| 前端页面空白 | Node 模块未安装 | `cd frontend && npm install` |
| IntelliJ 报"不支持发行版本 5" | 语言级别错误 | Project Structure → Language Level → 17 |
| IntelliJ 报"程序包 xxx 不存在" | Maven 依赖未下载 | Maven → Reload Project |

### 日志查看

```bash
# 开发环境：IDEA 控制台直接看

# 生产环境
sudo journalctl -u ai-cost-guard -f       # systemd
docker logs -f ai-cost-guard-app-1         # Docker
```

### 健康检查

```bash
# 后端是否存活
curl http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
# 返回非 500 即存活

# MySQL 连接
mysql -u root -p -e "SELECT 1"

# Redis 连接
redis-cli ping
```
