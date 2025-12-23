# ArchSense - AI-Powered Architecture Analysis Platform

An intelligent system that analyzes software architecture diagrams using Claude AI to identify issues, provide recommendations, track architectural evolution, and estimate cloud costs.

## 🚀 Features

- **AI-Powered Analysis**: Claude AI analyzes architecture diagrams and documents
- **Versioned Evolution Tracking**: Compare architectures across versions
- **Constraint-Based Analysis**: Context-aware evaluation based on QPS, latency, and budget
- **Cost Estimation**: AWS cloud cost breakdown per component
- **Confidence Scoring**: AI confidence levels for each identified issue
- **Multi-Format Support**: PNG, PDF, TXT, MD architecture documents

## 🏗️ Architecture

### Backend (Spring Boot Microservices)
- **user-service** (port 8081): User authentication & management
- **project-service** (port 8082): Project & constraint management
- **artifact-service** (port 8083): File upload & storage (S3/local)
- **analysis-service** (port 8084): Analysis orchestration
- **analysis-executor** (port 8085): AI analysis execution (Claude integration)
- **api-gateway** (port 8080): API Gateway with JWT authentication

### Frontend (Angular)
- Single-page application with Material Design
- Real-time analysis status updates
- Interactive cost breakdowns and evolution charts

### Infrastructure
- **Database**: MongoDB (6 separate databases for microservices)
- **Storage**: AWS S3 or local file system
- **AI**: Claude API (Anthropic)
- **Communication**: HTTP/REST (async execution)

## 📦 Prerequisites

- **Java**: 17+
- **Node.js**: 18+
- **MongoDB**: 6.0+
- **Gradle**: 8.0+ (included via wrapper)
- **Angular CLI**: 17+

## 🔧 Environment Setup

