# Scripts de Build e Deploy
Este diretório contém scripts para facilitar o build, push de imagens Docker e redeploy no EKS.
## Configuração do HikariCP
As configurações do HikariCP foram adicionadas com valores mínimos:
- maximum-pool-size: 5
- minimum-idle: 2
## Build e Push de Imagens
### Uso:
```bash
./scripts/build-and-push-images.sh
```
### Com tag customizada:
```bash
TAG=v1.0.0 ./scripts/build-and-push-images.sh
```
## Redeploy no EKS
### Uso Interativo:
```bash
./scripts/redeploy-eks.sh
```
### Redeploy de todos os serviços:
```bash
./scripts/redeploy-eks.sh all
```
### Verificar status:
```bash
./scripts/redeploy-eks.sh status
```
Para mais detalhes, consulte os comentários dentro dos scripts.
