param(
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$composePath = Join-Path $projectRoot 'compose.lab.yaml'
$dbEnvPath = Join-Path $projectRoot '.env'
$labEnvPath = Join-Path $projectRoot '.env.lab'
$labProject = 'tenantguard-lab-lab'
$labVolume = 'tenantguard-lab-lab_lab_postgres_data'
$devVolume = 'tenantguard-lab_postgres_data'
$composeArgs = @('compose', '--env-file', $dbEnvPath, '--env-file', $labEnvPath, '-f', $composePath)

function Invoke-DockerChecked {
    param([string[]]$DockerArgs)
    $output = & docker @DockerArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Docker command failed with exit code $LASTEXITCODE. No further reset steps will run."
    }
    return $output
}

function Assert-LabVolume {
    $labelsJson = Invoke-DockerChecked @('volume', 'inspect', $labVolume, '--format', '{{json .Labels}}')
    $labels = $labelsJson | ConvertFrom-Json
    if ($labels.'com.docker.compose.project' -cne $labProject -or
        $labels.'com.docker.compose.volume' -cne 'lab_postgres_data') {
        throw 'The volume labels do not identify the expected lab volume.'
    }
}

foreach ($requiredPath in @($composePath, $dbEnvPath, $labEnvPath)) {
    if (-not (Test-Path -LiteralPath $requiredPath -PathType Leaf)) {
        throw "Required local file is missing: $requiredPath"
    }
}

$configJson = Invoke-DockerChecked ($composeArgs + @('config', '--format', 'json'))
$config = ($configJson -join "`n") | ConvertFrom-Json
if ($config.name -cne $labProject -or
    $config.volumes.lab_postgres_data.name -cne $labVolume -or
    $config.services.api.environment.SPRING_PROFILES_ACTIVE -cne 'lab') {
    throw 'Compose configuration does not match the expected lab project, volume and profile.'
}
if ($labVolume -ceq $devVolume) {
    throw 'The lab volume matches the development volume.'
}

Assert-LabVolume
$devInfo = Invoke-DockerChecked @('volume', 'inspect', $devVolume, '--format', '{{.Name}}')
if ($devInfo -cne $devVolume) {
    throw 'The development volume could not be verified.'
}

$attached = @(Invoke-DockerChecked @('ps', '-a', '--filter', "volume=$labVolume", '--format', '{{.Names}}'))
foreach ($container in $attached) {
    if ($container -and $container -cne 'tenantguard-lab-lab-db-1') {
        throw "An unexpected container uses the lab volume: $container"
    }
}

Write-Host "Verified project: $labProject"
Write-Host "Lab volume to replace: $labVolume"
Write-Host "Development volume to preserve: $devVolume"
if ($CheckOnly) {
    Write-Host 'Check only: no containers or volumes were changed.'
    return
}

$confirmation = Read-Host "To erase only the lab data, type RESET $labVolume"
if ($confirmation -cne "RESET $labVolume") {
    Write-Host 'Confirmation did not match. Nothing was changed.'
    return
}

Invoke-DockerChecked ($composeArgs + @('down')) | Out-Host
Assert-LabVolume
$remaining = @(Invoke-DockerChecked @('ps', '-a', '--filter', "volume=$labVolume", '--format', '{{.Names}}')) | Where-Object { $_ }
if ($remaining.Count -ne 0) {
    throw 'The lab volume is still attached to a container; it was not removed.'
}

Invoke-DockerChecked @('volume', 'rm', $labVolume) | Out-Host
Invoke-DockerChecked ($composeArgs + @('up', '-d', '--build')) | Out-Host
Write-Host 'Lab data was reset and the lab was started again.'
