param(
    [int]$Puerto = 18080,
    [string]$TomcatHome = $env:CATALINA_HOME
)

$ErrorActionPreference = 'Stop'
$taskRaiz = Split-Path -Parent $PSScriptRoot
$taskWar = Join-Path $taskRaiz 'target\RentATroka.war'
$taskBase = Join-Path $taskRaiz '.local\tomcat'
$taskPidFile = Join-Path $taskBase 'servidor.pid'

if (-not $TomcatHome -or -not (Test-Path -LiteralPath (Join-Path $TomcatHome 'bin\bootstrap.jar'))) {
    throw 'Configura CATALINA_HOME con la carpeta de Tomcat 11, o usa -TomcatHome.'
}
if (-not (Test-Path -LiteralPath $taskWar)) {
    throw 'Primero compila el proyecto con mvn clean verify o con Clean and Build en NetBeans.'
}
if ($Puerto -lt 1024 -or $Puerto -gt 65535) { throw 'El puerto debe estar entre 1024 y 65535.' }
if (Test-Path -LiteralPath $taskPidFile) {
    $taskServidorId = [int](Get-Content -LiteralPath $taskPidFile)
    $taskServidor = Get-CimInstance Win32_Process -Filter "ProcessId = $taskServidorId"
    if ($taskServidor -and $taskServidor.CommandLine.Contains($taskBase)) {
        throw 'El servidor local ya esta iniciado. Usa scripts\Detener-Local.ps1 antes de reiniciarlo.'
    }
}
$taskSonda = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Puerto)
try { $taskSonda.Start() } finally { $taskSonda.Stop() }

foreach ($taskCarpeta in @('conf', 'logs', 'temp', 'webapps', 'work')) {
    New-Item -ItemType Directory -Path (Join-Path $taskBase $taskCarpeta) -Force | Out-Null
}
foreach ($taskArchivo in @('web.xml', 'context.xml', 'catalina.properties', 'logging.properties')) {
    Copy-Item -LiteralPath (Join-Path $TomcatHome "conf\$taskArchivo") -Destination (Join-Path $taskBase "conf\$taskArchivo")
}
Copy-Item -LiteralPath $taskWar -Destination (Join-Path $taskBase 'webapps\RentATroka.war')
$taskServerXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<Server port="-1">
  <Service name="Catalina">
    <Connector address="127.0.0.1" port="$Puerto" protocol="HTTP/1.1" connectionTimeout="20000" />
    <Engine name="Catalina" defaultHost="localhost">
      <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true" />
    </Engine>
  </Service>
</Server>
"@
Set-Content -LiteralPath (Join-Path $taskBase 'conf\server.xml') -Value $taskServerXml -Encoding utf8

$taskJava = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { (Get-Command java.exe).Source }
$taskClasspath = (Join-Path $TomcatHome 'bin\bootstrap.jar') + ';' + (Join-Path $TomcatHome 'bin\tomcat-juli.jar')
$taskArgumentos = @(
    "-Dcatalina.home=`"$TomcatHome`"",
    "-Dcatalina.base=`"$taskBase`"",
    "-Djava.io.tmpdir=`"$(Join-Path $taskBase 'temp')`"",
    '-Dfile.encoding=UTF-8',
    '-classpath', "`"$taskClasspath`"",
    'org.apache.catalina.startup.Bootstrap', 'start'
)
$taskProceso = Start-Process -FilePath $taskJava -ArgumentList $taskArgumentos -WorkingDirectory $taskRaiz `
    -WindowStyle Hidden -PassThru `
    -RedirectStandardOutput (Join-Path $taskBase 'logs\salida.log') `
    -RedirectStandardError (Join-Path $taskBase 'logs\errores.log')
Set-Content -LiteralPath $taskPidFile -Value $taskProceso.Id
$taskUrl = "http://127.0.0.1:$Puerto/RentATroka/login"
for ($taskIntento = 0; $taskIntento -lt 45; $taskIntento++) {
    if ($taskProceso.HasExited) { throw "Tomcat no pudo iniciar. Revisa $taskBase\logs\errores.log" }
    try {
        $taskRespuesta = Invoke-WebRequest -Uri $taskUrl -UseBasicParsing -TimeoutSec 2
        if ($taskRespuesta.StatusCode -eq 200) { Write-Output "RentATroka disponible en $taskUrl"; return }
    } catch { Start-Sleep -Milliseconds 500 }
}
throw "No se pudo confirmar el inicio. Revisa $taskBase\logs\errores.log"

