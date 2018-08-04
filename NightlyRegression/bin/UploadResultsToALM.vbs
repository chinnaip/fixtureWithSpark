'==============================================================================
' UploadResultsToALM
'==============================================================================
'
' Description:
'	Script to upload test results to ALM/QC.
'   It can upload results for a single test or for multiple results based on a Fitnesse result file.
'
' Prerequisites:
'   ALM OTA API must be available on the script system.
'   Script must be run with 32-Bit interpreter: c:\windows\syswow64\cscript.exe
'   Inside QTP/ALM the "Test Sets" must be available in the "Test Lab"
'
'
'==============================================================================
' 
' Command Line Arguments:
'   -qcurl            e.g. "http://qc4e.atlanta.hp.com/qcbin"      ' Quality Center/ALM URL
'   -qcdomain         e.g. "TS_DEV"                                ' QC Domain   
'   -qcproject        e.g. "SA_20"                                 ' QC Project
'   -qcuser           e.g. "john.doe_hp.com"                       ' QC Username
'   -qcpassword       e.g. "mypass53cd7841c"                       ' QC/ALM Password
'
' The following parameters must only be used, if fitnesse resultfile should be uploaded
'   -qcbaseresultset  e.g. "Root\SA20\Release1404\Sprint1"         ' Baseresultset, from there the structure of the fitnessereults must be availabe
'   -suppresspath     e.g. "Mytest.ThisSuite"                      ' Suppress a path from the individual lines in the resultsfile, so it can be loaded into ALM
'   -fitnesseresults  e.g. "C:\results.txt"                        ' Text file with results provided from a Fitnesse test run  (created through e.g.>java -jar fitnesse-standalone.jar -c "MyTest?suite&format=text" > c:\result.txt)
'   -fitnesseserver   e.g. "http://myserver.deu.hp.com/"           ' Server where to get the detailled fitnesseresults
'   -fitnesseuser     e.g. "john_doe"                              ' In case fitnesse server is password protected
'   -fitnessepassword e.g. "mypw232332"                            ' Password for fitnesseserver
'
' The following parameters are only valid, if fitnesseresults are not provided 
'   -qcresultset      e.g. "Root\Temp\UploadTest"                  ' Resultset where the results will be uploaded to. In the resultset the test must be available
'   -testresult       e.g. "Failed"                                ' Result of the test either "Failed", "Passed" or "No Run"
'   -testname         e.g. "CheckUiproxydCrash"                    ' Name of the Test in the resultset (has to be exact!)       
'   -testhostname     e.g. "myhost.mydomain.com"                   ' Hostname, where the test ran (arbitrary string)
'   -startdatetime    e.g. "2012-11-04_12:13:33"                   ' Start Date and Time (Format: YYYY-MM-DD_HH24:MI:SS 
'   -testattachments  e.g. "c:\temp\log.txt;f:\sc\screenshot.png"  ' List of attachments semicolon separated 
'
' Example Calls:
'	C:\windows\syswow64\cscript.exe UploadResultsToALM.vbs -testname "CheckUiproxydCrash" -testresult "Passed"  -qcresultset "Root\Temp\UploadTest" -testattachments "C:\temp\log.txt;c:\buli\kk.txt" -qcpassword "53cd7841cf78a81dc0b121edbde4b606d51c437909e0"
'	C:\windows\syswow64\cscript.exe UploadResultsToALM.vbs -fitnesseresults "C:\results.txt" -qcbaseresultset "Root\Temp" -qcpassword "53cd7841cf78a81dc0b121edbde4b606d51c437909e0" 
'
'==============================================================================

'
' Check Arguments, and assign default values from configfile
'
Option Explicit

Dim qcurl           :qcurl               = CheckArgument("-qcurl", "http://qc4e.atlanta.hp.com/qcbin")                 
Dim qcdomain        :qcdomain            = CheckArgument("-qcdomain","TS_DEV")                                         
Dim qcproject       :qcproject           = CheckArgument("-qcproject","SA_20")                                         
Dim qcuser          :qcuser              = CheckArgument("-qcuser","martin.seibold_hp.com")                            
Dim qcpassword      :qcpassword          = CheckArgument("-qcpassword","") 
Dim qcresultset     :qcresultset         = CheckArgument("-qcresultset","")                        
Dim testresult      :testresult          = CheckArgument("-testresult","")                                       
Dim testname        :testname            = CheckArgument("-testname","")                                               
Dim testhostname    :testhostname        = CheckArgument("-testhostname","")                                  
Dim startdatetime   :startdatetime       = CheckArgument("-startdatetime","")                       
Dim testattachments :testattachments     = CheckArgument("-testattachments","") 
Dim fitnesseresults :fitnesseresults     = CheckArgument("-fitnesseresults","") 
Dim fitnesseserver  :fitnesseserver      = CheckArgument("-fitnesseserver","") 
Dim fitnesseuser    :fitnesseuser        = CheckArgument("-fitnesseuser","") 
Dim fitnessepassword:fitnessepassword    = CheckArgument("-fitnessepassword","") 
Dim qcbaseresultset :qcbaseresultset     = CheckArgument("-qcbaseresultset","") 
Dim suppresspath    :suppresspath        = CheckArgument("-suppresspath","") 

'
' Initialize variables
'
Dim tdc
Dim res

'Connect to server
Set tdc = CreateObject("TDAPIOLE80.TDConnection")
tdc.InitConnectionEx qcurl

'Authenticate user
tdc.Login qcuser, qcpassword

' Connect to project
tdc.Connect qcdomain, qcproject

' Upload the results, either single or a fitnesse file
If fitnesseresults = "" Then
	res = UploadSingleResult(tdc,qcresultset,testresult,testname,testhostname,startdatetime,testattachments)
Else
	res = UploadFitnesseResults(tdc,fitnesseresults,qcbaseresultset,suppresspath,fitnesseserver,fitnesseuser,fitnessepassword)
End If

' Disconnect from the project. 
tdc.Disconnect

' Log the user off the server
tdc.Logout

' Release the TDConnection object.
tdc.ReleaseConnection

' Release the object
Set tdc = Nothing


'==============================================================================
' UploadFitnesseResults
'==============================================================================
'
' Fitnesse results into ALM
'	
Function UploadFitnesseResults(oQcConnect,fitnesseresults,qcbaseresultset,suppresspath,fitnesseserver,fitnesseuser,fitnessepassword)
   
	Dim oFso, oDict, hFileRef, f
	Dim res, line, ret, row
	Dim aColumns
	
	Dim qcresultset     : qcresultset = ""
	Dim testresult      : testresult = ""
	Dim testname        : testname = ""
	Dim testhostname    : testhostname = ""
	Dim startdatetime   : startdatetime = ""
	Dim testattachment  : testattachment = ""
	Dim fitnesseurl     : fitnesseurl = ""
	Dim templogfile     : templogfile = ""
	Dim templogfilelocation : templogfilelocation = "C:\temp"
	
	If FileExists(fitnesseresults)=False Then
		Log "File does not exist!"
		UploadFitnesseResults = False
		Exit Function
	End If
	
	Set oFso = CreateObject("Scripting.FileSystemObject")
	Set oDict = CreateObject("Scripting.Dictionary")
	Set hFileRef = oFso.OpenTextFile(fitnesseresults, 1)
	
	row = 0
	Do Until hFileRef.AtEndOfStream
		line = hFileRef.Readline
		oDict.Add row, line
		row = row + 1
	Loop
	
	hFileRef.close
	
	' Read The File
	For Each line in oDict.Items
	
		' Replace tabs
		line = Replace(line,chr(9)," ")
		
		' Remove duplicate blanks from line so it can be split
		Do
			If InStr(1, line, "  ") > 0 Then
				line = Replace(line, "  ", " ")
			Else
				Exit Do
			End If
		Loop
	
		' Split the line by blanks
		aColumns=Split(line)
	
		' Create the parameters based on the contents of the result line. Skip over lines not starting with "." or "X" or "F"
		If ubound(aColumns)>5 Then
			If aColumns(0)="." or aColumns(0)="X" or aColumns(0)="F" Then
				
				' Testname
				testname      = aColumns(6)
				
				' Testresult
				Select Case aColumns(0)
					Case "."
						testresult = "Passed"
					Case else
						testresult = "Failed"
				End Select
				
				' Startdatetime (use date from the last modified date of the file plus the time from the file)
				Set f = oFso.GetFile(fitnesseresults)
				startdatetime = DatePart("yyyy",f.DateLastModified) & "-" & Right("0" & Cstr(month(f.DateLastModified)), 2) & "-" & Right("0" & Cstr(day(f.DateLastModified)), 2)
				startdatetime = startdatetime & "_" & aColumns(1)
				
				' QCResultset
				qcresultset = Mid(aColumns(7),2,len(aColumns(7))-2) 		         ' Extract the resultset remove the braces (Mytest.Level1.Test) => Mytest.Level1.Test
				qcresultset = Replace(qcresultset,suppresspath,"")                   ' Remove suppresspath from resultset
				qcresultset = Replace(qcresultset,".","\") 		                     ' Replace the . with \  Mytest.Level1.Test => Mytest\Level1\Test
				If InStrRev(qcresultset,"\") > 0 Then
					qcresultset = Mid(qcresultset,1,InStrRev(qcresultset,"\")-1) 	 ' Get rid of the testname Mytest\Level1\Test => Mytest\Level1
				Else                                                                  
					qcresultset = ""                                                 ' just in case this is a top level test (not likely)
				End If
				qcresultset   = qcbaseresultset + "\" + qcresultset                  ' Add the base resultset
				
				' Request the detailed results from Fitnesse server - only if fitnesseserver is provided
				If fitnesseserver <> "" Then
					' Build the URL from the results
					fitnesseurl = fitnesseserver & "/"
					fitnesseurl = fitnesseurl & Mid(aColumns(7),2,len(aColumns(7))-2)
					fitnesseurl = fitnesseurl & "?pageHistory&resultDate="
					fitnesseurl = fitnesseurl & DatePart("yyyy",f.DateLastModified) & Right("0" & Cstr(month(f.DateLastModified)), 2) & Right("0" & Cstr(day(f.DateLastModified)), 2)
					fitnesseurl = fitnesseurl & Replace(aColumns(1),":","")
					
					' Write the results into a temporary logfile name it like "C:\temp\mytest_2012-11-23_141223.html"
					templogfile = templogfilelocation & "\" & testname & "_" & Replace(startdatetime,":","") & ".html"
					
					' Execute the http request
					If GetUrl(fitnesseurl,fitnesseuser,fitnessepassword,templogfile) = True Then
						testattachment=templogfile
					Else	
						testattachment=""
					End If
				End If
					
				' Execute the upload of the results
				Log ""
				Log "Processing Fitnesseresult"
				Log qcresultset
				Log testname
				Log testresult
				Log startdatetime
				Log testattachment
			
				res = UploadSingleResult(oQcConnect,qcresultset,testresult,testname,testhostname,startdatetime,testattachment)
				
				' Cleanup
				If templogfile <> "" Then
					FileDelete(templogfile)
				End If
				
				If res <> True Then
					ret = False
				End If
			End If
		End If
	Next

	UploadFitnesseResults = ret
 
 End Function

               
'==============================================================================
' UploadSingleResult
'==============================================================================
'
' Function to upload the a single result into ALM
'
Function UploadSingleResult(oQcConnect,qcresultset,testresult,testname,testhostname,startdatetime,testattachments)
	Dim oFso
	Dim oTestFolder
	Dim oTSList, oTestSet, oTestSetFact, oTestSetTree 
	Dim oTestInstanceFactory, oTSTestList
	Dim oRunTest, oRunFact, oRun, oStorage
	Dim oAtt, oAttFact
	Dim i, strRunName
	Dim aAttachments
	Dim testnode : testnode=left(qcresultset,instrrev(qcresultset,"\"))
	Dim testset  : testset=right(qcresultset,len(qcresultset)-instrrev(qcresultset,"\"))

	'
	' Find Test Set and Test Case on the QC TestLab 
	'
	Set oTestSetFact         = oQcConnect.TestSetFactory         
	Set oTestSetTree         = oQcConnect.TestSetTreeManager                           
	Set oTestFolder          = oTestSetTree.NodeByPath(testnode)      
	Set oTSList              = oTestFolder.FindTestSets(testset)   
	
	If oTSList IS Nothing Then
		Log "Testset does not exist in QC."
		UploadSingleResult = False
		Exit Function 
	End If
	
	Set oTestSet             = oTSList.Item(1)
	Set oTestInstanceFactory = oTestSet.TSTestFactory
	Set oTSTestList          = oTestInstanceFactory.NewList("")               
					
	Dim bFound: bFound = False
	For i = 1 to oTSTestList.Count
		If oTSTestList(i).Name = "[1]" & testname Or oTSTestList(i).Name = testname Then
			Set oRunTest = oTSTestList(i)
			bFound = True
			oRunTest.Post
			oRunTest.Refresh
			Exit For
		End If
	Next
	
	'
	'If oRunTest Is Not Found
	'
	If bFound = False Then
		Log " Failed to find: " & testname & " on QC"
		UploadSingleResult = False
		Exit Function 
	End If
					
	Set oRunFact = oRunTest.RunFactory     
	
	'
	' Create a New Test Run
	'
	strRunName = testname & "_" & startdatetime 
	Set oRun = oRunFact.AddItem(CStr(strRunName))
	
	'
	' Post Test Case Status, Hostname and Build Number to the TESTRUN
	'
	oRun.Status = testresult
	oRun.Field("RN_HOST") = testhostname
	oRun.Post
	oRun.Refresh
	Log "Test Run created..."
	
	'
	' Upload attachments
	'
	If testattachments <> ""  Then
		aAttachments = split(testattachments,";")
		For i = 0 to Ubound(aAttachments)
			If FileExists(aAttachments(i)) Then
				Set oAttFact = oRun.Attachments
				Set oAtt = oAttFact.AddItem(GetFilename(aAttachments(i)))
				oAtt.Post
	
				Set oStorage = oAtt.AttachmentStorage
				oStorage.ClientPath = GetFilepath(aAttachments(i))
				oStorage.Save GetFilename(aAttachments(i)), True       
			Else
				Log "Attachment file does not exist"
			End If
		Next
		Log "Attachments uploaded..."
	End If
	
	
	Set oAtt = Nothing
	Set oAttFact = Nothing
	Set oRun = Nothing
	Set oRunFact = Nothing                
	Set oTSTestList = Nothing
	Set oTestInstanceFactory = Nothing
	Set oTestSet = Nothing
	Set oTSList = Nothing
	Set oRunTest = Nothing
	
	UploadSingleResult = True                       
	
End Function

'==============================================================================
' CheckArgument(parametername, defaultvalue)
'==============================================================================
'
' Check the Arguments of the script. If not provided the defaultvalue is returned
'
Function CheckArgument(parametername, defaultvalue)

	Dim strArg
	Dim objArgs
	Dim i:     i=0

	Set objArgs = Wscript.Arguments

	CheckArgument = defaultvalue

	For Each strArg in objArgs
		i = i + 1
		If strArg = parametername Then
			CheckArgument = objArgs(i)
			Exit Function
		End If
	Next

End Function

'==============================================================================
' GetFilename(sFile)
'==============================================================================
'
' Extracts only the filename from a path
'
Function GetFilename (sFilePath)

	Dim arrPARTS

	arrPARTS = split(sFilePath,"\")
	GetFilename = arrPARTS(ubound(arrPARTS))

End Function

'==============================================================================
' GetPath(sFile)
'==============================================================================
'
' Extracts only the path from a filepath
'
Function GetFilepath (sFilePath)

	GetFilepath = left(sFilepath,len(sFilepath)-len(GetFilename(sFilepath)))
                
End Function

'==============================================================================
' FileExists(sFileName)
'==============================================================================
'
' Checks wether the given sFileName exists or not. 
'
Function FileExists (sFileName)
	Dim oFso
	Set oFso = CreateObject("Scripting.FileSystemObject")
	If oFso.FileExists(sFileName) Then
		FileExists = True
	Else
		FileExists = False
	End If
	Set oFso = Nothing
End Function

'==============================================================================
' FileDelete(sFileName)
'==============================================================================
'
' Checks wether the given sFileName exists or not. 
'
Function FileDelete (sFileName)

	Dim oFso
    Set oFso = CreateObject("Scripting.FileSystemObject")
    oFso.DeleteFile (sFileName)
    Set oFso = nothing

End Function

'==============================================================================
' GetUrl(sUrl,sUser,sPassword,sFileName)
'==============================================================================
'
' Requests an URL and stores the body into a file
'
Function GetUrl (sUrl,sUser,sPassword,sFileName)
	Dim objHTTP
	Dim objFSO 
	Dim objTextFile 

	Set objHTTP = CreateObject("MSXML2.XMLHTTP")
	objHTTP.open "GET", sUrl, False, sUser, sPassword
	objHTTP.send

    Set objFSO = CreateObject("Scripting.FileSystemObject")
	Set objTextFile = objFSO.CreateTextFile(sFileName, True)
   
	objTextFile.Write (objHTTP.responseText)
	objTextFile.Close
	
	If objHTTP.Status = 200 Then
		GetUrl = True 
	Else
		GetUrl = False
	End If
	
End Function


'==============================================================================
' Log(sMessage)
'==============================================================================
'
' Logging to stdout
'
Sub Log(sMessage)
	Wscript.Echo (sMessage)
End Sub