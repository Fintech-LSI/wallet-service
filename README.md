# 💰 Wallet Service

A Spring Boot microservice for digital wallet and currency management in a fintech ecosystem. 🚀

## 🔍 Project Overview

The **Wallet Service** handles:
- 💳 Digital wallet management
- 💱 Multi-currency operations
- 🔄 Transaction processing
- 🌐 Currency exchange support

## 📂 Project Structure

```
src/main/java/com.fintech.walletservice/
├── config/          # Configuration files
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # Domain entities
├── repository/      # Data repositories
└── service/         # Business logic
```

## 🛠 Tech Stack

- **Framework**: Spring Boot
- **Build**: Maven
- **Database**: PostgreSQL
- **Containerization**: Docker
- **Cloud**: AWS EKS
- **CI/CD**: Jenkins

## 🚀 CI/CD Pipeline Stages

1. 💻 **Code Checkout**
2. 🔍 **Static Code Analysis**
3. 🏗 **Maven Build**
4. 🐳 **Docker Build & Push**
5. ☸️ **Kubernetes Deployment**

## 🚢 Deployment Quick Start

```bash
# Build project
mvn clean package

# Run locally
mvn spring-boot:run

# Docker build
docker build -t wallet-service .

# Kubernetes deploy
kubectl apply -f k8s/ -n fintech
```

## 🔬 Monitoring

- 📊 Application metrics
- 🚨 Error tracking
- 💻 Resource utilization

## 👥 Team

| Avatar                                                                                                  | Name | Role | GitHub |
|---------------------------------------------------------------------------------------------------------|------|------|--------|
| <img src="https://github.com/zachary013.png" width="50" height="50" style="border-radius: 50%"/>        | Zakariae Azarkan | DevOps Engineer | [@zachary013](https://github.com/zachary013) |
| <img src="https://github.com/goalaphx.png" width="50" height="50" style="border-radius: 50%"/>          | El Mahdi Id Lahcen | Frontend Developer | [@goalaphx](https://github.com/goalaphx) |
| <img src="https://github.com/hodaifa-ech.png" width="50" height="50" style="border-radius: 50%"/>       | Hodaifa | Cloud Architect | [@hodaifa-ech](https://github.com/hodaifa-ech) |
| <img src="https://github.com/khalilh2002.png" width="50" height="50" style="border-radius: 50%"/>       | Khalil El Houssine | Backend Developer | [@khalilh2002](https://github.com/khalilh2002) |
| <img src="https://github.com/Medamine-Bahassou.png" width="50" height="50" style="border-radius: 50%"/> | Mohamed Amine BAHASSOU | ML Engineer | [@Medamine-Bahassou](https://github.com/Medamine-Bahassou) |

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push branch
5. Open Pull Request

## 📄 License

MIT License