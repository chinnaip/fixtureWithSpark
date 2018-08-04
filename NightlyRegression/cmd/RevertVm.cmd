REM
REM RevertVm.cmd - Command file to upload the fitnesse results into ALM
REM

SET LOGFILE=C:\Shared\RevertVm.log
SET ERRFILE=C:\Shared\RevertVm.err

REM Execute the Powershell Script
powershell -file C:\SA20\bin\RevertVm.ps1 ^
    "nsl230.deu.hp.com" ^
	"uuu" ^
	"ppp" ^
	"nsl044 - SA2 TestSandbox" ^
	"READY 2.5 OFFLINE"	>%LOGFILE% 2>%ERRFILE%
