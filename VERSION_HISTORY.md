# Version History
* [Version 0.4.0](#version-040)
* [Version 0.3.0](#version-030)

# Version 0.4.0
| # | Feature | Description |
| --- | --- | --- |
| 1 | SonarQube | PSScriptAnalyzer Tests and their Details can be loaded into SonarQube via XML Files  |
| 2 | PSScriptAnalyzer Plugin Settings | Path to XML Files for Test Details can be set |


# Version 0.3.0
The first Version I published on Github. These are the Key Features:
| # | Feature | Description |
| --- | --- | --- |
| 1 | PSScriptAnalyzer | PSScriptAnalyzer will be invoked and Results are transferred to SonarQube. There are 3 pre-defined Rules in SonarQube and PSScriptAnalayzer Findings are matched by Severity. |
| 2 | Pester | Pester can optionally be invoked and Results are transferred to SonarQube<br>- Each Line covered by  a Test is marked as covered <br>- All other lines are marked as not covered<br>- Percentage of covered and not covered Code is displayed in SonarQube |
| 3 | SonarQube Duplicate Code | Duplicated Lines within Script and within the Project are detected and marked by SonarQube |
| 4 | SonarQube Cyclomatic Complexity | Cyclomatic Complexity is calculated per Script and displayed in SonarQube |
| 5 | SonarQube Cognitive Complexity | Cognitive Complexity is calculated per Script and displayed in SonarQube |
| 6 | SonarQube Code Highlighting | Scripts are highlighted for better readability |
| 7 | SonarQube Technical Dept | Remediation Effort is calculated by SonarQube based the estimated Effort set in the 3 pre-defined Rules |
| 8 | PSScriptAnalyzer Plugin Settings | - Path to custom PSScriptAnalyzer Rules can be set<br>- Custom Rules can be enabled/disabled<br>- Default PSScriptAnalyzer Rules can be enabled/disabled<br>- Debug Output can be enabled/disabled. Output is produced in the Terminal where the Scanner runs<br>- Deletion of internmediate Files can be enabled/disabled. Sometimes it is usefull to see, what the Analyzer script produces.<br>- Invokation of Pester can be enabled/disabled
