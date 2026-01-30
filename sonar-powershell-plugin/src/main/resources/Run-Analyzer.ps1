param(
	[string]$inputDir,
	[string]$outputDir,
	[string]$includeDefaultRules = "1",
	[string]$includeCustomRules = "0",
	[string]$customRulesPath = [string]::Empty,
	[string]$excludeRule = [string]::Empty,
	[string]$runPester = "0",
	[string]$debugOutputEnabled = "0"
)

function ConvertTo-Boolean
{
	param(
		[string]$Value
	)

	$intValue = [System.Convert]::ToInt16($Value)
	return $intValue -ne 0
}

$ErrorActionPreference = "Stop"
$PSDefaultParameterValues['*:Verbose'] = (ConvertTo-Boolean -Value $debugOutputEnabled)

Import-Module PSScriptAnalyzer -ErrorAction Stop

if(-not (Test-Path -Path $inputDir)) {
	throw "Input directory not found: $inputDir"
}

$params = @{
	Path = $inputDir
	Recurse = $true
	IncludeDefaultRules =  ConvertTo-Boolean -Value $includeDefaultRules
}

if(-not [string]::IsNullOrEmpty($excludeRule))
{
    $excludedRuleList = $excludeRule -split ","
	$params.Add("ExcludeRule", $excludedRuleList)
}

if(-not [string]::IsNullOrEmpty($customRulesPath)) {
	if((ConvertTo-Boolean -Value $includeCustomRules)) {
		if((Test-Path -Path $customRulesPath)) {
			$params.Add("CustomRulePath", $customRulesPath)
			write-Verbose "Using custom rules path: $customRulesPath"
		}
		else {
			Write-Verbose "Custom rules path not found: $customRulesPath"
		}
	}
}

Write-Host "PS: Analyzing scripts in directory: $inputDir"

$results = Invoke-ScriptAnalyzer @params |
    Select-Object RuleName,
                  @{ Name = 'Severity'; Expression = { $_.Severity.ToString() } },
                  ScriptName,
                  Line,
                  Message


Write-Host "Writing results to: $outputDir"
$fileContent = $results | ConvertTo-Json -Depth 5
if ($fileContent -notmatch '^\s*\[') {
    $fileContent = "[$fileContent]"
}

$fileContent | Out-File $outputDir -Encoding UTF8

if(-not (ConvertTo-Boolean -Value $runPester)) {
	exit
}

Copy-Item -Path $outputDir -Destination (Join-Path $inputDir -ChildPath "artifacts/ScriptAnalyzerFindings.json") -Force

$pesterModule = Get-Module -ListAvailable -ErrorAction SilentlyContinue | where-Object { $_.Name -eq 'Pester' }
if($null -eq $pesterModule -or $pesterModule.Version.ToString() -lt '5.5.0'){
	Write-Verbose "Pester Module missing or outdated."
	exit
}

#region Pester Functions
function ConvertTo-SonarQubeDiagnisticFile
{
	param(
		[string]$PesterTestReportFile,
		[string]$CoverageReportFile
	)

	$testResults = [xml] (Get-Content -Path $PesterTestReportFile)
	$testSuites = $testResults | Select-Xml -Xpath "//testsuite"
    $tests = Get-Tests -TestSuites $testSuites

	# $pesterExecutionReportPath -> C:\DEV\sonar-ps-test\artifacts\PesterExecutionReport.xml
	$artifactsDir = Split-Path -Path $PesterTestReportFile -Parent
	$pesterExecutionReportPath = Join-Path $artifactsDir -ChildPath "PesterExecutionReport.xml"
    Write-Host "Generating Pester Execution Report: $pesterExecutionReportPath"

	$xmlWriter = Open-XmlWriter -Path $pesterExecutionReportPath
	Get-ExecutionReport -xmlWriter $xmlWriter -Test $tests -ScriptPath $inputDir
    Close-XmlWriter -XmlWriter $xmlWriter

	#$coverageReportFilePath = Join-Path $inputDir -ChildPath "artefacts/pesterTestCoverage.xml"
    $coverageResults = [xml] (Get-Content -Path $CoverageReportFile)
    $folders = $coverageResults | Select-Xml -XPath "//package "

	$pesterCoverageReportPath = Join-Path $artifactsDir -ChildPath "SonarCodeCoverage.xml"

	Write-Host "Generating Coverage Report: $pesterCoverageReportPath"
	$xmlWriter = Open-XmlWriter -Path $pesterCoverageReportPath
	$inputDir = Split-Path -Path $artifactsDir -Parent
	Get-CodeCoverageReport -Folders $folders -ScriptPath $inputDir -xmlWriter $xmlWriter
	Close-XmlWriter -XmlWriter $xmlWriter

}

