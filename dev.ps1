# Sobe o StreamPulse completo no Windows: infraestrutura, back-end (.jar) e front-end.
# Uso:   .\dev.ps1          -> roda (compila so se o .jar nao existir)
#        .\dev.ps1 -Build   -> recompila o back-end antes (rode com o Docker fechado)
param([switch]$Build)

$root = $PSScriptRoot
$jar = "$root\backend\build\libs\streampulse-backend-0.0.1-SNAPSHOT.jar"

# 1. Compilar o back-end (so quando necessario)
if ($Build -or -not (Test-Path $jar)) {
    Write-Host "==> Compilando o back-end..."
    Push-Location "$root\backend"
    .\gradlew.bat bootJar --no-daemon
    $ok = $LASTEXITCODE -eq 0
    Pop-Location
    if (-not $ok) { Write-Host "!! Falha ao compilar o back-end."; exit 1 }
}

# 2. Garantir que o Docker esta rodando
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "==> Abrindo o Docker Desktop..."
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    do {
        Start-Sleep -Seconds 5
        docker info *> $null
    } while ($LASTEXITCODE -ne 0)
}
Write-Host "==> Docker pronto."

# 3. Infraestrutura
Write-Host "==> Subindo Kafka, PostgreSQL e Redis..."
docker compose -f "$root\docker-compose.yml" up -d

# 4. Back-end em outra janela
Write-Host "==> Iniciando back-end em outra janela..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -Xmx320m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=48m -XX:MaxMetaspaceSize=160m -jar '$jar'"

Write-Host "==> Aguardando o back-end na porta 8080..."
while ($true) {
    try {
        Invoke-WebRequest -UseBasicParsing http://localhost:8080/api/regions/status -TimeoutSec 2 | Out-Null
        break
    } catch {
        Start-Sleep -Seconds 3
    }
}
Write-Host "==> Back-end no ar."

# 5. Front-end
Set-Location "$root\frontend"
if (-not (Test-Path node_modules)) {
    Write-Host "==> Instalando dependencias do front-end..."
    npm install
}
Write-Host "==> Front-end em http://localhost:5173"
npm run dev