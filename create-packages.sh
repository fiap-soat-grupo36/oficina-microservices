#!/bin/bash

echo "========================================"
echo "Creating package structure for all microservices..."
echo "========================================"

# Catalog Service
echo "Creating catalog-service structure..."
mkdir -p catalog-service/src/main/java/br/com/fiap/oficina/catalog/{controller,service,repository,entity,dto/{request,response},mapper}
mkdir -p catalog-service/src/main/resources
mkdir -p catalog-service/src/test/java/br/com/fiap/oficina/catalog

# Inventory Service
echo "Creating inventory-service structure..."
mkdir -p inventory-service/src/main/java/br/com/fiap/oficina/inventory/{controller,service,repository,entity,dto/{request,response},mapper}
mkdir -p inventory-service/src/main/resources
mkdir -p inventory-service/src/test/java/br/com/fiap/oficina/inventory

# Work Order Service
echo "Creating work-order-service structure..."
mkdir -p work-order-service/src/main/java/br/com/fiap/oficina/workorder/{controller,service,repository,entity,dto/{request,response},mapper,client}
mkdir -p work-order-service/src/main/resources
mkdir -p work-order-service/src/test/java/br/com/fiap/oficina/workorder

# Budget Service
echo "Creating budget-service structure..."
mkdir -p budget-service/src/main/java/br/com/fiap/oficina/budget/{controller,service,repository,entity,dto/{request,response},mapper,client}
mkdir -p budget-service/src/main/resources
mkdir -p budget-service/src/test/java/br/com/fiap/oficina/budget

# Auth Service
echo "Creating auth-service structure..."
mkdir -p auth-service/src/main/java/br/com/fiap/oficina/auth/{controller,service,repository,entity,dto/{request,response},mapper,config}
mkdir -p auth-service/src/main/resources
mkdir -p auth-service/src/test/java/br/com/fiap/oficina/auth

echo ""
echo "========================================"
echo "Done! Package structure created successfully."
echo "========================================"
echo ""
echo "Summary:"
echo "✓ catalog-service: Created"
echo "✓ inventory-service: Created"
echo "✓ work-order-service: Created"
echo "✓ budget-service: Created"
echo "✓ auth-service: Created"
echo ""