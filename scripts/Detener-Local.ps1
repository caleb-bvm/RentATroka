$ErrorActionPreference = 'Stop'
$taskRaiz = Split-Path -Parent $PSScriptRoot
$taskBase = Join-Path $taskRaiz '.local\tomcat'
$taskPidFile = Join-Path $taskBase 'servidor.pid'
if (-not (Test-Path -LiteralPath $taskPidFile)) { Write-Output 'No hay un servidor local registrado.'; return }
$taskServidorId = [int](Get-Content -LiteralPath $taskPidFile)
$taskServidor = Get-CimInstance Win32_Process -Filter "ProcessId = $taskServidorId"
if ($taskServidor) {
    if (-not $taskServidor.CommandLine.Contains($taskBase)) {
        throw 'Ese proceso no corresponde al servidor local de este proyecto; no se detuvo.'
    }
    Stop-Process -Id $taskServidorId
}
Remove-Item -LiteralPath $taskPidFile
Write-Output 'Tomcat local detenido. MySQL conserva sus datos y puede detenerse con docker compose -p rentatroka stop.'

