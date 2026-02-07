# sonarqube-powershell-plugin

* [What the Plugin does](#what-the-plugin-does)
* [Prerequisites](#prerequisites)
    * [Folder Structure](#folder-structure)
    * [Required Software](#required-software)
* [Installation](#installation)
* [Configuration](#configuration)
* [Illustration](#illustration)

## What the Plugin does
- Runs Built-in PSScriptAnalyzer Rules and displays Findings in Code.
- Integrates custom PSScriptAnalyzer Rules the same way as it handles Built-in Rules. This Part can be turned on/off in SonarQube Administration.
- Runs Pester Tests and calculates and shows covered parts of the Code (and Code not covered). This Part can also be turned on/off in SonarQube Administration.
- Calculates and displays
    - Duplicate Code
    - Cyclomatic Complexity
    - Cognitive Complexity
    - Technical Debt
- Uses Code Highlighting to make the Scripts more readable.
- Produces a lot of Debug output if turned on in  SonarQube Administration.

This repository contains two different styles of a SonarQube Powershell Plugin.

Originally I was inspired by the [sonar-ps-plugin](https://github.com/gretard/sonar-ps-plugin) Powershell Plugin which I used for quite a while. And I really appreciate the work done. But it did not work anymore and I think it is mainly because of SonarQube changed the way Language definition is made in newer versions of the API.

Now it is:

```
public PowershellLanguage(final Configuration configuration)
```
compared to the old way:
```
public PowershellLanguage(final Settings settings)
```

Beside this, it has also outdated dependencies. But never the less, the folder "sonarqube-powershell-plugin/src/branch/main/sonar-ps-plugin" contains a working example of gretard's repo.

I started to re-write a new Plugin for the following reasons:
- First of all I wanted to understand what gretard code is actually doing. And I am now able to update the code on my own.
- I wanted to be compatible to the current API's.
- I use custom PSScriptAnalyzer rules and gretard's Plugin must be re-compiled and re-deployed for every new rule I want to use. In my project new rules can be used without any change in the current SonarQube configuration.
- I also wanted to integrate Pester Test results and Code Coverage from Pester Tests.

But there is a disadvantage which comes with the flexibility!
In gretard's implementation every rule is baked into the code at compile time. This allows to add much more information to rules. Unforunately SonarQube dos not allow to change the part "Why is this an issue" in the Sensor. At least I did not find a way yet.

I decided to have only 3 rules and every issue is matched to one of these rules based on Severity. I can add more custom PSScriptAnalyzer rules and their findings appear in SonarQube without the need to modify configuration files and re-compile the project.

# Prerequisites
## Folder Structure
In my projects I use this folder structure.
- 'project' is the root folder. It can be named as you like. This folder contains the files and folders to be scanned by PSScriptAnalyzer and Pester. Inclusions and exclusions are explained below.
- 'artifacts' is the folder for the files transferred to SonarQube at the end of the scan process. If it does not exist, it will be created.
- 'tests' folder contains the Pester test files.

The process also creates intermediate files. These files are located in the users %temp% folder.

```
project/
 -  artifacts/
 -  tests/
     - my-script-to-be-analyzed.Tests.ps1
 - sonar-project.properties
 - my-script-to-be-analyzed.ps1
 - my-module-to-be-analyzed.psm1

```

You can have more sub-folders and manage inclusions and exclusions in the properties file explained later.

## Required Software

| Plugin Version | PSScriptAnalyzer | Pester | Java | SonarQube |
| --- | --- | --- | --- | --- |
| 0.3.0 | 1.24.0 | 5.7.1 | 17+ | 26.1.0.118079 |


I run my Tests on Windows 11 (never tested it on Linux).

# Installation
To install the Plugin just copy the jar file to the 'downloads' folder of your SonarQube instance and restart SonarQube.

# Configuration
You can configure the Plugin in SonarQube GUI or you can set it the sonar-project.properties file.

You have to set these options with project related values
- sonar.organization
- sonar.projectName
- sonar.projectKey
- sonar.host.url
- sonar.token


The following options might be important to run the plugin depending on your project:

| # | Option | Vaule | Description |
| --- | ---- | --- | ---- |
| 1 | sonar.sourceEncoding | UTF-8 | files are UTF-8 encoded |
| 2 | sonar.sources | . | working directory is the current folder |
| 3 | sonar.language | powershell | must be "powershell" to trigger the plugin |
| 4 | sonar.lang.patterns.powershell | \*\*/\*.ps1,**/*.psm1  | regex which defines the files which should be assined to the language 'powershell' |
| 5 | sonar.inclusions | \*\*/\*.ps1,**/*.psm1 | regex which defines files should be analyzed |
| 6 | sonar.exclusions | artifacts/**,tests/*.Tests.ps1 | files and folders which should be excluded from scan |
| 7 | sonar.dynamicAnalysis | reuseReports | |
| 8 | sonar.tests | tests | the folder where test files for Pester tests are stored |
| 9 | sonar.testExecutionReportPaths | artifacts/PesterExecutionReport.xml | I have a "artifacts" folder where reports are stored before they are transmitted to Sonarqube. The filename is currently hardcoded. |
| 10 | sonar.coverageReportPaths | artifacts/SonarCodeCoverage.xml | Same as testExecutionReportPaths |
| 11 | sonar.dependencyCheck.skip | true | there is no dependency check |
| 12 | sonar.scm.disabled | true | |
| 13 | sonar.projectVersion | 0.9.001 | you can set every version you like |
| 14 | sonar.verbose | true | determines the amount of output Sonarqbube produces during the scan |
| 15 | psscriptanalyzer.customrules.path | c:\dev\PSScriptAnalyzerRules\\*.psm1 | folder containing custom rules for PSScriptanalyzer. This is NOT used if the follwoing option is set to false |
| 16 | psscriptanalyzer.customrules.enabled | true | determines if custom rules are used to analyze the scripts |
| 17 | psscriptanalyzer.defaultrules.enabled | true | can be used to only run custom rules and ignore the PSScriptanalyzer default rules |
| 18 | psscriptanalyzer.debugoutput.enabled | false | produces a lot more output during scan when set to true |
| 19 | psscriptanalyzer.exclude.rule | | A comma-seperated list of PSScriptAnalyzer Rule Names that will be skipped |
| 20 | psscriptanalyzer.pester.enabled | true | runs Pester after PSScriptAnalyzer has finished |
| 21 | sonar.jacoco.reportPaths | artifacts | folder where SonarQube expects file with scan results |
| 22 | sonar.jacoco.xmlReportPaths | artifacts | same as 21 |
| 23 | sonar.java.coveragePlugin | jacoco | Plugin used for code analysis |
| 24 | sonar.cpd.powershell.minimumLines | | number of duplicated lines which raise a problem in duplication detection |
| 25 | sonar.cpd.powershell.minimumTokens | | number of duplicated tokens which raise a problem in duplication detection |
| 26 | sonar.web.maxContentLength | | max. size of content length which can be transferred to SonarQube. |
| 27 | sonar.max.file.size | | max file size of result files transferred to SonarQube |

Settings starting with 'psscriptanalyzer' can also be found and set SonarQube GUI

Administration -> Configuration -> PSScriptAnalyzer

# Illustration
Here are a few pictures of my running instance.
## Code highlighted for readability
![Code highlited for readability](/pictures/Code_Highlighted.png)
![Code highlited to point out problems](/pictures/Code_not_Covered_New_code.png)
## Overview of Findings and Numbers per File
![Overview of Findings and Numbers per File](pictures/Project_Numbers_1.png)
## PSScriptAnalyzer Finding in Detail
![PSScriptAnalyzer Finding in Detail](/pictures/Findings.png)
## Complexity
![Complexity](/pictures/Project_Numbers_2.png)
## File Size
![File Size](/pictures/Project_Numbers_3.png)
## Duplicate Code Overview
![Duplicate Code Overview](/pictures/Project_Numbers_4.png)
## Duplicate Code in Detail
![Duplicate Code in Detail](/pictures/Duplications.png)
## Code Coverage
![Code Coverage](/pictures/Project_Numbers_5.png)
## Maintainablility
![Maintainablility](/pictures/Project_Numbers_6.png)
## Plugin Settings
![Plugin Settings](/pictures/Settings.png)
