REM
REM PullMasterAndCompileFixtures.cmd - Command file to pull the newest master branch and compile the Fitnesse Fixtures
REM

SET LOGFILE=C:\Shared\PullMasterAndCompileFixtures.log

REM Pull Master
CD C:\curiosity > %LOGFILE%
git checkout master >> %LOGFILE%
git pull >> %LOGFILE%

REM Build Fixtures
CD curiosity-test\curiosity-fitnesse-fixtures >> %LOGFILE%
CALL mvn clean >> %LOGFILE%
CALL mvn package >> %LOGFILE%

