[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$backendRoot = Join-Path $projectRoot 'backend'
$frontendRoot = Join-Path $projectRoot 'frontend'
$backendProcess = $null
$frontendProcess = $null

function Write-Step([string] $Message) {
    Write-Host "[dev] $Message" -ForegroundColor Cyan
}

function Import-DotEnv([string] $Path) {
    if (-not (Test-Path -LiteralPath $Path)) {
        Write-Warning "$Path was not found. System environment variables and application defaults will be used."
        return
    }

    foreach ($rawLine in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $line = $rawLine.Trim()
        if (-not $line -or $line.StartsWith('#')) {
            continue
        }

        $separator = $line.IndexOf('=')
        if ($separator -lt 1) {
            continue
        }

        $name = $line.Substring(0, $separator).Trim()
        $value = $line.Substring($separator + 1).Trim()
        if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        if ($name -match '^[A-Za-z_][A-Za-z0-9_]*$' -and $null -eq [Environment]::GetEnvironmentVariable($name, 'Process')) {
            [Environment]::SetEnvironmentVariable($name, $value, 'Process')
        }
    }
}

function Assert-Command([string] $Name, [string] $InstallHint) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name was not found. $InstallHint"
    }
}

function Resolve-Java21 {
    $candidates = [System.Collections.Generic.List[string]]::new()
    if ($env:JAVA_HOME) {
        $candidates.Add((Join-Path $env:JAVA_HOME 'bin\java.exe'))
    }

    $pathJava = Get-Command 'java.exe' -ErrorAction SilentlyContinue
    if ($pathJava) {
        $candidates.Add($pathJava.Source)
    }

    @(
        (Join-Path $HOME '.jdks\*\bin\java.exe'),
        'C:\Program Files\Java\jdk-*\bin\java.exe',
        'C:\Program Files\Eclipse Adoptium\jdk-*\bin\java.exe'
    ) | ForEach-Object {
        Get-Item -Path $_ -ErrorAction SilentlyContinue | ForEach-Object { $candidates.Add($_.FullName) }
    }

    foreach ($candidate in $candidates | Select-Object -Unique) {
        if (-not (Test-Path -LiteralPath $candidate)) {
            continue
        }
        $previousErrorAction = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try {
            $versionOutput = & $candidate -version 2>&1
            $versionExitCode = $LASTEXITCODE
        } finally {
            $ErrorActionPreference = $previousErrorAction
        }
        if ($versionExitCode -eq 0 -and ($versionOutput -join "`n") -match 'version "21[\.]') {
            return $candidate
        }
    }

    throw 'A working JDK 21 installation was not found. Install JDK 21 or configure JAVA_HOME.'
}

function Assert-PortAvailable([int] $Port, [string] $ServiceName) {
    $listener = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($listener) {
        throw "$ServiceName port $Port is already used by process $($listener.OwningProcess). Stop it or change the port configuration."
    }
}

function Wait-HttpReady([string] $Url, [System.Diagnostics.Process] $Process, [int] $TimeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if ($Process.HasExited) {
            throw "Process exited before it became ready (exit code $($Process.ExitCode))."
        }
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 2
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                return
            }
        } catch {
            Start-Sleep -Milliseconds 500
        }
    }
    throw "Timed out waiting for $Url."
}

function Stop-ProcessTree([System.Diagnostics.Process] $Process) {
    if ($null -eq $Process -or $Process.HasExited) {
        return
    }
    & taskkill.exe /PID $Process.Id /T /F 2>$null | Out-Null
}

try {
    Import-DotEnv (Join-Path $projectRoot '.env')
    Assert-Command 'node' 'Install Node.js 22 or later.'
    Assert-Command 'npm.cmd' 'Install Node.js 22 or later.'

    $javaExecutable = Resolve-Java21
    $javaBin = Split-Path -Parent $javaExecutable
    $env:JAVA_HOME = Split-Path -Parent $javaBin
    $env:Path = "$javaBin;$env:Path"
    Write-Step "Using JDK: $env:JAVA_HOME"

    $serverPort = if ($env:SERVER_PORT) { [int] $env:SERVER_PORT } else { 8090 }
    $frontendPort = if ($env:FRONTEND_PORT) { [int] $env:FRONTEND_PORT } else { 5173 }
    Assert-PortAvailable $serverPort 'Backend'
    Assert-PortAvailable $frontendPort 'Frontend'

    if (-not (Test-Path -LiteralPath (Join-Path $frontendRoot 'node_modules'))) {
        Write-Step 'Installing frontend dependencies for the first run...'
        Push-Location $frontendRoot
        try {
            & npm.cmd install
            if ($LASTEXITCODE -ne 0) {
                throw "Frontend dependency installation failed (exit code $LASTEXITCODE)."
            }
        } finally {
            Pop-Location
        }
    }

    Write-Step 'Building backend...'
    Push-Location $backendRoot
    try {
        & (Join-Path $backendRoot 'mvnw.cmd') -q package -pl his-application -am -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "Backend build failed (exit code $LASTEXITCODE)."
        }
    } finally {
        Pop-Location
    }

    Write-Step 'Starting backend...'
    $backendJar = Join-Path $backendRoot 'his-application\target\his-application-0.1.0-SNAPSHOT.jar'
    if (-not (Test-Path -LiteralPath $backendJar)) {
        throw "Backend JAR was not generated at $backendJar."
    }
    $backendProcess = Start-Process -FilePath $javaExecutable `
        -ArgumentList '-jar', $backendJar `
        -WorkingDirectory $backendRoot -NoNewWindow -PassThru

    Write-Step 'Starting frontend...'
    $frontendProcess = Start-Process -FilePath 'npm.cmd' `
        -ArgumentList 'run', 'dev', '--', '--host', '127.0.0.1', '--port', $frontendPort `
        -WorkingDirectory $frontendRoot -NoNewWindow -PassThru

    Wait-HttpReady "http://127.0.0.1:$serverPort/actuator/health" $backendProcess 90
    Wait-HttpReady "http://127.0.0.1:$frontendPort" $frontendProcess 30

    Write-Host ''
    Write-Host 'Hospital HIS is ready' -ForegroundColor Green
    Write-Host "  Frontend: http://127.0.0.1:$frontendPort"
    Write-Host "  Backend:  http://127.0.0.1:$serverPort"
    Write-Host "  Swagger: http://127.0.0.1:$serverPort/swagger-ui.html"
    Write-Host '  Press Ctrl+C to stop both services.'
    Write-Host ''

    while (-not $backendProcess.HasExited -and -not $frontendProcess.HasExited) {
        Start-Sleep -Seconds 1
    }

    if ($backendProcess.HasExited) {
        throw "Backend exited (exit code $($backendProcess.ExitCode))."
    }
    throw "Frontend exited (exit code $($frontendProcess.ExitCode))."
} catch {
    Write-Host "[dev] Startup failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Write-Step 'Stopping development services...'
    Stop-ProcessTree $frontendProcess
    Stop-ProcessTree $backendProcess
}
