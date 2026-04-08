@echo off
setlocal
cd /d %~dp0
echo Checking Docker availability...
where docker >nul 2>&1
if errorlevel 1 (
  echo ERROR: Docker is not installed or not in PATH.
  echo Install Docker Desktop for Windows and then reopen this terminal.
  pause
  exit /b 1
)
echo Checking Docker Compose availability...
docker compose version >nul 2>&1
if errorlevel 1 (
  echo ERROR: docker compose is not available.
  echo Make sure Docker Desktop is installed and Compose is enabled.
  pause
  exit /b 1
)
echo Checking Docker compose configuration...
if exist .env (
  echo Loading .env file...
) else (
  echo Warning: .env file not found. Using defaults from docker-compose.yml and .env.example if available.
)
echo Validating docker-compose...
docker compose config >nul 2>&1
if errorlevel 1 (
  echo ERROR: docker compose config failed. Fix the compose file first.
  pause
  exit /b 1
)
echo Docker compose is valid. Starting local Docker stack...
docker compose up --build
if errorlevel 1 (
  echo.
  echo ERROR: docker compose up failed. Review the logs above.
  pause
  exit /b 1
)
echo.
echo Docker compose finished successfully.
pause
