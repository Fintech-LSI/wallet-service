# Wallet Service

![CI/CD Pipeline](/images/pipeline-diagram.png)

A Spring Boot microservice for managing digital wallets and currency transactions in a fintech ecosystem.

## Project Overview

The Wallet Service is a microservice built with Spring Boot that handles:
- Digital wallet management
- Currency operations
- Transaction processing
- Multi-currency support

## Project Structure

```
src/main/java/com.fintech.walletservice/
├── config/          # Configuration files
├── controller/      # REST controllers
│   ├── CurrencyController
│   └── WalletController
├── dto/            # Data Transfer Objects
│   ├── requests/   # Request DTOs
│   └── responses/  # Response DTOs
├── entity/         # Domain entities
│   ├── Currency
│   └── Wallet
├── repository/     # Data repositories
│   ├── CurrencyRepository
│   └── WalletRepository
└── service/        # Business logic
    ├── CurrencyService
    ├── TransactionProducerService
    └── WalletService
```

## Tech Stack

- **Framework**: Spring Boot
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Container**: Docker
- **Container Registry**: AWS ECR
- **Orchestration**: Kubernetes (EKS)
- **CI/CD**: Jenkins
- **Code Quality**: SonarQube

## CI/CD Pipeline

Our automated pipeline includes:

1. **Code Checkout**: Retrieves code from the repository
2. **Static Code Analysis**: SonarQube scan (currently commented out)
3. **Build**: Maven package generation
4. **Docker Build**: Creates container image
5. **Image Push**: Publishes to AWS ECR
6. **Deployment**: Automated deployment to EKS

## Deployment

The service is deployed to AWS EKS using Kubernetes manifests located in the `k8s/` directory:
- `configmap.yaml`: Environment configurations
- `secrets.yaml`: Sensitive data
- `deployment.yaml`: Pod specifications
- `service.yaml`: Service configurations

## Getting Started

1. **Prerequisites**
    - Java 17+
    - Maven
    - Docker
    - AWS CLI
    - kubectl

2. **Local Development**
   ```bash
   # Build the project
   mvn clean package

   # Run locally
   mvn spring-boot:run
   ```

3. **Docker Build**
   ```bash
   docker build -t wallet-service .
   ```

4. **Deploy to Kubernetes**
   ```bash
   # Configure kubectl
   aws eks update-kubeconfig --region region --name cluster-name

   # Apply manifests
   kubectl apply -f k8s/
   ```

## Monitoring

The service includes monitoring integration for:
- Application metrics
- Performance monitoring
- Error tracking
- Resource utilization

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests
4. Submit a pull request

## Contributors

## Contributors

| Name | Role | GitHub |
|------|------|--------|
| Zakariae Azarkan | DevOps Engineer | [@zachary013](https://github.com/zachary013) |
| El Mahdi Id Lahcen | Frontend Developer | [@goalaphx](https://github.com/goalaphx) |
| Hodaifa | Cloud Architect | [@hodaifa-ech](https://github.com/hodaifa-ech) |
| Khalil El Houssine | Backend Developer | [@khalilh2002](https://github.com/khalilh2002) |
| Mohamed Amine BAHASSOU | ML Engineer | [@Medamine-Bahassou](https://github.com/Medamine-Bahassou) |

## License

[Add your license information here]
