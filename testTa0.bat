@echo off

setlocal ENABLEDELAYEDEXPANSION

set "CMD=ibmcloud service key-show ta0 user0"
for /f "tokens=* skip=4" %%a in ('%CMD%') do (set LINE=%%a & set KEY=!KEY!!LINE!)
@echo %KEY%

for /f "delims=" %%a in ('cmd /c "echo %KEY% | jq -r .password"') do set PASSWORD=%%a
for /f "delims=" %%a in ('cmd /c "echo %KEY% | jq -r .username"') do set USERNAME=%%a
for /f "delims=" %%a in ('cmd /c "echo %KEY% | jq -r .url"') do set URL=%%a

set "URL=%URL%/v3/tone^?version^=2017-09-21"

@echo URL=%URL%

set "TEXT={"text": "On en a gros !"}"

echo %TEXT% > tone.json

set "CMD=curl -X POST -u ^"%USERNAME%:%PASSWORD%^" ^
-H "Content-Type: application/json" -H "Content-Language: fr" -H "Accept-Language: fr" ^
--data-binary @tone.json %URL%"

@echo %CMD%
for /f "tokens=*" %%a in ('cmd /c "%CMD%"') do (set LINE=%%a & set OUTPUT=!OUTPUT!!LINE!)

@echo %TEXT% | jq -r ".text"  
@echo %OUTPUT% | jq -r ".document_tone.tones[].tone_name"

