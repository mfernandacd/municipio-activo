@echo off
title Municipio Activo
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0iniciar-municipio.ps1"
if errorlevel 1 (
    echo.
    echo No se pudo iniciar el sistema. Revisa el mensaje anterior.
    pause
)
