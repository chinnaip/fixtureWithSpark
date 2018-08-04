REM
REM DeployFitnesseTestsAndFixtures.cmd - Command file to deploy the Fitnesse Tests and Fixtures
REM

SET LOGFILE=C:\Shared\DeployFitnesseTestsAndFixtures.log

SET CURIOSITY_TEST_HOME="C:\curiosity\curiosity-test"
SET STAGING="C:\Staging"
SET FITNESSE_ROOT="C:\Fitnesse\FitNesseRoot"
SET FIXTURE_HOME="C:\SA20Test"
SET SA20_HOME="C:\SA20"

REM Staging: Remove old dirs and create new ones
RMDIR /S /Q C:\Staging > %LOGFILE%
MKDIR C:\Staging >> %LOGFILE%
MKDIR C:\Staging\SA20Test >> %LOGFILE%

REM  Staging: Copy Fitnesse Structure
XCOPY %CURIOSITY_TEST_HOME%\FitNesseRoot\CuriosityStaging\CuriosityTesting %STAGING%\CuriosityTesting /E /I >> %LOGFILE%

REM  Staging: Copy Fixture Classes
COPY  %CURIOSITY_TEST_HOME%\curiosity-fitnesse-fixtures\target\CuriosityIntegrationTest-1.0.jar %STAGING%\SA20Test\ /Y >> %LOGFILE%
XCOPY %CURIOSITY_TEST_HOME%\curiosity-fitnesse-fixtures\src\main\resources                      %STAGING%\SA20Test\resources  /E /I /Y >> %LOGFILE%
XCOPY %CURIOSITY_TEST_HOME%\curiosity-fitnesse-fixtures\target\dependency                       %STAGING%\SA20Test\dependency /E /I /Y >> %LOGFILE%

REM Staging: Copy Scripts
XCOPY %CURIOSITY_TEST_HOME%\sandbox\scripts  %STAGING%\UnixScripts /E /I /Y >> %LOGFILE%
%SA20_HOME%\bin\dos2unix %STAGING%\UnixScripts\*.sh >> %LOGFILE%

REM  Staging: Convert Hbase result file to UNIX format
%SA20_HOME%\bin\dos2unix %STAGING%\SA20Test\resources\HbaseIntegration\expected.result >> %LOGFILE%

REM  Remove old tests from FitnesseRoot
RMDIR %FITNESSE_ROOT%\NightlyRegression\CuriosityTesting /S /Q >> %LOGFILE%
RMDIR %FITNESSE_ROOT%\NightlyRegressionBackup\CuriosityTesting /S /Q >> %LOGFILE%
RMDIR %FITNESSE_ROOT%\NightlyRegressionDev\CuriosityTesting /S /Q >> %LOGFILE%

REM  Copy ne tests from  Staging to FitnesseRoot
XCOPY %STAGING%\CuriosityTesting %FITNESSE_ROOT%\NightlyRegression\CuriosityTesting /E /I >> %LOGFILE%
XCOPY %STAGING%\CuriosityTesting %FITNESSE_ROOT%\NightlyRegressionBackup\CuriosityTesting /E /I >> %LOGFILE%
XCOPY %STAGING%\CuriosityTesting %FITNESSE_ROOT%\NightlyRegressionDev\CuriosityTesting /E /I >> %LOGFILE%

REM Make Build Info available
COPY %STAGING%\CuriosityTesting\SETUp\content.txt.buildinfo.template %FITNESSE_ROOT%\NightlyRegression\CuriosityTesting\SETUp\content.txt /Y >> %LOGFILE%
COPY %STAGING%\CuriosityTesting\SETUp\content.txt.buildinfo.template %FITNESSE_ROOT%\NightlyRegressionBackup\CuriosityTesting\SETUp\content.txt /Y >> %LOGFILE%

REM Enable Timeout tests
COPY %SA20_HOME%\properties\enabled_properties.xml %FITNESSE_ROOT%\NightlyRegression\CuriosityTesting\CuriosityPlatformArchitecture\GenericApis\TimeoutTests\properties.xml /Y >> %LOGFILE%
COPY %SA20_HOME%\properties\enabled_properties.xml %FITNESSE_ROOT%\NightlyRegressionBackup\CuriosityTesting\CuriosityPlatformArchitecture\GenericApis\TimeoutTests\properties.xml /Y >> %LOGFILE%

REM Enable Random Tests
REM COPY %SA20_HOME%\properties\enabled_properties.xml %FITNESSE_ROOT%\NightlyRegression\CuriosityTesting\ZxRandomTests\properties.xml /Y >> %LOGFILE%
REM COPY %SA20_HOME%\properties\enabled_properties.xml %FITNESSE_ROOT%\NightlyRegressionBackup\CuriosityTesting\ZxRandomTests\properties.xml /Y >> %LOGFILE%

REM Enable HBAseIntegrationTests
COPY %SA20_HOME%\properties\enabled_properties.xml %FITNESSE_ROOT%\NightlyRegression\CuriosityTesting\ZyHbaseIntegrationTest\properties.xml /Y >> %LOGFILE%
COPY %SA20_HOME%\properties\enabled_properties.xml %FITNESSE_ROOT%\NightlyRegressionBackup\CuriosityTesting\ZyHbaseIntegrationTest\properties.xml /Y >> %LOGFILE%

REM Delete old Fixtures and Resources
DEL   %FIXTURE_HOME%\*.* /Q >> %LOGFILE%
RMDIR %FIXTURE_HOME%\resources /S /Q >> %LOGFILE%
RMDIR %FIXTURE_HOME%\dependency /S /Q >> %LOGFILE%

REM Copy New Fixtures and Resources
XCOPY %STAGING%\SA20Test\resources %FIXTURE_HOME%\resources /E /I >> %LOGFILE%
XCOPY %STAGING%\SA20Test\dependency %FIXTURE_HOME%\dependency /E /I >> %LOGFILE%
XCOPY %STAGING%\SA20Test\*.jar %FIXTURE_HOME%\ >> %LOGFILE%