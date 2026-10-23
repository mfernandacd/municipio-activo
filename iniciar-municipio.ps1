$ErrorActionPreference = 'Stop'
$exitCode = 1
$passwordPointer = [IntPtr]::Zero

try {
    $defaultHost = 'bziotj8qzahsjnnfoacp-mysql.services.clever-cloud.com'
    $defaultDatabase = 'bziotj8qzahsjnnfoacp'

    $hostInput = Read-Host "Host MySQL [$defaultHost]"
    $databaseInput = Read-Host "Nombre de la base [$defaultDatabase]"
    $portInput = Read-Host 'Puerto MySQL [3306]'
    $username = Read-Host 'Usuario MySQL'
    $securePassword = Read-Host 'Contrasena MySQL (no se mostrara)' -AsSecureString

    $mysqlHost = if ([string]::IsNullOrWhiteSpace($hostInput)) { $defaultHost } else { $hostInput.Trim() }
    $database = if ([string]::IsNullOrWhiteSpace($databaseInput)) { $defaultDatabase } else { $databaseInput.Trim() }
    $port = if ([string]::IsNullOrWhiteSpace($portInput)) { '3306' } else { $portInput.Trim() }

    if ([string]::IsNullOrWhiteSpace($username)) {
        throw 'El usuario MySQL es obligatorio.'
    }
    if ($port -notmatch '^\d+$' -or [int]$port -lt 1 -or [int]$port -gt 65535) {
        throw 'El puerto MySQL debe ser un numero entre 1 y 65535.'
    }
    if ($securePassword.Length -eq 0) {
        throw 'La contrasena MySQL no puede estar vacia.'
    }

    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $env:MYSQL_ADDON_HOST = $mysqlHost
    $env:MYSQL_ADDON_PORT = $port
    $env:MYSQL_ADDON_DB = $database
    $env:MYSQL_ADDON_USER = $username.Trim()
    $env:MYSQL_ADDON_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $env:SPRING_PROFILES_ACTIVE = 'cloud'
    $env:SESSION_COOKIE_SECURE = 'false'
    $env:PORT = '8080'

    foreach ($name in @('DB_HOST', 'DB_PORT', 'DB_NAME', 'DB_USER', 'DB_PASSWORD')) {
        Remove-Item "Env:$name" -ErrorAction SilentlyContinue
    }

    Push-Location (Join-Path $PSScriptRoot 'backend')
    try {
        & .\mvnw.cmd 'spring-boot:run'
        $exitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Host "No se pudo iniciar el sistema: $($_.Exception.Message)" -ForegroundColor Red
}
finally {
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    foreach ($name in @(
        'MYSQL_ADDON_HOST',
        'MYSQL_ADDON_PORT',
        'MYSQL_ADDON_DB',
        'MYSQL_ADDON_USER',
        'MYSQL_ADDON_PASSWORD',
        'SPRING_PROFILES_ACTIVE',
        'SESSION_COOKIE_SECURE',
        'PORT'
    )) {
        Remove-Item "Env:$name" -ErrorAction SilentlyContinue
    }
}

exit $exitCode
