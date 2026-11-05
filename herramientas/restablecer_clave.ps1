# Restablece la contrasena de un usuario de SIGEV (ver RestablecerClave.java).
# Uso, desde PowerShell en la carpeta SIGEV_Web:
#   powershell -ExecutionPolicy Bypass -File herramientas\restablecer_clave.ps1

$ErrorActionPreference = 'Stop'
$proyecto = Split-Path -Parent $PSScriptRoot
$jdk = 'C:\Program Files\Apache NetBeans\jdk\bin'
$driver = Join-Path $proyecto 'web\WEB-INF\lib\mysql-connector-j-26.7.0.jar'
$config = Join-Path $proyecto 'src\java\db.properties'
$salida = Join-Path $env:TEMP 'sigev_restablecer'

if (-not (Test-Path $config)) {
    Write-Host 'Falta src\java\db.properties (cree el archivo a partir de db.properties.example).'
    exit 1
}

if (Test-Path $salida) { Remove-Item -Recurse -Force $salida }
New-Item -ItemType Directory $salida | Out-Null
$codigo = 1
try {
    $fuentes = @(
        (Join-Path $proyecto 'src\java\com\sigev\util\Clave.java'),
        (Join-Path $proyecto 'src\java\com\sigev\util\ConexionBD.java'),
        (Join-Path $PSScriptRoot 'RestablecerClave.java')
    )
    & "$jdk\javac.exe" -encoding UTF-8 -d $salida $fuentes
    if ($LASTEXITCODE -ne 0) { exit 1 }
    Copy-Item $config $salida
    & "$jdk\java.exe" -cp "$salida;$driver" herramientas.RestablecerClave
    $codigo = $LASTEXITCODE
} finally {
    # Se borra la copia de db.properties y las clases compiladas
    Remove-Item -Recurse -Force $salida
}
exit $codigo
