# sonarqube-powershell-plugin

This repository contains two different styles of a Sonarube Powershell Plugin.

Originally I was inspired by the [sonar-ps-plugin](https://github.com/gretard/sonar-ps-plugin) Powershell Plugin which I used for quite a while. But it did not work anymore and I think it is mainly because of Sonarqube changed the way Language definition is made in newer versions of the API.

```
public PowershellLanguage(final Configuration configuration)
```
compared to the old way
```
public PowershellLanguage(final Settings settings)
```

Beside this, it has also outdated dependencies. But never the less, the folder "sonarqube-powershell-plugin/src/branch/main/sonar-ps-plugin" contains a working example of gretard's repo.

I started to write a new Plugin for the following reasons:
- I wanted to be compatible to the current API's
- I use custom PSScriptAnalyzer rules and gretard's Plugin must be re-compiled and re-deployed for every new rule I want to use. In my project new rules can be used without any change in the current Sonarqube configuration.
- I also wanted to integrate Pester Test results and Code Coverage from Pester Tests.

But there is a disadvantage which comes with the flexibility!
In gretard's implementation every rule is baked into the code at compile time. This allows to add much more information to rules.

I decided to have only 3 rules and every issue is matched to one of these rules based on Severity. I can add more custom PSScriptAnalyzer rules and their findings appear in Sonarqube without the need to modify configuration files and re-compile the project.

# Prerequisites
## Folder Structure
In my projects I use this folder structure.
- 'project' is the root folder. It can be named as you like. This folder contains the files and folders to be scanned by PSScriptAnalyzer and Pester. Inclusions and exclusions are explained below.
- 'artifacts' is the folder for the files transferred to Sonarqube at the end of the scan process.
- 'tests' folder contains the Pester test files.
- 'lib' folder is just to make clear that it is possible to have scripts and modules also in subfolders.

The process also creates intermediate files. These files are located in the users %temp% folder.

```
project/
 -  artifacts/
 -  tests/
     - my-script-to-be-analyzed.Tests.ps1
 -  lib/
     - my-other-module-to-ba-analyzed.psm1
 - sonar-project.properties
 - my-script-to-be-analyzed.ps1
 - my-module-to-be-analyzed.psm1

```

## PSScriptAnalyzer
Works with PSScriptAnalyzer Version 1.24.0

## Pester
Works with Pester Version 5.7.1

# Configuration
You can configure the Plugin in Sonarqube GUI or you can set it the sonar-project.properties file.

ou have to set these options with project related values
- sonar.organization
- sonar.projectName
- sonar.projectKey
- sonar.host.url
- sonar.token

The following options are important to run the plugin:

| # | Option | Vaule | Description |
| --- | ---- | --- | ---- |
| 1 | sonar.sourceEncoding | UTF-8 | files are UTF-8 encoded |
| 2 | sonar.sources | . | working directory is the current folder |
| 3 | sonar.language | powershell | must be "powershell" to trigger the plugin |
| 4 | sonar.lang.patterns.powershell | \*\*/\*.ps1,**/*.psm1  | regex which defines the files which should be assined to the language 'powershell' |
| 5 | sonar.inclusions | \*\*/\*.ps1,**/*.psm1 | regex which defines files to be analyzed |
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
| 19 | psscriptanalyzer.pester.enabled | true | runs Pester after PSScriptAnalyzer has finished |

Settings starting with 'psscriptanalyzer' can also be found and set Sonarqube GUI

Administration -> Configuration -> PSScriptAnalyzer

