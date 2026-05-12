@echo off
echo ========================================================
echo   FinFlow - Single Click Deployment (Docker)
echo ========================================================
echo.
echo Starting all services (this may take several minutes on first run)...
echo.

docker-compose up --build -d

echo.
echo ========================================================
echo   All services are starting up!
echo.
echo   Frontend:    http://localhost:80
echo   API Gateway: http://localhost:8080
echo   Eureka:      http://localhost:8761
echo   SonarQube:   http://localhost:9000
echo   Zipkin:      http://localhost:9411
echo.
echo   Note: Services take 1-2 minutes to be fully ready.
echo ========================================================
pause