### 1. Clone Repository
\`\`\`bash
git clone https://github.com/your-username/archsense.git
cd archsense
\`\`\`

### 2. Setup MongoDB
\`\`\`bash
# Start MongoDB
mongod --dbpath /path/to/data

# Create databases and users (run in mongo shell)
use admin
db.createUser({user: "archsense", pwd: "your_password", roles: ["root"]})
\`\`\`

### 3. Configure Environment Variables

Copy `.env.example` to `.env` in each service and configure:

\`\`\`bash
# Example for analysis-executor
cd analysis-executor
cp .env.example .env
# Edit .env with your actual credentials
\`\`\`

**Required Variables:**
- `ANTHROPIC_API_KEY`: Get from https://console.anthropic.com/
- `AWS_ACCESS_KEY_ID` & `AWS_SECRET_ACCESS_KEY`: AWS credentials (if using S3)
- `MONGODB_URI`: MongoDB connection string
- `JWT_SECRET`: Random 32+ character string

### 4. Update application.yml Files

Replace hardcoded values with `${ENV_VAR}` format (see Security Guide above).

## 🚀 Running the Application

### Backend

\`\`\`bash
# Start all services (from root directory)
# Terminal 1
cd user-service && ../gradlew bootRun

# Terminal 2
cd project-service && ../gradlew bootRun

# Terminal 3
cd artifact-service && ../gradlew bootRun

# Terminal 4
cd analysis-service && ../gradlew bootRun

# Terminal 5
cd analysis-executor && ../gradlew bootRun

# Terminal 6
cd api-gateway && ../gradlew bootRun
\`\`\`

Or use the convenience script:
\`\`\`bash
./start-services.sh
\`\`\`

### Frontend

\`\`\`bash
cd archsense-frontend
npm install
ng serve
\`\`\`

Access at: http://localhost:4200

## 🧪 Testing

### Quick Test Workflow

\`\`\`bash
# 1. Register user
curl -X POST http://localhost:8080/api/users/register \\
  -H "Content-Type: application/json" \\
  -d '{"email":"test@test.com","password":"test12345","name":"Test User"}'

# 2. Login
TOKEN=$(curl -X POST http://localhost:8080/api/users/login \\
  -H "Content-Type: application/json" \\
  -d '{"email":"test@test.com","password":"test12345"}' | jq -r '.token')

# 3. Create project with constraints
PROJECT=$(curl -X POST http://localhost:8080/api/projects \\
  -H "Authorization: Bearer $TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Test Project",
    "description": "Testing Phase 1 features",
    "constraints": {
      "expectedQps": 10000,
      "latencyTargetMs": 100,
      "consistencyLevel": "STRONG",
      "budgetSensitivity": "HIGH"
    }
  }' | jq -r '.id')

# 4. Upload artifact
ARTIFACT=$(curl -X POST http://localhost:8080/api/projects/$PROJECT/artifacts \\
  -H "Authorization: Bearer $TOKEN" \\
  -F "file=@architecture_diagram.png" | jq -r '.id')

# 5. Run analysis
ANALYSIS=$(curl -X POST http://localhost:8080/api/projects/$PROJECT/analyses \\
  -H "Authorization: Bearer $TOKEN" \\
  -H "Content-Type: application/json" \\
  -d "{\"artifactIds\":[\"$ARTIFACT\"]}" | jq -r '.id')

# 6. Check report (wait 30 seconds first)
curl -X GET "http://localhost:8080/api/analyses/$ANALYSIS/report" \\
  -H "Authorization: Bearer $TOKEN" | jq
\`\`\`

## 📊 Phase 1 Features

### 1. Versioned Architecture Evolution
- Automatic version tracking (v1, v2, v3...)
- Links to previous analysis
- Improvement score (0-100)

### 2. Constraint-Based Analysis
Configure project constraints:
- Expected QPS (queries per second)
- Latency target (milliseconds)
- Consistency level (EVENTUAL/STRONG/STRICT)
- Budget sensitivity (LOW/MEDIUM/HIGH)

AI evaluates issues based on these constraints.

### 3. Cost Estimation Engine
- Component-level AWS cost breakdown
- Monthly estimate
- Cost tier (LOW/MEDIUM/HIGH)
- Optimization suggestions

### 4. Evolution Analysis
For version 2+:
- Resolved issues from previous version
- New issues detected
- Regressed issues
- Overall trend (IMPROVING/STABLE/DEGRADING)

### 5. Confidence Scoring
Each issue includes confidence score (0.0-1.0) indicating AI certainty.

## 🛠️ Technology Stack

**Backend:**
- Spring Boot 3.2
- MongoDB 6.0
- AWS S3 SDK
- Claude AI API (Anthropic)
- JWT Authentication

**Frontend:**
- Angular 17
- TypeScript
- RxJS
- Angular Material

## 📈 Roadmap

### Phase 2 (Planned)
- Real-time collaboration features
- Advanced confidence scoring with evidence
- Custom constraints per analysis

### Phase 3 (Future)
- Security vulnerability scanning
- Architecture Decision Records (ADRs)
- Integration with CI/CD pipelines

## 🤝 Contributing

Contributions welcome! Please read CONTRIBUTING.md first.

## 📄 License

MIT License - see LICENSE file for details

## 📧 Contact

Keshav Dave

Email - davekeshav4@gmail.com

Project Link: https://github.com/Keshav_the_way/archsense

---

## 🔑 Create Environment Variable Templates

### **Root `.env.example`**
```bash
# MongoDB Configuration
MONGODB_URI=mongodb://archsense:your_password@localhost:27017

# JWT Configuration (generate with: openssl rand -hex 32)
JWT_SECRET=your_jwt_secret_minimum_32_characters_long
JWT_EXPIRATION_MS=3600000

# Claude AI Configuration
ANTHROPIC_API_KEY=sk-ant-api03-your_key_here
CLAUDE_MODEL=claude-haiku-4-5-20251001
CLAUDE_MAX_TOKENS=10000

# AWS S3 Configuration (optional - can use local storage)
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=eu-north-1
S3_BUCKET_ARTIFACTS=archsense-artifacts-prod
S3_BUCKET_REPORTS=archsense-reports-prod

# Storage Type (s3 or local)
STORAGE_TYPE=local
LOCAL_ARTIFACTS_PATH=./storage/artifacts
LOCAL_REPORTS_PATH=./storage/reports

# Service Ports
USER_SERVICE_PORT=8081
PROJECT_SERVICE_PORT=8082
ARTIFACT_SERVICE_PORT=8083
ANALYSIS_SERVICE_PORT=8084
EXECUTOR_SERVICE_PORT=8085
GATEWAY_PORT=8080
```

---

### Created with ❤️ by Keshav Dave
