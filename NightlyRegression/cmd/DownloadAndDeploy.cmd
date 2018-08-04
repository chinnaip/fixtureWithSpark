REM
REM DownloadAndDeploy.cmd - Command file to remotely execute download, initialization and installation scripts
REM

SET LOGFILE=C:\Shared\DownloadAndDeploy.log
SET HOST=nsl044.deu.hp.com

SET USER=uuu
SET PASSWORD=ppp
SET LOGIN=%USER%@%HOST%
SET UXSCRIPTS_SOURCE=C:\Staging\UnixScripts\*.*
SET UXSCRIPTS_DEST=/root/scripts
SET PLINK="C:\Program Files (x86)\putty\plink" -pw %PASSWORD% %LOGIN%
SET PSCP="C:\Program Files (x86)\putty\pscp"
SET PUTTY="C:\Program Files (x86)\putty\putty"

REM Upload the newest scripts to /root/scripts
%PSCP% -sftp -pw %PASSWORD% %UXSCRIPTS_SOURCE% %LOGIN%:%UXSCRIPTS_DEST%

REM Execute the commands
%PLINK% chmod 775 %UXSCRIPTS_DEST%/* > %LOGFILE%

REM Create Kafka topics
%PLINK% %UXSCRIPTS_DEST%/start_kafka.sh >> %LOGFILE%
%PLINK% sleep 20
%PLINK% %UXSCRIPTS_DEST%/create_sa2_kafka_topics.sh >> %LOGFILE%
%PLINK% sleep 20

REM Download newest builds
%PLINK% %UXSCRIPTS_DEST%/download_builds.sh >> %LOGFILE%

REM Deploy Web
REM %PLINK% %UXSCRIPTS_DEST%/deploy_web.sh >> %LOGFILE%

REM Start HbaseIntegration Tests
%PLINK% %UXSCRIPTS_DEST%/hbase-it.sh > C:\Temp\HbaseIntegrationTests_NIGHTLY_REGRESSION.log

REM Initialize HBase
%PLINK% %UXSCRIPTS_DEST%/init_hbase.sh >> %LOGFILE%

REM Submit Topology
%PLINK% %UXSCRIPTS_DEST%/submit_topology.sh >> %LOGFILE%
%PLINK% sleep 60

REM Start Rest Interface
%PLINK% %UXSCRIPTS_DEST%/start_rest.sh >> %LOGFILE%
%PLINK% sleep 60

