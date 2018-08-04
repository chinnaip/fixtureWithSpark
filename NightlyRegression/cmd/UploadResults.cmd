REM
REM UploadResults.cmd - Command file to upload the fitnesse results into ALM
REM

SET LOGFILE=c:\Shared\UploadResults.log
SET ERRFILE=c:\Shared\UploadResults.err

REM Execute the Visual Basic Script
C:\Windows\SysWOW64\cscript.exe ^
	C:\SA20\bin\UploadResultsToALM.vbs ^
		-qcurl            "http://qc4f.austin.hp.com/qcbin" ^
		-qcdomain         "TS_DEV" ^
		-qcproject        "SA_20" ^
		-qcuser           "uuu" ^
        -qcpassword       "ppp" ^
		-fitnesseresults  "C:\Shared\ExecuteFitnesse.log" ^
		-fitnesseserver   "http://nsl046.deu.hp.com" ^
		-fitnesseuser     "root" ^
		-fitnessepassword "mapr" ^
		-qcbaseresultset  "Root\Release 1503\NightlyRegression" ^
		-suppresspath     "NightlyRegression.CuriosityTesting." >%LOGFILE%  2>%ERRFILE% 