function Get-CodeCoverageReport
{
    <#
        .SYNOPSIS
            Create a XML file file that can be used by SonarQube to analysie Code coverage
        .PARAMETER Folders
            List of 'package' nodes from Pester test code coverage file
        .PARAMETER ScriptPath
            Root folder for the script, will be stripped from full path in the report
        .PARAMETER xmlWriter
            System.XMl.XmlTextWriter object
        .LINK
            https://docs.sonarsource.com/sonarqube/9.9/analyzing-source-code/test-coverage/generic-test-data/
    #>
    param(
        [array]$Folders,
        [string]$ScriptPath,
        [System.XMl.XmlTextWriter]$xmlWriter
    )
    $xmlWriter.WriteStartElement("coverage")
    $xmlWriter.WriteAttributeString("version","1")

    foreach($folder in $Folders)
    {
        $folderPath = $folder.node.name
        $files = $folder | Select-Xml -Xpath ".//sourcefile"
        foreach($file in $files){
            $path = "{0}/{1}" -f $folderPath, $file.node.name
            $ScriptPath = $ScriptPath.Replace("\","/")
            $path = $path.Replace($ScriptPath,"")
            $path = $path.Substring(1, $path.Length - 1 )

            $xmlWriter.WriteStartElement("file")
            $xmlWriter.WriteAttributeString("path",$path)

            $lines = $file | Select-Xml -XPath ".//line"
            foreach($line in $lines){
                $xmlWriter.WriteStartElement("lineToCover")
                $xmlWriter.WriteAttributeString("lineNumber",$line.node.nr)
                $covered = (0 -lt [int]$line.node.ci)
                $xmlWriter.WriteAttributeString("covered",$covered.ToString().ToLower())
                $xmlWriter.WriteEndElement()                  # lineToCover
            }
            $xmlWriter.WriteEndElement()                      # </file>
        }
    }
    $xmlWriter.WriteEndElement()                              # </coverage>
}


function Open-XmlWriter
{
    <#
        .SYNOPSIS
            Starting a XML file
        .PARAMETER Path
            Path for the file to create
    #>
    [cmdletbinding()]
    [OutputType([System.XMl.XmlTextWriter])]
    param(
        [string]$Path
    )
    $xmlWriter = New-Object System.XMl.XmlTextWriter($Path, [System.Text.Encoding]::UTF8)
    $xmlWriter.Formatting = 'Indented'
    $xmlWriter.Indentation = 1
    $xmlWriter.IndentChar = "`t"
    $xmlWriter
}

function Close-XmlWriter
{
    <#
        .SYNOPSIS
            Close a XML file
        .PARAMETER XmlWriter
            System.XMl.XmlTextWriter object to close

    #>
    [cmdletbinding()]
    [OutputType([System.Void])]
    param(
        [System.XMl.XmlTextWriter]$XmlWriter
    )
    $XmlWriter.Flush()
    $XmlWriter.Close()
}


function Get-Tests
{
    <#
        .SYNOPSIS
            Get Tests from Pester test results file 'testsuite' nodes
        .PARAMETER TestSuites
            List of 'testsuite' nodes from Pester test results file
    #>
    [cmdletbinding()]
    [OutputType([hashtable])]
    param(
        [array]$TestSuites
    )

    $testsInScript = [System.Collections.Generic.List[PSCustomObject]]::new()
    foreach($testSuite in $TestSuites)
    {
        $testCases = @($testSuite.Node.SelectNodes(".//testcase"))
        foreach($testCase in $testCases)
        {
            $functionName = $testCase.name.Split(".")[0]        # first part is 'Describe' from test
            $testName = $testCase.name.Split(".")[1]
            $testFilePath = $testCase.classname
            $testTime = $testCase.time

            $test = New-Object -TypeName PSCustomObject
            $test | Add-Member -MemberType NoteProperty -Name SUT -Value $functionName
            $test | Add-Member -MemberType NoteProperty -Name TestName -Value $testName
            $test | Add-Member -MemberType NoteProperty -Name FilePath -Value $testFilePath
            $test | Add-Member -MemberType NoteProperty -Name Duration -Value $testTime

            if($testCase.status -eq "Failed")
            {
                $errorMessage = ($testCase.ChildNodes | Select-Object -First 1).message
                $test | Add-Member -MemberType NoteProperty -Name Result -Value $testCase.status
                $test | Add-Member -MemberType NoteProperty -Name ErrorText -Value $errorMessage
            }

            if($testCase.status -eq "Passed")
            {
                $test | Add-Member -MemberType NoteProperty -Name Result -Value "success"
            }

            $testsInScript.Add($test)
        }
    }

    $testsInScript
}


function Get-ExecutionReport
{
    <#
        .SYNOPSIS
            Create a XML file file that can be used by SonarQube to analysie Test execution
        .PARAMETER xmlWriter
            System.XMl.XmlTextWriter object
        .PARAMETER Tests
            List of Test objects created by Get-Tests function
        .PARAMETER ScriptPath
            Root folder for the script, will be stripped from full path in the report
    #>
    [cmdletbinding()]
    [OutputType([System.Void])]
    param(
        [System.XMl.XmlTextWriter]$xmlWriter,
        [System.Collections.Generic.List[PSCustomObject]]$Tests,
        [string]$ScriptPath
    )

    $xmlWriter.WriteStartElement("testExecutions")           # <testExecutions version="1">
    $xmlWriter.WriteAttributeString("version","1")

    $testGroups = $tests | Group-Object Filepath
    $groupCount = $testGroups | Measure-Object | Select-Object -ExpandProperty Count

    for ($index_1 = 0; $index_1 -lt $groupCount; $index_1++) {
        $path = $testGroups[$index_1].Name.Replace($ScriptPath,"")
        $path = $path.Substring(1, $path.Length - 1).Replace("\","/")

        $xmlWriter.WriteStartElement("file")                     # <file path="Tests/Invoke-CommonFunctions.Tests.ps1">
        $xmlWriter.WriteAttributeString("path",$path)

        $grouping = $testGroups[$index_1].Group
        for($index_2 = 0; $index_2 -lt $grouping.Count; $index_2++)
        {
            # <testCase name="Find-OperationalDomainController" duration="3.8439"/>
            $xmlWriter.WriteStartElement("testCase")
            $xmlWriter.WriteAttributeString("name",$grouping[$index_2].TestName)
            $duration = [double]::Parse($grouping[$index_2].Duration, [cultureinfo]'en-us') * 10000
            $xmlWriter.WriteAttributeString("duration", $duration)
            if($grouping[$index_2].Result -ne "success")
            {
                $msgParts = $grouping[$index_2].ErrorText.Split("`n")
                $xmlWriter.WriteStartElement("error")
                $xmlWriter.WriteAttributeString("message",$msgParts[0].Trim())
                $xmlWriter.WriteString($grouping[$index_2].ErrorText)

                $xmlWriter.WriteEndElement()
            }

            $xmlWriter.WriteEndElement()
        }

        $xmlWriter.WriteEndElement()                          # </file>
    }
    $xmlWriter.WriteEndElement()                       # testExecutions
}

#endregion Pester Functions

#region Pester Execution
Import-Module Pester -ErrorAction Stop
if($null -eq (Get-Module -Name Pester -ErrorAction SilentlyContinue)){
	Write-Error "Failed to import Pester module."
	exit
}

$artifactsPath = Join-Path $inputDir -ChildPath "artifacts"
$pesterTestReportFile = Join-Path $artifactsPath -ChildPath "PesterTestReport.xml"
$coverageReportFile = Join-Path $artifactsPath -ChildPath "PesterTestCoverage.xml"
$testsPath = Join-Path $inputDir -ChildPath "tests"

if(-not (Test-Path -Path $testsPath)) {
	Write-Verbose "No tests directory found at path: $testsPath"
	exit
}

if(-not (Test-Path -Path $artifactsPath)) {
	Write-Verbose "Creating artifacts directory at path: $artifactsPath"
	New-Item -Path $artifactsPath -ItemType Directory | Out-Null
}


Write-Verbose "Running Pester tests in path: $testsPath"

# https://pester-docs.netlify.app/docs/commands/New-PesterConfiguration

$configuration = @{
    Run = @{
        PassThru = $false
        Path = "$testsPath/*"
        TestExtension = '.Tests.ps1'
    }
    CodeCoverage = @{
        Enabled = $true
        Path = @('./*.ps*','modules/*.ps*')
        OutputPath = $coverageReportFile
        OutputFormat = 'CoverageGutters'       # JaCoCo, CoverageGutters
        OutputEncoding = 'UTF8'
    }
    TestResult = @{
        Enabled = $true
        OutputFormat = "JUnitXml"      #  NUnitXml, NUnit2.5, NUnit3 or JUnitXml
        OutputPath = $pesterTestReportFile
        OutputEncoding = 'UTF8'
        TestSuiteName = 'Pester'
    }
    Output = @{
        Verbosity = 'Normal'           # Detailed, Normal, Filtered, Auto, Error
    }
}

$config = New-PesterConfiguration -Hashtable $configuration
Invoke-Pester -Configuration $config

<#
# normalize project root: forward slashes and lowercase
$normalizedInputDir = ($inputDir -replace '\\','/').ToLower()

[xml]$xml = Get-Content $coverageReportFile

if ($xml.DocumentType) {
    $xml.RemoveChild($xml.DocumentType) | Out-Null
}

foreach ($package in $xml.report.package) {

    # fix package name
    $packageNameNormalized = ($package.name -replace '\\','/').ToLower()
    if ($packageNameNormalized -eq $normalizedInputDir -or $packageNameNormalized.StartsWith($normalizedInputDir + "/")) {
        $package.name = "."
    }

    # fix class names
    foreach ($class in $package.class) {
     # normalize class name for comparison
        $classNameNormalized = ($class.name -replace '\\','/').ToLower()

        # remove project root prefix if it matches
        if ($classNameNormalized.StartsWith($normalizedInputDir)) {
            $relativePath = $classNameNormalized.Substring($normalizedInputDir.Length).TrimStart('/')
            $class.name = $relativePath
        }
    }
}


$xml.Save($coverageReportFile)
#>

write-Verbose "Pester test report written to: $coverageReportFile"
write-Verbose "Starting code coverage report generation."

# $pesterTestReportFile -> C:\DEV\sonar-ps-test\artifacts\PesterTestReport.xml
# $coverageReportFile -> C:\DEV\sonar-ps-test\artifacts\PesterTestCoverage.xml
ConvertTo-SonarQubeDiagnisticFile -PesterTestReportFile $pesterTestReportFile -CoverageReportFile $coverageReportFile


#endregion Pester Execution


