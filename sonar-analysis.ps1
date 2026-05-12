# FinFlow - SonarQube Analysis Script (Windows PowerShell)
# Run this from your project root (where pom.xml is)
#
# Usage:
#   .\sonar-analysis.ps1                         # uses default token prompt
#   .\sonar-analysis.ps1 -Token "your_token"     # pass token directly

param(
    [string]$Token = "",
    [string]$SonarUrl = "http://localhost:9000"
)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FinFlow - SonarQube Analysis" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# If no token passed, prompt for it
if ($Token -eq "") {
    Write-Host "Enter your SonarQube token (from http://localhost:9000 → My Account → Security):"
    $Token = Read-Host "Token"
}

if ($Token -eq "") {
    Write-Host "ERROR: Token is required." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 1: Running tests + generating coverage reports..." -ForegroundColor Yellow
mvn clean verify -DskipTests=false

if ($LASTEXITCODE -ne 0) {
    Write-Host "WARNING: Some tests failed. Continuing with analysis anyway..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Step 2: Running SonarQube analysis..." -ForegroundColor Yellow
mvn sonar:sonar -Dsonar.host.url=$SonarUrl -Dsonar.token=$Token -Dsonar.projectKey=finflow "-Dsonar.projectName=FinFlow Loan Management System"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Analysis complete!" -ForegroundColor Green
    Write-Host "  View results at: $SonarUrl/dashboard?id=finflow" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Analysis failed. Check the output above." -ForegroundColor Red
}