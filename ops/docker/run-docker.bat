@echo off
setlocal EnableDelayedExpansion
cd /d %~dp0
echo Checking Docker availability...
where docker >nul 2>&1
if errorlevel 1 (
  echo ERROR: Docker is not installed or not in PATH.
  pause
  exit /b 1
)
echo Checking Docker Compose availability...
docker compose version >nul 2>&1
if errorlevel 1 (
  echo ERROR: docker compose is not available.
  pause
  exit /b 1
)

set "LOCAL_ENV=%~dp0..\environment\.env"
set "ROOT_ENV=%~dp0..\..\.env"
set "USE_ENV="
if exist "%LOCAL_ENV%" set "USE_ENV=%LOCAL_ENV%"
if not defined USE_ENV if exist "%ROOT_ENV%" set "USE_ENV=%ROOT_ENV%"

if defined USE_ENV (
  echo Using env file: "!USE_ENV!"
) else (
  echo Warning: no ops\environment\.env or repo-root .env — set POSTGRES_PASSWORD and JWT_SECRET or compose will fail.
)

echo Validating docker-compose...
if defined USE_ENV (
  docker compose --env-file "!USE_ENV!" -f "%~dp0docker-compose.yml" config >nul 2>&1
) else (
  docker compose -f "%~dp0docker-compose.yml" config >nul 2>&1
)
if errorlevel 1 (
  echo ERROR: docker compose config failed.
  pause
  exit /b 1
)

echo Starting stack from ops\docker...
if defined USE_ENV (
  docker compose --env-file "!USE_ENV!" -f "%~dp0docker-compose.yml" up --build
) else (
  docker compose -f "%~dp0docker-compose.yml" up --build
)
if errorlevel 1 (
  echo ERROR: docker compose up failed.
  pause
  exit /b 1
)
echo Done.
pause
