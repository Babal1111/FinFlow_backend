# Force stop and remove all containers
Write-Host "Stopping and removing all containers..." -ForegroundColor Yellow
$containers = docker ps -aq
if ($containers) {
    docker rm -f $containers
}

# Remove all unused networks
Write-Host "Cleaning up networks..." -ForegroundColor Yellow
docker network prune -f

# Remove dangling volumes (to save disk space)
Write-Host "Cleaning up dangling volumes..." -ForegroundColor Yellow
docker volume prune -f

Write-Host "Docker cleanup complete!" -ForegroundColor Green
