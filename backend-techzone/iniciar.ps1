<#
  TechZone - Backend - arranque local

  Carga las variables de .env, comprueba que esten completas
  y lanza Spring Boot.

  Uso:
    .\iniciar.ps1            arranca el backend
    .\iniciar.ps1 -Build     limpia, compila y arranca
    .\iniciar.ps1 -Build     tambien sirve para verificar compilacion
#>
param(
    [switch]$Build
)

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path

# ------------------------------------------------------------
# 1) .env
# ------------------------------------------------------------
$envPath = Join-Path $raiz ".env"
if (-not (Test-Path $envPath)) {
    Write-Host "Falta .env. Copia .env.example a .env y rellena los valores." -ForegroundColor Red
    exit 1
}

Get-Content -LiteralPath $envPath | ForEach-Object {
    $linea = $_.Trim()
    if ($linea -eq "" -or $linea.StartsWith("#")) { return }
    $i = $linea.IndexOf("=")
    if ($i -lt 1) { return }
    $k = $linea.Substring(0, $i).Trim()
    $v = $linea.Substring($i + 1).Trim()
    if ($v.Length -ge 2 -and ($v[0] -eq '"' -or $v[0] -eq "'")) {
        $v = $v.Substring(1, $v.Length - 2)
    }
    [Environment]::SetEnvironmentVariable($k, $v, "Process")
}

# ------------------------------------------------------------
# 2) comprobaciones
# ------------------------------------------------------------
$requeridas = @(
    "SUPABASE_DB_URL",
    "SUPABASE_DB_USER",
    "SUPABASE_DB_PASSWORD",
    "SUPABASE_URL",
    "SUPABASE_SERVICE_KEY",
    "JWT_SECRET"
)
$faltan = @($requeridas | Where-Object { -not [Environment]::GetEnvironmentVariable($_) })
if ($faltan.Count -gt 0) {
    Write-Host "Variables sin definir en .env: $($faltan -join ', ')" -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------
# 3) java (priorizar Java 25 instalado)
# ------------------------------------------------------------
$candidatos = @(
    'C:\Program Files\Java\jdk-25.0.4.1',
    'C:\Program Files\Java\latest',
    (Join-Path $env:USERPROFILE ".jdks\jdk-25"),
    $env:JAVA_HOME,
    'C:\Program Files\Java\jdk-21.0.11'
)
$encontrado = $null
foreach ($c in $candidatos) {
    if ($c -and (Test-Path -LiteralPath $c -ErrorAction SilentlyContinue)) {
        $encontrado = $c
        break
    }
}
if ($encontrado) {
    $env:JAVA_HOME = $encontrado
} else {
    $jdks = @(Get-ChildItem -LiteralPath (Join-Path $env:USERPROFILE ".jdks") -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending)
    if ($jdks.Count -gt 0) { $env:JAVA_HOME = $jdks[0].FullName }
}
if (-not $env:JAVA_HOME) {
    Write-Host "No encuentro JAVA_HOME ni ningún JDK instalado." -ForegroundColor Red
    exit 1
}
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green

# ------------------------------------------------------------
# 4) build opcional
# ------------------------------------------------------------
$mvnw = Join-Path $raiz "mvnw.cmd"
if (-not (Test-Path $mvnw)) {
    Write-Host "No existe mvnw.cmd en $raiz" -ForegroundColor Red
    exit 1
}

if ($Build) {
    Write-Host "Compilando..." -ForegroundColor Cyan
    & $mvnw -q clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Fallo al compilar." -ForegroundColor Red
        exit 1
    }
}

# ------------------------------------------------------------
# 5) arranque
# ------------------------------------------------------------
Write-Host "Arrancando backend en http://localhost:8080  (Ctrl+C para detener)" -ForegroundColor Green
& $mvnw spring-boot:run
