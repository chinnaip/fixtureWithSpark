REM
REM ExecuteFitnesse.cmd - Command file to execute the fitnesse tests
REM

SET USER=uuu
SET PASSWORD=ppp
SET LOGFILE=C:\Shared\ExecuteFitnesse.log
SET ERRFILE=C:\Shared\ExecuteFitnesse.err

DEL %LOGFILE%
DEL %ERRFILE%

C:
CD \fitnesse

java -jar fitnesse-standalone.jar -a %USERNAME%:%PASSWORD% -d "C:\Fitnesse" -c "%USERNAME%:%PASSWORD%:NightlyRegression.CuriosityTesting?suite&format=text" >%LOGFILE% 2>%ERRFILE%

